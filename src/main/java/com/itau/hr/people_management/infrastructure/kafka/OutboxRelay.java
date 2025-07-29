package com.itau.hr.people_management.infrastructure.kafka;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.itau.hr.people_management.infrastructure.outbox.entity.OutboxMessage;
import com.itau.hr.people_management.infrastructure.outbox.enumeration.OutboxMessageStatus;
import com.itau.hr.people_management.infrastructure.outbox.repository.OutboxMessageRepository;

@Component
public class OutboxRelay {
    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);
    private static final String EMPLOYEE_CREATED_EVENT = "com.itau.hr.people_management.domain.employee.event.EmployeeCreatedEvent";
    private static final String EMPLOYEE_STATUS_CHANGED_EVENT = "com.itau.hr.people_management.domain.employee.event.EmployeeStatusChangedEvent";
    
    private final OutboxMessageRepository outboxMessageRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Map<String, String> eventTypeToTopicMap;
    private final RetryConfiguration retryConfiguration;
    
    @Value("${application.outbox.batch-size:10}")
    private int batchSize;

    public OutboxRelay(OutboxMessageRepository outboxMessageRepository, 
                      KafkaTemplate<String, String> kafkaTemplate,
                      @Value("${application.outbox.max-retries:5}") int maxRetries,
                      @Value("${application.outbox.backoff-durations:5,10,30,60,300}") String backoffDurationsStr) {
        this.outboxMessageRepository = outboxMessageRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.eventTypeToTopicMap = initializeEventTopicMapping();
        this.retryConfiguration = new RetryConfiguration(maxRetries, parseBackoffDurations(backoffDurationsStr));
    }

    @Scheduled(fixedDelayString = "${application.outbox.relay-delay:5000}")
    @Transactional 
    public void processOutbox() {
        List<OutboxMessage> pendingMessages = fetchPendingMessages();

        if (pendingMessages.isEmpty()) {
            log.debug("No pending outbox messages to process.");
            return;
        }

        log.info("Processing {} pending outbox messages.", pendingMessages.size());
        pendingMessages.forEach(this::processMessage);
    }

    private List<OutboxMessage> fetchPendingMessages() {
        List<OutboxMessageStatus> statuses = List.of(
            OutboxMessageStatus.PENDING, 
            OutboxMessageStatus.FAILED
        );
        
        return outboxMessageRepository.findByStatusInAndNextAttemptAtBeforeOrderByOccurredOnAsc(
            statuses, 
            Instant.now(), 
            PageRequest.of(0, batchSize)
        );
    }

    private void processMessage(OutboxMessage message) {
        try {
            String topic = getTopicForEventType(message.getEventType());
            sendMessageToKafka(message, topic);
        } catch (IllegalArgumentException e) {
            handleUnknownEventType(message);
        } catch (Exception e) {
            handleUnexpectedError(message, e);
        }
    }

    private void sendMessageToKafka(OutboxMessage message, String topic) {
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(
            topic, 
            message.getAggregateId().toString(), 
            message.getPayload()
        );

        future.whenComplete((result, ex) -> handleKafkaResult(message, topic, ex));
    }

    private void handleKafkaResult(OutboxMessage message, String topic, Throwable ex) {
        if (ex == null) {
            handleSuccessfulSend(message, topic);
        } else {
            handleFailedSend(message, topic, ex);
        }
    }

    private void handleSuccessfulSend(OutboxMessage message, String topic) {
        markMessageAsSuccessful(message);
        saveMessage(message);
        log.debug("Outbox message {} sent to Kafka topic {}", message.getId(), topic);
    }

    private void markMessageAsSuccessful(OutboxMessage message) {
        message.setStatus(OutboxMessageStatus.SENT);
        message.setProcessedAt(Instant.now());
        message.setRetryAttempts(0);
        message.setNextAttemptAt(null);
    }

    private void handleFailedSend(OutboxMessage message, String topic, Throwable ex) {
        incrementRetryAttempt(message);
        
        if (shouldMoveToDeadLetter(message)) {
            moveMessageToDeadLetter(message, topic, ex);
        } else {
            scheduleRetry(message, topic, ex);
        }
        
        message.setProcessedAt(Instant.now());
        saveMessage(message);
    }

    private void incrementRetryAttempt(OutboxMessage message) {
        message.setRetryAttempts(message.getRetryAttempts() + 1);
    }

    private boolean shouldMoveToDeadLetter(OutboxMessage message) {
        return message.getRetryAttempts() >= retryConfiguration.getMaxRetries();
    }

    private void moveMessageToDeadLetter(OutboxMessage message, String topic, Throwable ex) {
        message.setStatus(OutboxMessageStatus.DEAD_LETTER);
        message.setNextAttemptAt(null);
        
        log.error("Outbox message {} moved to DEAD_LETTER after {} retries. Topic: {}. Error: {}", 
                  message.getId(), message.getRetryAttempts(), topic, ex.getMessage(), ex);
    }

    private void scheduleRetry(OutboxMessage message, String topic, Throwable ex) {
        long delaySeconds = retryConfiguration.getBackoffDuration(message.getRetryAttempts());
        
        message.setStatus(OutboxMessageStatus.FAILED);
        message.setNextAttemptAt(Instant.now().plusSeconds(delaySeconds));
        
        log.warn("Outbox message {} failed (attempt {}/{}). Retrying in {} seconds. Topic: {}. Error: {}", 
                 message.getId(), message.getRetryAttempts(), retryConfiguration.getMaxRetries(), 
                 delaySeconds, topic, ex.getMessage());
    }

    private void handleUnknownEventType(OutboxMessage message) {
        log.error("No Kafka topic mapped for event type: {}. Message: {}", 
                  message.getEventType(), message.getId());
        updateMessageStatus(message, OutboxMessageStatus.FAILED);
    }

    private void handleUnexpectedError(OutboxMessage message, Exception e) {
        log.error("Unexpected error processing outbox message {}: {}", 
                  message.getId(), e.getMessage(), e);
        updateMessageStatus(message, OutboxMessageStatus.FAILED);
    }

    private void updateMessageStatus(OutboxMessage message, OutboxMessageStatus status) {
        message.setStatus(status);
        message.setProcessedAt(Instant.now());
        saveMessage(message);
    }

    private void saveMessage(OutboxMessage message) {
        outboxMessageRepository.save(message);
    }

    private String getTopicForEventType(String eventType) {
        String topic = eventTypeToTopicMap.get(eventType);
        if (topic == null) {
            throw new IllegalArgumentException("Unknown event type: " + eventType);
        }
        return topic;
    }

    private Map<String, String> initializeEventTopicMapping() {
        return Map.of(
            EMPLOYEE_CREATED_EVENT, "employee.created",
            EMPLOYEE_STATUS_CHANGED_EVENT, "employee.status.changed"
        );
    }

    private List<Long> parseBackoffDurations(String backoffDurationsStr) {
        return Arrays.stream(backoffDurationsStr.split(","))
            .map(String::trim)
            .map(Long::parseLong)
            .toList();
    }

    private static class RetryConfiguration {
        private final int maxRetries;
        private final List<Long> backoffDurations;

        public RetryConfiguration(int maxRetries, List<Long> backoffDurations) {
            this.maxRetries = maxRetries;
            this.backoffDurations = List.copyOf(backoffDurations);
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public long getBackoffDuration(int retryAttempt) {
            if (retryAttempt <= 0) return 0L;
            
            int index = Math.min(retryAttempt - 1, backoffDurations.size() - 1);
            return backoffDurations.get(index);
        }
    }
}

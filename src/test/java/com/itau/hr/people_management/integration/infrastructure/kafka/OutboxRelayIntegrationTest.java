package com.itau.hr.people_management.integration.infrastructure.kafka;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.itau.hr.people_management.domain.employee.enumeration.EventType;
import com.itau.hr.people_management.infrastructure.kafka.OutboxRelay;
import com.itau.hr.people_management.infrastructure.outbox.enumeration.OutboxMessageStatus;
import com.itau.hr.people_management.infrastructure.persistence.entity.OutboxMessage;
import com.itau.hr.people_management.infrastructure.persistence.repository.OutboxMessageRepository;
import com.itau.hr.people_management.integration.infrastructure.kafka.support.TestLogAppender;

import ch.qos.logback.classic.Logger;

@SpringBootTest(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.consumer.auto-offset-reset=earliest", 
    "spring.kafka.consumer.group-id=${random.uuid}",
    "spring.kafka.admin.properties.request.timeout.ms=20000",
    "spring.kafka.admin.properties.default.api.timeout.ms=20000",
    "spring.kafka.producer.retries=0",
    "spring.kafka.producer.acks=1", 
    "spring.kafka.consumer.session.timeout.ms=10000",
    "spring.kafka.consumer.heartbeat.interval.ms=3000"
})
@EmbeddedKafka(
    partitions = 1,
    controlledShutdown = false,
    topics = {"employee.created", "employee.status.changed"},
    brokerProperties = {
        "offsets.topic.replication.factor=1",
        "auto.create.topics.enable=true",
        "log.flush.interval.messages=1", 
        "log.flush.interval.ms=1"
    }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("OutboxRelay Integration Tests")
class OutboxRelayIntegrationTest {

    @MockitoBean 
    private OutboxMessageRepository outboxMessageRepository;

    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private OutboxRelay outboxRelay;

    private TestLogAppender testLogAppender;
    private Logger outboxRelayLogger;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        outboxRelayLogger = (Logger) LoggerFactory.getLogger("com.itau.hr.people_management.infrastructure.kafka.OutboxRelay");
        testLogAppender = new TestLogAppender();
        testLogAppender.start();
        outboxRelayLogger.addAppender(testLogAppender);

        when(kafkaTemplate.send(any(String.class), any(String.class), any(String.class)))
            .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));
    }

    @AfterEach
    void tearDown() {
        if (outboxRelayLogger != null && testLogAppender != null) {
            outboxRelayLogger.detachAppender(testLogAppender);
            testLogAppender.stop();
        }
        reset(outboxMessageRepository, kafkaTemplate);
    }

    @Test
    @DisplayName("Should process pending messages: Repository → Event Mapping → Kafka → Status Update")
    void shouldProcessPendingMessagesWithCompleteIntegrationChain() {
        // Arrange
        OutboxMessage pendingMessage = createTestOutboxMessage(
            OutboxMessageStatus.PENDING, 
            EventType.EMPLOYEE_CREATED_EVENT.name(),
            0
        );
        
        when(outboxMessageRepository.findByStatusInAndNextAttemptAtBeforeOrderByOccurredOnAsc(
            any(), any(), any(PageRequest.class)))
            .thenReturn(List.of(pendingMessage))
            .thenReturn(List.of());

        // Act
        outboxRelay.processOutbox();

        // Assert 
        await().atMost(4, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(outboxMessageRepository, atMost(4)).findByStatusInAndNextAttemptAtBeforeOrderByOccurredOnAsc(
                argThat(statuses -> statuses.contains(OutboxMessageStatus.PENDING) && 
                                   statuses.contains(OutboxMessageStatus.FAILED)),
                any(), any());

            verify(kafkaTemplate, times(1)).send(
                "employee.created",
                pendingMessage.getAggregateId().toString(),
                pendingMessage.getPayload()
            );

            verify(outboxMessageRepository, times(1)).save(argThat(savedMessage ->
                savedMessage.getStatus() == OutboxMessageStatus.SENT &&
                savedMessage.getProcessedAt() != null &&
                savedMessage.getNextAttemptAt() == null
            ));

            assertThat(testLogAppender.getLastInfoMessage(), 
                       containsString("Processing 1 pending outbox messages"));
        });
    }

    @Test
    @DisplayName("Should handle Kafka failure with exponential backoff retry logic")
    void shouldHandleKafkaFailureWithExponentialBackoffRetryLogic() {
        // Arrange
        OutboxMessage pendingMessage = createTestOutboxMessage(
            OutboxMessageStatus.PENDING, 
            EventType.EMPLOYEE_CREATED_EVENT.name(),
            0
        );
        
        when(outboxMessageRepository.findByStatusInAndNextAttemptAtBeforeOrderByOccurredOnAsc(
            any(), any(), any(PageRequest.class)))
            .thenReturn(List.of(pendingMessage));

        CompletableFuture<SendResult<String, String>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka broker unavailable"));
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(failedFuture);

        // Act
        outboxRelay.processOutbox();

        // Assert 
        await().atMost(4, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(outboxMessageRepository, times(1)).save(argThat(savedMessage ->
                savedMessage.getStatus() == OutboxMessageStatus.FAILED &&
                savedMessage.getRetryAttempts() == 1 &&
                savedMessage.getNextAttemptAt() != null &&
                savedMessage.getNextAttemptAt().isAfter(Instant.now().plusSeconds(1)) // 2s backoff from config
            ));

            assertThat(testLogAppender.getLogEvents().stream()
                .anyMatch(event -> event.getLevel().toString().equals("WARN") &&
                          event.getFormattedMessage().contains("failed (attempt 1/5)")),
                is(true)); 
        });
    }

    @Test
    @DisplayName("Should move message to dead letter after max retries exceeded")
    void shouldMoveMessageToDeadLetterAfterMaxRetriesExceeded() {
        // Arrange 
        OutboxMessage failedMessage = createTestOutboxMessage(
            OutboxMessageStatus.FAILED, 
            EventType.EMPLOYEE_CREATED_EVENT.name(),
            5
        );
        
        when(outboxMessageRepository.findByStatusInAndNextAttemptAtBeforeOrderByOccurredOnAsc(
            any(), any(), any()))
            .thenReturn(List.of(failedMessage));

        CompletableFuture<SendResult<String, String>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka broker unavailable"));
        when(kafkaTemplate.send(any(), any(), any())).thenReturn(failedFuture);

        // Act
        outboxRelay.processOutbox();

        // Assert
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(outboxMessageRepository, times(1)).save(argThat(savedMessage ->
                savedMessage.getStatus() == OutboxMessageStatus.DEAD_LETTER &&
                savedMessage.getRetryAttempts() == 6 &&
                savedMessage.getNextAttemptAt() == null
            ));

            assertThat(testLogAppender.getLastErrorMessage(), 
                       containsString("moved to DEAD_LETTER after 6 retries"));
        });
    }

    @Test
    @DisplayName("Should handle unknown event type and mark as failed")
    void shouldHandleUnknownEventTypeAndMarkAsFailed() {
        // Arrange
        OutboxMessage unknownEventMessage = createTestOutboxMessage(
            OutboxMessageStatus.PENDING,
            "com.unknown.EventType",
            0
        );
        
        when(outboxMessageRepository.findByStatusInAndNextAttemptAtBeforeOrderByOccurredOnAsc(
            any(), any(), any()))
            .thenReturn(List.of(unknownEventMessage));

        // Act
        outboxRelay.processOutbox();

        // Assert
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(kafkaTemplate, never()).send(any(), any(), any());

            verify(outboxMessageRepository, times(1)).save(argThat(savedMessage ->
                savedMessage.getStatus() == OutboxMessageStatus.FAILED
            ));

            assertThat(testLogAppender.getLastErrorMessage(), 
                       containsString("No Kafka topic mapped for event type: com.unknown.EventType"));
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("Should process multiple messages in batch with different outcomes")
    void shouldProcessMultipleMessagesInBatchWithDifferentOutcomes() {
        // Arrange
        OutboxMessage successMessage = createTestOutboxMessage(
            OutboxMessageStatus.PENDING, EventType.EMPLOYEE_CREATED_EVENT.name(), 0);
        OutboxMessage failMessage = createTestOutboxMessage(
            OutboxMessageStatus.PENDING, EventType.EMPLOYEE_STATUS_CHANGED_EVENT.name(), 0);
        
        when(outboxMessageRepository.findByStatusInAndNextAttemptAtBeforeOrderByOccurredOnAsc(
            any(), any(), any()))
            .thenReturn(List.of(successMessage, failMessage));

        when(kafkaTemplate.send(eq("employee.created"), any(), any()))
            .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));
        
        CompletableFuture<SendResult<String, String>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Network timeout"));
        when(kafkaTemplate.send(eq("employee.status.changed"), any(), any()))
            .thenReturn(failedFuture);

        // Act
        outboxRelay.processOutbox();

        // Assert
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(kafkaTemplate, times(2)).send(any(), any(), any());
            verify(outboxMessageRepository, times(2)).save(any());
            
            assertThat(testLogAppender.getLastInfoMessage(), 
                       containsString("Processing 2 pending outbox messages"));
        });
    }

    @Test
    @DisplayName("Should handle empty result and skip processing")
    void shouldHandleEmptyResultAndSkipProcessing() {
        // Arrange
        when(outboxMessageRepository.findByStatusInAndNextAttemptAtBeforeOrderByOccurredOnAsc(
            any(), any(), any()))
            .thenReturn(List.of());

        // Act
        outboxRelay.processOutbox();

        // Assert
        verify(kafkaTemplate, never()).send(any(), any(), any());
        verify(outboxMessageRepository, never()).save(any());
        
        assertThat(testLogAppender.getLogEvents().stream()
            .anyMatch(event -> event.getLevel().toString().equals("DEBUG") &&
                      event.getFormattedMessage().contains("No pending outbox messages to process")),
            is(true));
    }

    @Test
    @DisplayName("Should respect batch size configuration from properties")
    void shouldRespectBatchSizeConfigurationFromProperties() {
        // Arrange
        when(outboxMessageRepository.findByStatusInAndNextAttemptAtBeforeOrderByOccurredOnAsc(
            any(), any(), any()))
            .thenReturn(List.of());

        // Act
        outboxRelay.processOutbox();

        // Assert
        verify(outboxMessageRepository, atMost(4)).findByStatusInAndNextAttemptAtBeforeOrderByOccurredOnAsc(
            any(), any(), 
            argThat(pageRequest -> pageRequest.getPageSize() == 5) // From @TestPropertySource
        );
    }

    private OutboxMessage createTestOutboxMessage(OutboxMessageStatus status, String eventType, int retryAttempts) {
        UUID messageId = UUID.randomUUID();
        UUID aggregateId = UUID.randomUUID();
        
        OutboxMessage message = new OutboxMessage();
        message.setId(messageId);
        message.setAggregateId(aggregateId);
        message.setAggregateType("Employee");
        message.setEventType(eventType);
        message.setPayload("{\"employeeId\":\"" + aggregateId + "\",\"name\":\"Test Employee\"}");
        message.setStatus(status);
        message.setOccurredOn(Instant.now().minusSeconds(10));
        message.setRetryAttempts(retryAttempts);
        message.setNextAttemptAt(Instant.now().minusSeconds(1));
        
        return message;
    }
}
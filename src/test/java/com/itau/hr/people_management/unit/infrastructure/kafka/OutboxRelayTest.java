package com.itau.hr.people_management.unit.infrastructure.kafka;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import com.itau.hr.people_management.domain.employee.enumeration.EventType;
import com.itau.hr.people_management.infrastructure.kafka.OutboxRelay;
import com.itau.hr.people_management.infrastructure.outbox.enumeration.OutboxMessageStatus;
import com.itau.hr.people_management.infrastructure.persistence.entity.OutboxMessage;
import com.itau.hr.people_management.infrastructure.persistence.repository.OutboxMessageRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxRelay Unit Tests")
class OutboxRelayTest {

    @Mock
    private OutboxMessageRepository outboxMessageRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private OutboxMessage outboxMessage;

    @Mock
    private CompletableFuture<SendResult<String, String>> future;

    private OutboxRelay outboxRelay;

    private UUID aggregateId;
    private String payload;

    @BeforeEach
    void setUp() {
        outboxRelay = new OutboxRelay(outboxMessageRepository, kafkaTemplate, 5, "5,10,30,60,300");
        ReflectionTestUtils.setField(outboxRelay, "batchSize", 10);

        aggregateId = UUID.randomUUID();
        payload = "{\"eventId\":\"123\",\"employeeId\":\"456\"}";
    }

    @Nested
    @DisplayName("ProcessOutbox Tests")
    class ProcessOutboxTests {

        @Test
        @DisplayName("Should handle empty message list")
        void shouldHandleEmptyMessageList() {
            // Arrange
            when(outboxMessageRepository.findByStatusInAndNextAttemptAtBeforeOrderByOccurredOnAsc(
                any(), any(), any())).thenReturn(Collections.emptyList());

            // Act
            assertDoesNotThrow(() -> outboxRelay.processOutbox());

            // Assert
            verify(kafkaTemplate, never()).send(any(), any(), any());
        }

        @Test
        @DisplayName("Should process pending messages")
        void shouldProcessPendingMessages() {
            // Arrange
            setupOutboxMessage234(EventType.EMPLOYEE_CREATED_EVENT);
            when(outboxMessageRepository.findByStatusInAndNextAttemptAtBeforeOrderByOccurredOnAsc(
                any(), any(), any())).thenReturn(List.of(outboxMessage));
            when(kafkaTemplate.send("employee.created", aggregateId.toString(), payload))
                .thenReturn(future);

            // Act
            outboxRelay.processOutbox();

            // Assert
            verify(kafkaTemplate).send("employee.created", aggregateId.toString(), payload);
        }

        @Test
        @DisplayName("Should fetch messages with correct status filter")
        void shouldFetchMessagesWithCorrectStatusFilter() {
            // Arrange
            when(outboxMessageRepository.findByStatusInAndNextAttemptAtBeforeOrderByOccurredOnAsc(
                any(), any(), any())).thenReturn(Collections.emptyList());

            // Act
            outboxRelay.processOutbox();

            // Assert
            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<OutboxMessageStatus>> statusCaptor = ArgumentCaptor.forClass(List.class);
            verify(outboxMessageRepository).findByStatusInAndNextAttemptAtBeforeOrderByOccurredOnAsc(
                statusCaptor.capture(), any(), any());
            
            assertThat(statusCaptor.getValue(), 
                containsInAnyOrder(OutboxMessageStatus.PENDING, OutboxMessageStatus.FAILED));
        }
    }

    @Nested
    @DisplayName("Event Type Mapping Tests")
    class EventTypeMappingTests {

        @Test
        @DisplayName("Should map EmployeeCreatedEvent to correct topic")
        void shouldMapEmployeeCreatedEventToCorrectTopic() {
            // Arrange
            setupOutboxMessage234(EventType.EMPLOYEE_CREATED_EVENT);
            when(outboxMessageRepository.findByStatusInAndNextAttemptAtBeforeOrderByOccurredOnAsc(
                any(), any(), any())).thenReturn(List.of(outboxMessage));
            when(kafkaTemplate.send(eq("employee.created"), any(), any())).thenReturn(future);

            // Act
            outboxRelay.processOutbox();

            // Assert
            verify(kafkaTemplate).send("employee.created", aggregateId.toString(), payload);
        }

        @Test
        @DisplayName("Should map EmployeeStatusChangedEvent to correct topic")
        void shouldMapEmployeeStatusChangedEventToCorrectTopic() {
            // Arrange
            setupOutboxMessage234(EventType.EMPLOYEE_STATUS_CHANGED_EVENT);
            when(outboxMessageRepository.findByStatusInAndNextAttemptAtBeforeOrderByOccurredOnAsc(
                any(), any(), any())).thenReturn(List.of(outboxMessage));
            when(kafkaTemplate.send(eq("employee.status.changed"), any(), any())).thenReturn(future);

            // Act
            outboxRelay.processOutbox();

            // Assert
            verify(kafkaTemplate).send("employee.status.changed", aggregateId.toString(), payload);
        }

        @Test
        @DisplayName("Should handle unknown event type")
        void shouldHandleUnknownEventType() {
            // Arrange
            when(outboxMessage.getId()).thenReturn(UUID.randomUUID());
            when(outboxMessage.getEventType()).thenReturn("unkown.event.type");
            when(outboxMessageRepository.findByStatusInAndNextAttemptAtBeforeOrderByOccurredOnAsc(
                any(), any(), any())).thenReturn(List.of(outboxMessage));

            // Act
            outboxRelay.processOutbox();

            // Assert
            verify(outboxMessage).setStatus(OutboxMessageStatus.FAILED);
            verify(outboxMessage).setProcessedAt(any(Instant.class));
            verify(outboxMessageRepository).save(outboxMessage);
        }
    }

    @Nested
    @DisplayName("Kafka Result Handling Tests")
    class KafkaResultHandlingTests {

        @Test
        @DisplayName("Should handle successful Kafka send")
        void shouldHandleSuccessfulKafkaSend() {
            // Arrange
            setupOutboxMessage1234(EventType.EMPLOYEE_CREATED_EVENT);
            when(outboxMessageRepository.findByStatusInAndNextAttemptAtBeforeOrderByOccurredOnAsc(
                any(), any(), any())).thenReturn(List.of(outboxMessage));
            when(kafkaTemplate.send(any(), any(), any())).thenReturn(future);
            
            doAnswer(invocation -> {
                java.util.function.BiConsumer<SendResult<String, String>, Throwable> callback = invocation.getArgument(0);
                callback.accept(null, null); // Success
                return null;
            }).when(future).whenComplete(any());

            // Act
            outboxRelay.processOutbox();

            // Assert
            verify(outboxMessage).setStatus(OutboxMessageStatus.SENT);
            verify(outboxMessage).setProcessedAt(any(Instant.class));
            verify(outboxMessage).setRetryAttempts(0);
            verify(outboxMessage).setNextAttemptAt(null);
        }

        @Test
        @DisplayName("Should handle failed Kafka send with retry")
        void shouldHandleFailedKafkaSendWithRetry() {
            // Arrange
            setupOutboxMessage(EventType.EMPLOYEE_CREATED_EVENT);
            when(outboxMessage.getRetryAttempts()).thenReturn(1, 2);
            when(outboxMessageRepository.findByStatusInAndNextAttemptAtBeforeOrderByOccurredOnAsc(
                any(), any(), any())).thenReturn(List.of(outboxMessage));
            when(kafkaTemplate.send(any(), any(), any())).thenReturn(future);
            
            doAnswer(invocation -> {
                java.util.function.BiConsumer<SendResult<String, String>, Throwable> callback = invocation.getArgument(0);
                callback.accept(null, new RuntimeException("Send failed"));
                return null;
            }).when(future).whenComplete(any());

            // Act
            outboxRelay.processOutbox();

            // Assert
            verify(outboxMessage).setRetryAttempts(2);
            verify(outboxMessage).setStatus(OutboxMessageStatus.FAILED);
            verify(outboxMessage).setNextAttemptAt(any(Instant.class));
        }

        @Test
        @DisplayName("Should move to dead letter after max retries")
        void shouldMoveToDeadLetterAfterMaxRetries() {
            // Arrange
            setupOutboxMessage(EventType.EMPLOYEE_CREATED_EVENT);
            when(outboxMessage.getRetryAttempts()).thenReturn(4, 5);
            when(outboxMessageRepository.findByStatusInAndNextAttemptAtBeforeOrderByOccurredOnAsc(
                any(), any(), any())).thenReturn(List.of(outboxMessage));
            when(kafkaTemplate.send(any(), any(), any())).thenReturn(future);
            
            doAnswer(invocation -> {
                java.util.function.BiConsumer<SendResult<String, String>, Throwable> callback = invocation.getArgument(0);
                callback.accept(null, new RuntimeException("Send failed"));
                return null;
            }).when(future).whenComplete(any());

            // Act
            outboxRelay.processOutbox();

            // Assert
            verify(outboxMessage).setRetryAttempts(5);
            verify(outboxMessage).setStatus(OutboxMessageStatus.DEAD_LETTER);
            verify(outboxMessage).setNextAttemptAt(null);
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Should handle invalid backoff duration format")
        void shouldHandleInvalidBackoffDurationFormat() {
            // Act & Assert
            assertThrows(NumberFormatException.class, () -> {
                new OutboxRelay(outboxMessageRepository, kafkaTemplate, 1, "invalid,format");
            });
        }

        @Test
        @DisplayName("Should handle unexpected processing exception")
        void shouldHandleUnexpectedProcessingException() {
            // Arrange
            setupOutboxMessage1234(EventType.EMPLOYEE_CREATED_EVENT);
            when(outboxMessageRepository.findByStatusInAndNextAttemptAtBeforeOrderByOccurredOnAsc(
                any(), any(), any())).thenReturn(List.of(outboxMessage));
            when(kafkaTemplate.send(any(), any(), any())).thenThrow(new RuntimeException("Kafka error"));

            // Act
            outboxRelay.processOutbox();

            // Assert
            verify(outboxMessage).setStatus(OutboxMessageStatus.FAILED);
            verify(outboxMessage).setProcessedAt(any(Instant.class));
        }
    }

    private void setupOutboxMessage(EventType eventType) {
        when(outboxMessage.getId()).thenReturn(UUID.randomUUID());
        when(outboxMessage.getAggregateId()).thenReturn(aggregateId);
        when(outboxMessage.getPayload()).thenReturn(payload);
        when(outboxMessage.getEventType()).thenReturn(eventType.name());
        when(outboxMessage.getRetryAttempts()).thenReturn(0);
    }

    private void setupOutboxMessage234(EventType eventType) {
        when(outboxMessage.getAggregateId()).thenReturn(aggregateId);
        when(outboxMessage.getPayload()).thenReturn(payload);
        when(outboxMessage.getEventType()).thenReturn(eventType.name());
    }

     private void setupOutboxMessage1234(EventType eventType) {
        when(outboxMessage.getId()).thenReturn(UUID.randomUUID());
        when(outboxMessage.getAggregateId()).thenReturn(aggregateId);
        when(outboxMessage.getPayload()).thenReturn(payload);
        when(outboxMessage.getEventType()).thenReturn(eventType.name());
     }
}
package com.itau.hr.people_management.infrastructure.outbox.publisher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itau.hr.people_management.domain.employee.event.EmployeeCreatedEvent;
import com.itau.hr.people_management.domain.employee.event.EmployeeStatusChangedEvent;
import com.itau.hr.people_management.domain.shared.event.DomainEvent;
import com.itau.hr.people_management.infrastructure.outbox.entity.OutboxMessage;
import com.itau.hr.people_management.infrastructure.outbox.enumeration.OutboxMessageStatus;
import com.itau.hr.people_management.infrastructure.outbox.exception.OutboxEventSerializationException;
import com.itau.hr.people_management.infrastructure.outbox.repository.OutboxMessageRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("OutboxEventPublisher Unit Tests")
class OutboxEventPublisherTest {

    @Mock
    private OutboxMessageRepository outboxMessageRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EmployeeCreatedEvent employeeCreatedEvent;

    @Mock
    private EmployeeStatusChangedEvent employeeStatusChangedEvent;

    @Mock
    private DomainEvent unknownEvent;

    private OutboxEventPublisher publisher;

    private UUID eventId;
    private UUID employeeId;
    private Instant occurredOn;
    private String payload;

    @BeforeEach
    void setUp() {
        publisher = new OutboxEventPublisher(outboxMessageRepository, objectMapper);
        
        eventId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        occurredOn = Instant.now();
        payload = "{\"eventId\":\"123\",\"employeeId\":\"456\"}";
    }

    @Nested
    @DisplayName("Publish EmployeeCreatedEvent Tests")
    class PublishEmployeeCreatedEventTests {

        @Test
        @DisplayName("Should publish EmployeeCreatedEvent successfully")
        void shouldPublishEmployeeCreatedEventSuccessfully() throws JsonProcessingException {
            // Arrange
            setupEmployeeCreatedEventMocks();
            when(objectMapper.writeValueAsString(employeeCreatedEvent)).thenReturn(payload);

            // Act
            publisher.publish(employeeCreatedEvent);

            // Assert
            ArgumentCaptor<OutboxMessage> captor = ArgumentCaptor.forClass(OutboxMessage.class);
            verify(outboxMessageRepository).save(captor.capture());
            
            OutboxMessage savedMessage = captor.getValue();
            assertThat(savedMessage.getAggregateId(), is(employeeId));
            assertThat(savedMessage.getAggregateType(), is("Employee"));
            assertThat(savedMessage.getEventType(), is(EmployeeCreatedEvent.class.getName()));
            assertThat(savedMessage.getPayload(), is(payload));
            assertThat(savedMessage.getStatus(), is(OutboxMessageStatus.PENDING));
        }
    }

    @Nested
    @DisplayName("Publish EmployeeStatusChangedEvent Tests")
    class PublishEmployeeStatusChangedEventTests {

        @Test
        @DisplayName("Should publish EmployeeStatusChangedEvent successfully")
        void shouldPublishEmployeeStatusChangedEventSuccessfully() throws JsonProcessingException {
            // Arrange
            setupEmployeeStatusChangedEventMocks();
            when(objectMapper.writeValueAsString(employeeStatusChangedEvent)).thenReturn(payload);

            // Act
            publisher.publish(employeeStatusChangedEvent);

            // Assert
            ArgumentCaptor<OutboxMessage> captor = ArgumentCaptor.forClass(OutboxMessage.class);
            verify(outboxMessageRepository).save(captor.capture());
            
            OutboxMessage savedMessage = captor.getValue();
            assertThat(savedMessage.getAggregateType(), is("Employee"));
            assertThat(savedMessage.getEventType(), is(EmployeeStatusChangedEvent.class.getName()));
        }
    }

    @Nested
    @DisplayName("Publish Unknown Event Tests")
    class PublishUnknownEventTests {

        @Test
        @DisplayName("Should publish unknown event with null aggregate data")
        void shouldPublishUnknownEventWithNullAggregateData() throws JsonProcessingException {
            // Arrange
            when(unknownEvent.getEventId()).thenReturn(eventId);
            when(unknownEvent.getOccurredOn()).thenReturn(occurredOn);
            when(objectMapper.writeValueAsString(unknownEvent)).thenReturn(payload);

            // Act
            publisher.publish(unknownEvent);

            // Assert
            ArgumentCaptor<OutboxMessage> captor = ArgumentCaptor.forClass(OutboxMessage.class);
            verify(outboxMessageRepository).save(captor.capture());
            
            OutboxMessage savedMessage = captor.getValue();
            assertThat(savedMessage.getAggregateId(), is(nullValue()));
            assertThat(savedMessage.getAggregateType(), is(nullValue()));
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Should throw OutboxEventSerializationException on JSON error")
        void shouldThrowSerializationExceptionOnJsonError() throws JsonProcessingException {
            // Arrange
            setupEmployeeCreatedEventMocks();
            JsonProcessingException jsonException = mock(JsonProcessingException.class);
            when(objectMapper.writeValueAsString(employeeCreatedEvent)).thenThrow(jsonException);

            // Act & Assert
            OutboxEventSerializationException exception = assertThrows(
                OutboxEventSerializationException.class,
                () -> publisher.publish(employeeCreatedEvent)
            );

            assertThat(exception.getCause(), is(sameInstance(jsonException)));
        }

        @Test
        @DisplayName("Should handle repository exception gracefully")
        void shouldHandleRepositoryExceptionGracefully() throws JsonProcessingException {
            // Arrange
            setupEmployeeCreatedEventMocks();
            when(objectMapper.writeValueAsString(employeeCreatedEvent)).thenReturn(payload);
            doThrow(new RuntimeException("Database error")).when(outboxMessageRepository).save(any());

            // Act - Não deve propagar exceção
            assertDoesNotThrow(() -> publisher.publish(employeeCreatedEvent));
        }
    }

    private void setupEmployeeCreatedEventMocks() {
        when(employeeCreatedEvent.getEventId()).thenReturn(eventId);
        when(employeeCreatedEvent.employeeId()).thenReturn(employeeId);
    }

    private void setupEmployeeStatusChangedEventMocks() {
        when(employeeStatusChangedEvent.getEventId()).thenReturn(eventId);
        when(employeeStatusChangedEvent.getOccurredOn()).thenReturn(occurredOn);
        when(employeeStatusChangedEvent.employeeId()).thenReturn(employeeId);
    }
}
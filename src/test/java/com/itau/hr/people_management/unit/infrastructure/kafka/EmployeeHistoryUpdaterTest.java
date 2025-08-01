package com.itau.hr.people_management.unit.infrastructure.kafka;

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
import com.itau.hr.people_management.domain.employee.enumeration.EmployeeStatus;
import com.itau.hr.people_management.domain.employee.enumeration.EventType;
import com.itau.hr.people_management.domain.employee.event.EmployeeCreatedEvent;
import com.itau.hr.people_management.domain.employee.event.EmployeeStatusChangedEvent;
import com.itau.hr.people_management.domain.employee.history.EmployeeEvent;
import com.itau.hr.people_management.domain.employee.repository.EmployeeEventRepository;
import com.itau.hr.people_management.infrastructure.kafka.EmployeeHistoryUpdater;
import com.itau.hr.people_management.infrastructure.kafka.exception.EmployeeEventDeserializationException;
import com.itau.hr.people_management.infrastructure.kafka.exception.EmployeeEventProcessingException;
import com.itau.hr.people_management.infrastructure.kafka.exception.EmployeeHistoryEventSaveException;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeHistoryUpdater Unit Tests")
class EmployeeHistoryUpdaterTest {

    @Mock
    private EmployeeEventRepository employeeEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EmployeeCreatedEvent employeeCreatedEvent;

    @Mock
    private EmployeeStatusChangedEvent employeeStatusChangedEvent;

    private EmployeeHistoryUpdater historyUpdater;

    private String validJsonMessage;
    private UUID eventId;
    private UUID employeeId;
    private Instant occurredOn;

    @BeforeEach
    void setUp() {
        historyUpdater = new EmployeeHistoryUpdater(employeeEventRepository, objectMapper);
        
        eventId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        occurredOn = Instant.now();
        validJsonMessage = "{\"eventId\":\"123\",\"employeeId\":\"456\"}";
    }

    @Nested
    @DisplayName("HandleEmployeeCreatedEvent Tests")
    class HandleEmployeeCreatedEventTests {

        @Test
        @DisplayName("Should process valid EmployeeCreatedEvent message")
        void shouldProcessValidEmployeeCreatedEventMessage() throws JsonProcessingException {
            // Arrange
            setupEmployeeCreatedEventMocks();
            when(objectMapper.readValue(validJsonMessage, EmployeeCreatedEvent.class))
                .thenReturn(employeeCreatedEvent);

            // Act
            assertDoesNotThrow(() -> historyUpdater.handleEmployeeCreatedEvent(validJsonMessage));

            // Assert
            verify(objectMapper).readValue(validJsonMessage, EmployeeCreatedEvent.class);
            verify(employeeEventRepository).save(any(EmployeeEvent.class));
        }

        @Test
        @DisplayName("Should throw EmployeeEventDeserializationException for invalid JSON")
        void shouldThrowDeserializationExceptionForInvalidJson() throws JsonProcessingException {
            // Arrange
            String invalidJson = "{invalid json";
            JsonProcessingException jsonException = mock(JsonProcessingException.class);
            when(objectMapper.readValue(invalidJson, EmployeeCreatedEvent.class))
                .thenThrow(jsonException);

            // Act & Assert
            EmployeeEventDeserializationException exception = assertThrows(
                EmployeeEventDeserializationException.class,
                () -> historyUpdater.handleEmployeeCreatedEvent(invalidJson)
            );

            assertThat(exception.getMessage(), containsString("Failed to deserialize EMPLOYEE_CREATED_EVENT"));
            assertThat(exception.getCause(), is(sameInstance(jsonException)));
        }

        @Test
        @DisplayName("Should throw EmployeeEventProcessingException for null message")
        void shouldThrowProcessingExceptionForNullMessage() {
            // Act & Assert
            EmployeeEventProcessingException exception = assertThrows(
                EmployeeEventProcessingException.class,
                () -> historyUpdater.handleEmployeeCreatedEvent(null)
            );

            assertThat(exception.getMessage(), containsString("Failed to process EMPLOYEE_CREATED_EVENT"));
            assertThat(exception.getCause(), is(instanceOf(IllegalArgumentException.class)));
        }

        @Test
        @DisplayName("Should create history event with correct data")
        void shouldCreateHistoryEventWithCorrectData() throws JsonProcessingException {
            // Arrange
            setupEmployeeCreatedEventMocks();
            when(objectMapper.readValue(validJsonMessage, EmployeeCreatedEvent.class))
                .thenReturn(employeeCreatedEvent);

            ArgumentCaptor<EmployeeEvent> historyEventCaptor = ArgumentCaptor.forClass(EmployeeEvent.class);

            // Act
            historyUpdater.handleEmployeeCreatedEvent(validJsonMessage);

            // Assert
            verify(employeeEventRepository).save(historyEventCaptor.capture());
            EmployeeEvent savedEvent = historyEventCaptor.getValue();
            
            assertThat(savedEvent.getId(), is(eventId));
            assertThat(savedEvent.getEmployeeId(), is(employeeId));
            assertThat(savedEvent.getEventType(), is(EventType.EMPLOYEE_CREATED_EVENT));
            assertThat(savedEvent.getEventData(), is(validJsonMessage));
        }

        @Test
        @DisplayName("Should handle null deserialized event")
        void shouldHandleNullDeserializedEvent() throws JsonProcessingException {
            // Arrange
            when(objectMapper.readValue(validJsonMessage, EmployeeCreatedEvent.class))
                .thenReturn(null);

            // Act & Assert
            EmployeeEventProcessingException exception = assertThrows(
                EmployeeEventProcessingException.class,
                () -> historyUpdater.handleEmployeeCreatedEvent(validJsonMessage)
            );

            assertThat(exception.getCause(), is(instanceOf(IllegalArgumentException.class)));
        }
    }

    @Nested
    @DisplayName("HandleEmployeeStatusChangedEvent Tests")
    class HandleEmployeeStatusChangedEventTests {

        @Test
        @DisplayName("Should process valid EmployeeStatusChangedEvent message")
        void shouldProcessValidEmployeeStatusChangedEventMessage() throws JsonProcessingException {
            // Arrange
            setupEmployeeStatusChangedEventMocks();
            when(objectMapper.readValue(validJsonMessage, EmployeeStatusChangedEvent.class))
                .thenReturn(employeeStatusChangedEvent);

            // Act
            assertDoesNotThrow(() -> historyUpdater.handleEmployeeStatusChangedEvent(validJsonMessage));

            // Assert
            verify(objectMapper).readValue(validJsonMessage, EmployeeStatusChangedEvent.class);
            verify(employeeEventRepository).save(any(EmployeeEvent.class));
        }

        @Test
        @DisplayName("Should create history event with correct status data")
        void shouldCreateHistoryEventWithCorrectStatusData() throws JsonProcessingException {
            // Arrange
            setupEmployeeStatusChangedEventMocks();
            when(objectMapper.readValue(validJsonMessage, EmployeeStatusChangedEvent.class))
                .thenReturn(employeeStatusChangedEvent);

            ArgumentCaptor<EmployeeEvent> historyEventCaptor = ArgumentCaptor.forClass(EmployeeEvent.class);

            // Act
            historyUpdater.handleEmployeeStatusChangedEvent(validJsonMessage);

            // Assert
            verify(employeeEventRepository).save(historyEventCaptor.capture());
            EmployeeEvent savedEvent = historyEventCaptor.getValue();
            
            assertThat(savedEvent.getEventType(), is(EventType.EMPLOYEE_STATUS_CHANGED_EVENT));
            assertThat(savedEvent.getDescription(), containsString("ACTIVE"));
            assertThat(savedEvent.getDescription(), containsString("TERMINATED"));
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Should throw EmployeeHistoryEventSaveException when repository save fails")
        void shouldThrowSaveExceptionWhenRepositorySaveFails() throws JsonProcessingException {
            // Arrange
            setupEmployeeCreatedEventMocks();
            when(objectMapper.readValue(validJsonMessage, EmployeeCreatedEvent.class))
                .thenReturn(employeeCreatedEvent);
            
            RuntimeException repositoryException = new RuntimeException("Database error");
            doThrow(repositoryException).when(employeeEventRepository).save(any(EmployeeEvent.class));

            // Act & Assert
            Throwable exception = assertThrows(
                EmployeeHistoryEventSaveException.class,
                () -> historyUpdater.handleEmployeeCreatedEvent(validJsonMessage)
            ).getCause();

            assertThat(exception.getMessage(), is("Failed to save employee history event"));
            assertThat(exception.getCause(), is(sameInstance(repositoryException)));
        }

        @Test
        @DisplayName("Should handle unexpected runtime exceptions")
        void shouldHandleUnexpectedRuntimeExceptions() throws JsonProcessingException {
            // Arrange
            when(objectMapper.readValue(validJsonMessage, EmployeeCreatedEvent.class))
                .thenThrow(new RuntimeException("Unexpected error"));

            // Act & Assert
            EmployeeEventProcessingException exception = assertThrows(
                EmployeeEventProcessingException.class,
                () -> historyUpdater.handleEmployeeCreatedEvent(validJsonMessage)
            );

            assertThat(exception.getMessage(), containsString("Failed to process EMPLOYEE_CREATED_EVENT"));
        }
    }

    private void setupEmployeeCreatedEventMocks() {
        when(employeeCreatedEvent.eventId()).thenReturn(eventId);
        when(employeeCreatedEvent.employeeId()).thenReturn(employeeId);
        when(employeeCreatedEvent.employeeName()).thenReturn("John Doe");
        when(employeeCreatedEvent.employeeEmail()).thenReturn("john.doe@example.com");
        when(employeeCreatedEvent.occurredOn()).thenReturn(occurredOn);
    }

    private void setupEmployeeStatusChangedEventMocks() {
        when(employeeStatusChangedEvent.eventId()).thenReturn(eventId);
        when(employeeStatusChangedEvent.employeeId()).thenReturn(employeeId);
        when(employeeStatusChangedEvent.oldStatus()).thenReturn(EmployeeStatus.ACTIVE);
        when(employeeStatusChangedEvent.newStatus()).thenReturn(EmployeeStatus.TERMINATED);
        when(employeeStatusChangedEvent.occurredOn()).thenReturn(occurredOn);
    }
}
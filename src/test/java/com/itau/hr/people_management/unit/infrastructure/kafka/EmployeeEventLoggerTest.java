package com.itau.hr.people_management.unit.infrastructure.kafka;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itau.hr.people_management.domain.employee.enumeration.EmployeeStatus;
import com.itau.hr.people_management.domain.employee.event.EmployeeCreatedEvent;
import com.itau.hr.people_management.domain.employee.event.EmployeeStatusChangedEvent;
import com.itau.hr.people_management.infrastructure.kafka.EmployeeEventLogger;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeEventLogger Unit Tests")
class EmployeeEventLoggerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EmployeeCreatedEvent employeeCreatedEvent;

    @Mock
    private EmployeeStatusChangedEvent employeeStatusChangedEvent;

    private EmployeeEventLogger eventLogger;

    private String jsonMessage;

    @BeforeEach
    void setUp() {
        eventLogger = new EmployeeEventLogger(objectMapper);
        jsonMessage = "{\"eventId\":\"123\",\"employeeId\":\"456\"}";
    }

    @Nested
    @DisplayName("ListenEmployeeCreated Tests")
    class ListenEmployeeCreatedTests {

        @Test
        @DisplayName("Should process valid EmployeeCreatedEvent message")
        void shouldProcessValidEmployeeCreatedEventMessage() throws JsonProcessingException {
            // Arrange
            when(objectMapper.readValue(jsonMessage, EmployeeCreatedEvent.class))
                .thenReturn(employeeCreatedEvent);
            setupEmployeeCreatedEventMocks();

            // Act
            assertDoesNotThrow(() -> eventLogger.listenEmployeeCreated(jsonMessage));

            // Assert
            verify(objectMapper).readValue(jsonMessage, EmployeeCreatedEvent.class);
            verify(employeeCreatedEvent).eventId();
            verify(employeeCreatedEvent).employeeId();
            verify(employeeCreatedEvent).employeeName();
            verify(employeeCreatedEvent).employeeEmail();
            verify(employeeCreatedEvent).occurredOn();
        }

        @Test
        @DisplayName("Should handle JsonProcessingException gracefully")
        void shouldHandleJsonProcessingExceptionGracefully() throws JsonProcessingException {
            // Arrange
            when(objectMapper.readValue(jsonMessage, EmployeeCreatedEvent.class))
                .thenThrow(new JsonProcessingException("Parse error") {});

            // Act
            assertDoesNotThrow(() -> eventLogger.listenEmployeeCreated(jsonMessage));

            // Assert
            verify(objectMapper).readValue(jsonMessage, EmployeeCreatedEvent.class);
        }

        @Test
        @DisplayName("Should handle null event gracefully")
        void shouldHandleNullEventGracefully() throws JsonProcessingException {
            // Arrange
            when(objectMapper.readValue(jsonMessage, EmployeeCreatedEvent.class))
                .thenReturn(null);

            // Act
            assertDoesNotThrow(() -> eventLogger.listenEmployeeCreated(jsonMessage));

            // Assert
            verify(objectMapper).readValue(jsonMessage, EmployeeCreatedEvent.class);
        }

        @Test
        @DisplayName("Should handle runtime exception gracefully")
        void shouldHandleRuntimeExceptionGracefully() throws JsonProcessingException {
            // Arrange
            when(objectMapper.readValue(jsonMessage, EmployeeCreatedEvent.class))
                .thenThrow(new RuntimeException("Unexpected error"));

            // Act
            assertDoesNotThrow(() -> eventLogger.listenEmployeeCreated(jsonMessage));
        }
    }

    @Nested
    @DisplayName("ListenEmployeeStatusChanged Tests")
    class ListenEmployeeStatusChangedTests {

        @Test
        @DisplayName("Should process valid EmployeeStatusChangedEvent message")
        void shouldProcessValidEmployeeStatusChangedEventMessage() throws JsonProcessingException {
            // Arrange
            when(objectMapper.readValue(jsonMessage, EmployeeStatusChangedEvent.class))
                .thenReturn(employeeStatusChangedEvent);
            setupEmployeeStatusChangedEventMocks();

            // Act
            assertDoesNotThrow(() -> eventLogger.listenEmployeeStatusChanged(jsonMessage));

            // Assert
            verify(objectMapper).readValue(jsonMessage, EmployeeStatusChangedEvent.class);
            verify(employeeStatusChangedEvent).eventId();
            verify(employeeStatusChangedEvent).employeeId();
            verify(employeeStatusChangedEvent).oldStatus();
            verify(employeeStatusChangedEvent).newStatus();
            verify(employeeStatusChangedEvent).occurredOn();
        }

        @Test
        @DisplayName("Should handle JsonProcessingException gracefully")
        void shouldHandleJsonProcessingExceptionGracefully() throws JsonProcessingException {
            // Arrange
            when(objectMapper.readValue(jsonMessage, EmployeeStatusChangedEvent.class))
                .thenThrow(new JsonProcessingException("Parse error") {});

            // Act
            assertDoesNotThrow(() -> eventLogger.listenEmployeeStatusChanged(jsonMessage));

            // Assert
            verify(objectMapper).readValue(jsonMessage, EmployeeStatusChangedEvent.class);
        }

        @Test
        @DisplayName("Should handle null event gracefully")
        void shouldHandleNullEventGracefully() throws JsonProcessingException {
            // Arrange
            when(objectMapper.readValue(jsonMessage, EmployeeStatusChangedEvent.class))
                .thenReturn(null);

            // Act
            assertDoesNotThrow(() -> eventLogger.listenEmployeeStatusChanged(jsonMessage));

            // Assert
            verify(objectMapper).readValue(jsonMessage, EmployeeStatusChangedEvent.class);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null message string")
        void shouldHandleNullMessageString() throws JsonProcessingException {
            // Arrange
            when(objectMapper.readValue((String) isNull(), eq(EmployeeCreatedEvent.class)))
                .thenThrow(new JsonProcessingException("Parse error") {});

            // Act
            assertDoesNotThrow(() -> eventLogger.listenEmployeeCreated(null));

            // Assert
            verify(objectMapper).readValue((String) isNull(), eq(EmployeeCreatedEvent.class));
        }

        @Test
        @DisplayName("Should handle exception in event processing")
        void shouldHandleExceptionInEventProcessing() throws JsonProcessingException {
            // Arrange
            when(objectMapper.readValue(jsonMessage, EmployeeCreatedEvent.class))
                .thenReturn(employeeCreatedEvent);
            when(employeeCreatedEvent.eventId()).thenThrow(new RuntimeException("Event processing error"));

            // Act
            assertDoesNotThrow(() -> eventLogger.listenEmployeeCreated(jsonMessage));

            // Assert
            verify(employeeCreatedEvent).eventId();
        }
    }

    private void setupEmployeeCreatedEventMocks() {
        when(employeeCreatedEvent.eventId()).thenReturn(UUID.randomUUID());
        when(employeeCreatedEvent.employeeId()).thenReturn(UUID.randomUUID());
        when(employeeCreatedEvent.employeeName()).thenReturn("John Doe");
        when(employeeCreatedEvent.employeeEmail()).thenReturn("john.doe@example.com");
        when(employeeCreatedEvent.occurredOn()).thenReturn(Instant.now());
    }

    private void setupEmployeeStatusChangedEventMocks() {
        when(employeeStatusChangedEvent.eventId()).thenReturn(UUID.randomUUID());
        when(employeeStatusChangedEvent.employeeId()).thenReturn(UUID.randomUUID());
        when(employeeStatusChangedEvent.oldStatus()).thenReturn(EmployeeStatus.ACTIVE);
        when(employeeStatusChangedEvent.newStatus()).thenReturn(EmployeeStatus.TERMINATED);
        when(employeeStatusChangedEvent.occurredOn()).thenReturn(Instant.now());
    }
}
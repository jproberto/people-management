package com.itau.hr.people_management.unit.infrastructure.employee.repository;

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

import com.itau.hr.people_management.domain.employee.enumeration.EventType;
import com.itau.hr.people_management.domain.employee.history.EmployeeEvent;
import com.itau.hr.people_management.infrastructure.employee.entity.EmployeeEventJpaEntity;
import com.itau.hr.people_management.infrastructure.employee.repository.EmployeeEventRepositoryImpl;
import com.itau.hr.people_management.infrastructure.employee.repository.JpaEmployeeEventRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeEventRepositoryImpl Unit Tests")
class EmployeeEventRepositoryImplTest {

    @Mock
    private JpaEmployeeEventRepository jpaRepository;

    @Mock
    private EmployeeEvent employeeEvent;

    private EmployeeEventRepositoryImpl repository;

    private UUID eventId;
    private UUID employeeId;
    private EventType eventType;
    private Instant occurredOn;
    private String description;
    private String eventData;

    @BeforeEach
    void setUp() {
        repository = new EmployeeEventRepositoryImpl(jpaRepository);
        
        eventId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        eventType = EventType.EMPLOYEE_CREATED_EVENT;
        occurredOn = Instant.now();
        description = "Employee created successfully";
        eventData = "{\"name\":\"John Doe\",\"email\":\"john@example.com\"}";
    }

    @Nested
    @DisplayName("Save Method Tests")
    class SaveMethodTests {

        @Test
        @DisplayName("Should save employee event with all fields mapped correctly")
        void shouldSaveEmployeeEventWithAllFieldsMappedCorrectly() {
            // Arrange
            setupEmployeeEventMocks();

            // Act
            repository.save(employeeEvent);

            // Assert
            ArgumentCaptor<EmployeeEventJpaEntity> entityCaptor = ArgumentCaptor.forClass(EmployeeEventJpaEntity.class);
            verify(jpaRepository).save(entityCaptor.capture());
            
            EmployeeEventJpaEntity capturedEntity = entityCaptor.getValue();
            assertThat(capturedEntity.getId(), is(eventId));
            assertThat(capturedEntity.getEmployeeId(), is(employeeId));
            assertThat(capturedEntity.getEventType(), is(eventType));
            assertThat(capturedEntity.getOccurredOn(), is(occurredOn));
            assertThat(capturedEntity.getDescription(), is(description));
            assertThat(capturedEntity.getEventData(), is(eventData));
        }

        @Test
        @DisplayName("Should handle null values in employee event")
        void shouldHandleNullValuesInEmployeeEvent() {
            // Arrange
            when(employeeEvent.getId()).thenReturn(null);
            when(employeeEvent.getEmployeeId()).thenReturn(null);
            when(employeeEvent.getEventType()).thenReturn(null);
            when(employeeEvent.getOccurredOn()).thenReturn(null);
            when(employeeEvent.getDescription()).thenReturn(null);
            when(employeeEvent.getEventData()).thenReturn(null);

            // Act
            repository.save(employeeEvent);

            // Assert
            ArgumentCaptor<EmployeeEventJpaEntity> entityCaptor = ArgumentCaptor.forClass(EmployeeEventJpaEntity.class);
            verify(jpaRepository).save(entityCaptor.capture());
            
            EmployeeEventJpaEntity capturedEntity = entityCaptor.getValue();
            assertThat(capturedEntity.getId(), is(nullValue()));
            assertThat(capturedEntity.getEmployeeId(), is(nullValue()));
            assertThat(capturedEntity.getEventType(), is(nullValue()));
            assertThat(capturedEntity.getOccurredOn(), is(nullValue()));
            assertThat(capturedEntity.getDescription(), is(nullValue()));
            assertThat(capturedEntity.getEventData(), is(nullValue()));
        }

        @Test
        @DisplayName("Should handle empty string values")
        void shouldHandleEmptyStringValues() {
            // Arrange
            when(employeeEvent.getId()).thenReturn(eventId);
            when(employeeEvent.getEmployeeId()).thenReturn(employeeId);
            when(employeeEvent.getEventType()).thenReturn(eventType);
            when(employeeEvent.getOccurredOn()).thenReturn(occurredOn);
            when(employeeEvent.getDescription()).thenReturn("");
            when(employeeEvent.getEventData()).thenReturn("");

            // Act
            repository.save(employeeEvent);

            // Assert
            ArgumentCaptor<EmployeeEventJpaEntity> entityCaptor = ArgumentCaptor.forClass(EmployeeEventJpaEntity.class);
            verify(jpaRepository).save(entityCaptor.capture());
            
            EmployeeEventJpaEntity capturedEntity = entityCaptor.getValue();
            assertThat(capturedEntity.getDescription(), is(""));
            assertThat(capturedEntity.getEventData(), is(""));
        }
    }

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Should propagate JPA repository exceptions")
        void shouldPropagateJpaRepositoryExceptions() {
            // Arrange
            setupEmployeeEventMocks();
            RuntimeException jpaException = new RuntimeException("Database error");
            when(jpaRepository.save(any(EmployeeEventJpaEntity.class))).thenThrow(jpaException);

            // Act & Assert
            RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                repository.save(employeeEvent);
            });

            assertThat(thrownException, is(sameInstance(jpaException)));
        }

        @Test
        @DisplayName("Should propagate employee event getter exceptions")
        void shouldPropagateEmployeeEventGetterExceptions() {
            // Arrange
            RuntimeException getterException = new RuntimeException("Getter error");
            when(employeeEvent.getId()).thenThrow(getterException);

            // Act & Assert
            RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                repository.save(employeeEvent);
            });

            assertThat(thrownException, is(sameInstance(getterException)));
            verify(jpaRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should handle null employee event parameter")
        void shouldHandleNullEmployeeEventParameter() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> {
                repository.save(null);
            });

            verify(jpaRepository, never()).save(any());
        }
    }

    private void setupEmployeeEventMocks() {
        when(employeeEvent.getId()).thenReturn(eventId);
        when(employeeEvent.getEmployeeId()).thenReturn(employeeId);
        when(employeeEvent.getEventType()).thenReturn(eventType);
        when(employeeEvent.getOccurredOn()).thenReturn(occurredOn);
        when(employeeEvent.getDescription()).thenReturn(description);
        when(employeeEvent.getEventData()).thenReturn(eventData);
    }
}
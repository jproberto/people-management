package com.itau.hr.people_management.unit.domain.employee.event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.itau.hr.people_management.domain.employee.enumeration.EventType;
import com.itau.hr.people_management.domain.employee.event.EmployeeCreatedEvent;
import com.itau.hr.people_management.domain.shared.event.DomainEvent;

@DisplayName("EmployeeCreatedEvent Domain Event Tests")
class EmployeeCreatedEventTest {

    private UUID validEmployeeId;
    private String validEmployeeName;
    private String validEmployeeEmail;
    private UUID validEventId;
    private Instant validOccurredOn;

    @BeforeEach
    void setUp() {
        validEmployeeId = UUID.randomUUID();
        validEmployeeName = "John Doe";
        validEmployeeEmail = "john.doe@example.com";
        validEventId = UUID.randomUUID();
        validOccurredOn = Instant.now();
    }

    @Test
    @DisplayName("Should create event with full constructor")
    void shouldCreateEventWithFullConstructor() {
        // Act
        EmployeeCreatedEvent event = new EmployeeCreatedEvent(
            validEventId,
            validOccurredOn,
            EventType.EMPLOYEE_CREATED_EVENT,
            validEmployeeId,
            validEmployeeName,
            validEmployeeEmail
        );

        // Assert
        assertThat(event.eventId(), is(validEventId));
        assertThat(event.occurredOn(), is(validOccurredOn));
        assertThat(event.eventType(), is(EventType.EMPLOYEE_CREATED_EVENT));
        assertThat(event.employeeId(), is(validEmployeeId));
        assertThat(event.employeeName(), is(validEmployeeName));
        assertThat(event.employeeEmail(), is(validEmployeeEmail));
    }

    @Test
    @DisplayName("Should create event with convenience constructor")
    void shouldCreateEventWithConvenienceConstructor() {
        // Arrange
        Instant beforeCreation = Instant.now();

        // Act
        EmployeeCreatedEvent event = new EmployeeCreatedEvent(
            validEmployeeId,
            validEmployeeName,
            validEmployeeEmail
        );

        // Assert
        assertThat(event.eventId(), is(notNullValue()));
        assertThat(event.occurredOn(), is(greaterThanOrEqualTo(beforeCreation)));
        assertThat(event.eventType(), is(EventType.EMPLOYEE_CREATED_EVENT));
        assertThat(event.employeeId(), is(validEmployeeId));
        assertThat(event.employeeName(), is(validEmployeeName));
        assertThat(event.employeeEmail(), is(validEmployeeEmail));
    }

    @Test
    @DisplayName("Should generate unique event IDs for convenience constructor")
    void shouldGenerateUniqueEventIdsForConvenienceConstructor() {
        // Act
        EmployeeCreatedEvent event1 = new EmployeeCreatedEvent(validEmployeeId, validEmployeeName, validEmployeeEmail);
        EmployeeCreatedEvent event2 = new EmployeeCreatedEvent(validEmployeeId, validEmployeeName, validEmployeeEmail);

        // Assert
        assertThat(event1.eventId(), is(not(equalTo(event2.eventId()))));
    }

    @Test
    @DisplayName("Should implement DomainEvent interface")
    void shouldImplementDomainEventInterface() {
        // Arrange
        EmployeeCreatedEvent event = new EmployeeCreatedEvent(validEmployeeId, validEmployeeName, validEmployeeEmail);

        // Act & Assert
        assertThat(event, is(instanceOf(DomainEvent.class)));
        
        DomainEvent domainEvent = event;
        assertThat(domainEvent.getEventId(), is(event.eventId()));
        assertThat(domainEvent.getOccurredOn(), is(event.occurredOn()));
        assertThat(domainEvent.getEventType(), is(event.eventType()));
    }

    @Test
    @DisplayName("Should handle null values without throwing exceptions")
    void shouldHandleNullValuesWithoutThrowingExceptions() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            EmployeeCreatedEvent event = new EmployeeCreatedEvent(
                null, null, null, null, null, null
            );
            
            assertThat(event.eventId(), is(nullValue()));
            assertThat(event.occurredOn(), is(nullValue()));
            assertThat(event.eventType(), is(nullValue()));
            assertThat(event.employeeId(), is(nullValue()));
            assertThat(event.employeeName(), is(nullValue()));
            assertThat(event.employeeEmail(), is(nullValue()));
        });
    }

    @Test
    @DisplayName("Should be equal when all fields are the same")
    void shouldBeEqualWhenAllFieldsAreTheSame() {
        // Arrange
        EmployeeCreatedEvent event1 = new EmployeeCreatedEvent(
            validEventId, validOccurredOn, EventType.EMPLOYEE_CREATED_EVENT,
            validEmployeeId, validEmployeeName, validEmployeeEmail);
        
        EmployeeCreatedEvent event2 = new EmployeeCreatedEvent(
            validEventId, validOccurredOn, EventType.EMPLOYEE_CREATED_EVENT,
            validEmployeeId, validEmployeeName, validEmployeeEmail);

        // Act & Assert
        assertThat(event1, is(equalTo(event2)));
        assertThat(event1.hashCode(), is(equalTo(event2.hashCode())));
    }

    @Test
    @DisplayName("Should not be equal when any field differs")
    void shouldNotBeEqualWhenAnyFieldDiffers() {
        // Arrange
        EmployeeCreatedEvent originalEvent = new EmployeeCreatedEvent(
            validEventId, validOccurredOn, EventType.EMPLOYEE_CREATED_EVENT,
            validEmployeeId, validEmployeeName, validEmployeeEmail);

        EmployeeCreatedEvent differentEvent = new EmployeeCreatedEvent(
            UUID.randomUUID(), validOccurredOn, EventType.EMPLOYEE_CREATED_EVENT,
            validEmployeeId, validEmployeeName, validEmployeeEmail);

        // Act & Assert
        assertThat(originalEvent, is(not(equalTo(differentEvent))));
    }

    @Test
    @DisplayName("Should include key information in toString")
    void shouldIncludeKeyInformationInToString() {
        // Arrange
        EmployeeCreatedEvent event = new EmployeeCreatedEvent(
            validEventId, validOccurredOn, EventType.EMPLOYEE_CREATED_EVENT,
            validEmployeeId, validEmployeeName, validEmployeeEmail);

        // Act
        String result = event.toString();

        // Assert
        assertThat(result, containsString("EmployeeCreatedEvent"));
        assertThat(result, containsString(validEventId.toString()));
        assertThat(result, containsString(validEmployeeId.toString()));
        assertThat(result, containsString(validEmployeeName));
        assertThat(result, containsString(validEmployeeEmail));
    }
}
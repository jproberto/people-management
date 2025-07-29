package com.itau.hr.people_management.domain.employee.event;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import com.itau.hr.people_management.domain.employee.enumeration.EventType;
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

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

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
            assertThat(event, is(notNullValue()));
            assertThat(event.eventId(), is(equalTo(validEventId)));
            assertThat(event.occurredOn(), is(equalTo(validOccurredOn)));
            assertThat(event.eventType(), is(equalTo(EventType.EMPLOYEE_CREATED_EVENT)));
            assertThat(event.employeeId(), is(equalTo(validEmployeeId)));
            assertThat(event.employeeName(), is(equalTo(validEmployeeName)));
            assertThat(event.employeeEmail(), is(equalTo(validEmployeeEmail)));
        }

        @Test
        @DisplayName("Should create event with convenience constructor")
        void shouldCreateEventWithConvenienceConstructor() {
            // Arrange
            Instant beforeCreation = Instant.now().minus(1, ChronoUnit.SECONDS);

            // Act
            EmployeeCreatedEvent event = new EmployeeCreatedEvent(
                validEmployeeId,
                validEmployeeName,
                validEmployeeEmail
            );

            // Assert
            assertThat(event, is(notNullValue()));
            assertThat(event.eventId(), is(notNullValue()));
            assertThat(event.occurredOn(), is(notNullValue()));
            assertThat(event.occurredOn(), is(greaterThanOrEqualTo(beforeCreation)));
            assertThat(event.occurredOn(), is(lessThanOrEqualTo(Instant.now())));
            assertThat(event.eventType(), is(equalTo(EventType.EMPLOYEE_CREATED_EVENT)));
            assertThat(event.employeeId(), is(equalTo(validEmployeeId)));
            assertThat(event.employeeName(), is(equalTo(validEmployeeName)));
            assertThat(event.employeeEmail(), is(equalTo(validEmployeeEmail)));
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
        @DisplayName("Should generate occurred time close to now for convenience constructor")
        void shouldGenerateOccurredTimeCloseToNowForConvenienceConstructor() {
            // Arrange
            Instant beforeCreation = Instant.now();

            // Act
            EmployeeCreatedEvent event = new EmployeeCreatedEvent(validEmployeeId, validEmployeeName, validEmployeeEmail);

            // Assert
            Instant afterCreation = Instant.now();
            assertThat(event.occurredOn(), is(greaterThanOrEqualTo(beforeCreation)));
            assertThat(event.occurredOn(), is(lessThanOrEqualTo(afterCreation)));
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        private EmployeeCreatedEvent event;

        @BeforeEach
        void setUp() {
            event = new EmployeeCreatedEvent(
                validEventId,
                validOccurredOn,
                EventType.EMPLOYEE_CREATED_EVENT,
                validEmployeeId,
                validEmployeeName,
                validEmployeeEmail
            );
        }

        @Test
        @DisplayName("Should return correct event ID")
        void shouldReturnCorrectEventId() {
            // Act & Assert
            assertThat(event.eventId(), is(equalTo(validEventId)));
        }

        @Test
        @DisplayName("Should return correct occurred on timestamp")
        void shouldReturnCorrectOccurredOnTimestamp() {
            // Act & Assert
            assertThat(event.occurredOn(), is(equalTo(validOccurredOn)));
        }

        @Test
        @DisplayName("Should return correct event type")
        void shouldReturnCorrectEventType() {
            // Act & Assert
            assertThat(event.eventType(), is(equalTo(EventType.EMPLOYEE_CREATED_EVENT)));
        }

        @Test
        @DisplayName("Should return correct employee ID")
        void shouldReturnCorrectEmployeeId() {
            // Act & Assert
            assertThat(event.employeeId(), is(equalTo(validEmployeeId)));
        }

        @Test
        @DisplayName("Should return correct employee name")
        void shouldReturnCorrectEmployeeName() {
            // Act & Assert
            assertThat(event.employeeName(), is(equalTo(validEmployeeName)));
        }

        @Test
        @DisplayName("Should return correct employee email")
        void shouldReturnCorrectEmployeeEmail() {
            // Act & Assert
            assertThat(event.employeeEmail(), is(equalTo(validEmployeeEmail)));
        }
    }

    @Nested
    @DisplayName("DomainEvent Interface Tests")
    class DomainEventInterfaceTests {

        @Test
        @DisplayName("Should implement DomainEvent interface")
        void shouldImplementDomainEventInterface() {
            // Arrange
            EmployeeCreatedEvent event = new EmployeeCreatedEvent(validEmployeeId, validEmployeeName, validEmployeeEmail);

            // Act & Assert
            assertThat(event, is(instanceOf(DomainEvent.class)));
        }

        @Test
        @DisplayName("Should provide DomainEvent methods")
        void shouldProvideDomainEventMethods() {
            // Arrange
            EmployeeCreatedEvent event = new EmployeeCreatedEvent(validEmployeeId, validEmployeeName, validEmployeeEmail);
            DomainEvent domainEvent = event;

            // Act & Assert
            assertThat(domainEvent.getEventId(), is(equalTo(event.eventId())));
            assertThat(domainEvent.getOccurredOn(), is(equalTo(event.occurredOn())));
            assertThat(domainEvent.getEventType(), is(equalTo(event.eventType())));
        }
    }

    @Nested
    @DisplayName("Edge Cases and Validation Tests")
    class EdgeCasesAndValidationTests {

        @Test
        @DisplayName("Should handle null event ID in full constructor")
        void shouldHandleNullEventIdInFullConstructor() {
            // Act & Assert
            assertDoesNotThrow(() -> {
                EmployeeCreatedEvent event = new EmployeeCreatedEvent(
                    null,
                    validOccurredOn,
                    EventType.EMPLOYEE_CREATED_EVENT,
                    validEmployeeId,
                    validEmployeeName,
                    validEmployeeEmail
                );
                assertThat(event.eventId(), is(nullValue()));
            });
        }

        @Test
        @DisplayName("Should handle null occurred on in full constructor")
        void shouldHandleNullOccurredOnInFullConstructor() {
            // Act & Assert
            assertDoesNotThrow(() -> {
                EmployeeCreatedEvent event = new EmployeeCreatedEvent(
                    validEventId,
                    null,
                    EventType.EMPLOYEE_CREATED_EVENT,
                    validEmployeeId,
                    validEmployeeName,
                    validEmployeeEmail
                );
                assertThat(event.occurredOn(), is(nullValue()));
            });
        }

        @Test
        @DisplayName("Should handle null event type in full constructor")
        void shouldHandleNullEventTypeInFullConstructor() {
            // Act & Assert
            assertDoesNotThrow(() -> {
                EmployeeCreatedEvent event = new EmployeeCreatedEvent(
                    validEventId,
                    validOccurredOn,
                    null,
                    validEmployeeId,
                    validEmployeeName,
                    validEmployeeEmail
                );
                assertThat(event.eventType(), is(nullValue()));
            });
        }

        @Test
        @DisplayName("Should handle null employee ID")
        void shouldHandleNullEmployeeId() {
            // Act & Assert
            assertDoesNotThrow(() -> {
                EmployeeCreatedEvent event = new EmployeeCreatedEvent(null, validEmployeeName, validEmployeeEmail);
                assertThat(event.employeeId(), is(nullValue()));
            });
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("Should handle null, empty and whitespace employee names")
        void shouldHandleNullEmptyAndWhitespaceEmployeeNames(String employeeName) {
            // Act & Assert
            assertDoesNotThrow(() -> {
                EmployeeCreatedEvent event = new EmployeeCreatedEvent(validEmployeeId, employeeName, validEmployeeEmail);
                assertThat(event.employeeName(), is(equalTo(employeeName)));
            });
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("Should handle null, empty and whitespace employee emails")
        void shouldHandleNullEmptyAndWhitespaceEmployeeEmails(String employeeEmail) {
            // Act & Assert
            assertDoesNotThrow(() -> {
                EmployeeCreatedEvent event = new EmployeeCreatedEvent(validEmployeeId, validEmployeeName, employeeEmail);
                assertThat(event.employeeEmail(), is(equalTo(employeeEmail)));
            });
        }

        @Test
        @DisplayName("Should handle very long employee names")
        void shouldHandleVeryLongEmployeeNames() {
            // Arrange
            String longName = "A".repeat(1000);

            // Act & Assert
            assertDoesNotThrow(() -> {
                EmployeeCreatedEvent event = new EmployeeCreatedEvent(validEmployeeId, longName, validEmployeeEmail);
                assertThat(event.employeeName(), is(equalTo(longName)));
            });
        }

        @Test
        @DisplayName("Should handle very long employee emails")
        void shouldHandleVeryLongEmployeeEmails() {
            // Arrange
            String longEmail = "A".repeat(500) + "@example.com";

            // Act & Assert
            assertDoesNotThrow(() -> {
                EmployeeCreatedEvent event = new EmployeeCreatedEvent(validEmployeeId, validEmployeeName, longEmail);
                assertThat(event.employeeEmail(), is(equalTo(longEmail)));
            });
        }

        @Test
        @DisplayName("Should handle special characters in employee name")
        void shouldHandleSpecialCharactersInEmployeeName() {
            // Arrange
            String specialName = "José María O'Connor-Smith Müller";

            // Act & Assert
            assertDoesNotThrow(() -> {
                EmployeeCreatedEvent event = new EmployeeCreatedEvent(validEmployeeId, specialName, validEmployeeEmail);
                assertThat(event.employeeName(), is(equalTo(specialName)));
            });
        }

        @Test
        @DisplayName("Should handle special characters in employee email")
        void shouldHandleSpecialCharactersInEmployeeEmail() {
            // Arrange
            String specialEmail = "test+tag@sub-domain.example-site.com";

            // Act & Assert
            assertDoesNotThrow(() -> {
                EmployeeCreatedEvent event = new EmployeeCreatedEvent(validEmployeeId, validEmployeeName, specialEmail);
                assertThat(event.employeeEmail(), is(equalTo(specialEmail)));
            });
        }
    }

    @Nested
    @DisplayName("Record Behavior Tests")
    class RecordBehaviorTests {

        @Test
        @DisplayName("Should implement equals correctly for records")
        void shouldImplementEqualsCorrectlyForRecords() {
            // Arrange
            EmployeeCreatedEvent event1 = new EmployeeCreatedEvent(
                validEventId, validOccurredOn, EventType.EMPLOYEE_CREATED_EVENT,
                validEmployeeId, validEmployeeName, validEmployeeEmail);
            
            EmployeeCreatedEvent event2 = new EmployeeCreatedEvent(
                validEventId, validOccurredOn, EventType.EMPLOYEE_CREATED_EVENT,
                validEmployeeId, validEmployeeName, validEmployeeEmail);

            // Act & Assert
            assertThat(event1, is(equalTo(event2)));
            assertThat(event1.equals(event2), is(true));
        }

        @Test
        @DisplayName("Should implement hashCode correctly for records")
        void shouldImplementHashCodeCorrectlyForRecords() {
            // Arrange
            EmployeeCreatedEvent event1 = new EmployeeCreatedEvent(
                validEventId, validOccurredOn, EventType.EMPLOYEE_CREATED_EVENT,
                validEmployeeId, validEmployeeName, validEmployeeEmail);
            
            EmployeeCreatedEvent event2 = new EmployeeCreatedEvent(
                validEventId, validOccurredOn, EventType.EMPLOYEE_CREATED_EVENT,
                validEmployeeId, validEmployeeName, validEmployeeEmail);

            // Act & Assert
            assertThat(event1.hashCode(), is(equalTo(event2.hashCode())));
        }

        @Test
        @DisplayName("Should not be equal when any field differs")
        void shouldNotBeEqualWhenAnyFieldDiffers() {
            // Arrange
            EmployeeCreatedEvent originalEvent = new EmployeeCreatedEvent(
                validEventId, validOccurredOn, EventType.EMPLOYEE_CREATED_EVENT,
                validEmployeeId, validEmployeeName, validEmployeeEmail);

            EmployeeCreatedEvent differentEventId = new EmployeeCreatedEvent(
                UUID.randomUUID(), validOccurredOn, EventType.EMPLOYEE_CREATED_EVENT,
                validEmployeeId, validEmployeeName, validEmployeeEmail);

            EmployeeCreatedEvent differentEmployeeId = new EmployeeCreatedEvent(
                validEventId, validOccurredOn, EventType.EMPLOYEE_CREATED_EVENT,
                UUID.randomUUID(), validEmployeeName, validEmployeeEmail);

            EmployeeCreatedEvent differentName = new EmployeeCreatedEvent(
                validEventId, validOccurredOn, EventType.EMPLOYEE_CREATED_EVENT,
                validEmployeeId, "Different Name", validEmployeeEmail);

            EmployeeCreatedEvent differentEmail = new EmployeeCreatedEvent(
                validEventId, validOccurredOn, EventType.EMPLOYEE_CREATED_EVENT,
                validEmployeeId, validEmployeeName, "different@email.com");

            // Act & Assert
            assertThat(originalEvent, is(not(equalTo(differentEventId))));
            assertThat(originalEvent, is(not(equalTo(differentEmployeeId))));
            assertThat(originalEvent, is(not(equalTo(differentName))));
            assertThat(originalEvent, is(not(equalTo(differentEmail))));
        }

        @Test
        @DisplayName("Should implement toString correctly for records")
        void shouldImplementToStringCorrectlyForRecords() {
            // Arrange
            EmployeeCreatedEvent event = new EmployeeCreatedEvent(
                validEventId, validOccurredOn, EventType.EMPLOYEE_CREATED_EVENT,
                validEmployeeId, validEmployeeName, validEmployeeEmail);

            // Act
            String result = event.toString();

            // Assert
            assertThat(result, is(notNullValue()));
            assertThat(result, containsString("EmployeeCreatedEvent"));
            assertThat(result, containsString(validEventId.toString()));
            assertThat(result, containsString(validEmployeeId.toString()));
            assertThat(result, containsString(validEmployeeName));
            assertThat(result, containsString(validEmployeeEmail));
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Arrange
            EmployeeCreatedEvent event = new EmployeeCreatedEvent(validEmployeeId, validEmployeeName, validEmployeeEmail);

            // Act & Assert
            assertThat(event.equals(null), is(false));
        }

        @SuppressWarnings("unlikely-arg-type")
        @Test
        @DisplayName("Should not be equal to different class")
        void shouldNotBeEqualToDifferentClass() {
            // Arrange
            EmployeeCreatedEvent event = new EmployeeCreatedEvent(validEmployeeId, validEmployeeName, validEmployeeEmail);
            String differentObject = "Not an EmployeeCreatedEvent";

            // Act & Assert
            assertThat(event.equals(differentObject), is(false));
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should be immutable record")
        void shouldBeImmutableRecord() {
            // Arrange
            EmployeeCreatedEvent event = new EmployeeCreatedEvent(validEmployeeId, validEmployeeName, validEmployeeEmail);

            // Act - Try to access components
            UUID eventId = event.eventId();
            Instant occurredOn = event.occurredOn();
            EventType eventType = event.eventType();
            UUID employeeId = event.employeeId();
            String employeeName = event.employeeName();
            String employeeEmail = event.employeeEmail();

            // Assert - Values should remain the same
            assertThat(event.eventId(), is(equalTo(eventId)));
            assertThat(event.occurredOn(), is(equalTo(occurredOn)));
            assertThat(event.eventType(), is(equalTo(eventType)));
            assertThat(event.employeeId(), is(equalTo(employeeId)));
            assertThat(event.employeeName(), is(equalTo(employeeName)));
            assertThat(event.employeeEmail(), is(equalTo(employeeEmail)));
        }
    }

    @Nested
    @DisplayName("Time Precision Tests")
    class TimePrecisionTests {

        @Test
        @DisplayName("Should handle nanosecond precision in occurred on")
        void shouldHandleNanosecondPrecisionInOccurredOn() {
            // Arrange
            Instant preciseTime = Instant.now().truncatedTo(ChronoUnit.NANOS);

            // Act
            EmployeeCreatedEvent event = new EmployeeCreatedEvent(
                validEventId, preciseTime, EventType.EMPLOYEE_CREATED_EVENT,
                validEmployeeId, validEmployeeName, validEmployeeEmail);

            // Assert
            assertThat(event.occurredOn(), is(equalTo(preciseTime)));
        }

        @Test
        @DisplayName("Should handle past timestamps")
        void shouldHandlePastTimestamps() {
            // Arrange
            Instant pastTime = Instant.now().minus(365, ChronoUnit.DAYS);

            // Act
            EmployeeCreatedEvent event = new EmployeeCreatedEvent(
                validEventId, pastTime, EventType.EMPLOYEE_CREATED_EVENT,
                validEmployeeId, validEmployeeName, validEmployeeEmail);

            // Assert
            assertThat(event.occurredOn(), is(equalTo(pastTime)));
        }

        @Test
        @DisplayName("Should handle future timestamps")
        void shouldHandleFutureTimestamps() {
            // Arrange
            Instant futureTime = Instant.now().plus(365, ChronoUnit.DAYS);

            // Act
            EmployeeCreatedEvent event = new EmployeeCreatedEvent(
                validEventId, futureTime, EventType.EMPLOYEE_CREATED_EVENT,
                validEmployeeId, validEmployeeName, validEmployeeEmail);

            // Assert
            assertThat(event.occurredOn(), is(equalTo(futureTime)));
        }
    }
}
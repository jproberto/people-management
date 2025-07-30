package com.itau.hr.people_management.application.employee.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.itau.hr.people_management.domain.employee.enumeration.EmployeeStatus;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

@DisplayName("ChangeEmployeeStatusRequest Record Tests")
class ChangeEmployeeStatusRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("Record Constructor Tests")
    class RecordConstructorTests {

        @Test
        @DisplayName("Should create record with valid status")
        void shouldCreateRecordWithValidStatus() {
            // Act
            ChangeEmployeeStatusRequest request = new ChangeEmployeeStatusRequest(EmployeeStatus.ACTIVE);

            // Assert
            assertThat(request, is(notNullValue()));
            assertThat(request.newStatus(), is(equalTo(EmployeeStatus.ACTIVE)));
        }

        @ParameterizedTest
        @EnumSource(EmployeeStatus.class)
        @DisplayName("Should create record with all possible status values")
        void shouldCreateRecordWithAllPossibleStatusValues(EmployeeStatus status) {
            // Act
            ChangeEmployeeStatusRequest request = new ChangeEmployeeStatusRequest(status);

            // Assert
            assertThat(request, is(notNullValue()));
            assertThat(request.newStatus(), is(equalTo(status)));
        }

        @Test
        @DisplayName("Should create record with null status")
        void shouldCreateRecordWithNullStatus() {
            // Act
            ChangeEmployeeStatusRequest request = new ChangeEmployeeStatusRequest(null);

            // Assert
            assertThat(request, is(notNullValue()));
            assertThat(request.newStatus(), is(nullValue()));
        }
    }

    @Nested
    @DisplayName("Record Accessor Tests")
    class RecordAccessorTests {

        @Test
        @DisplayName("Should access newStatus field correctly")
        void shouldAccessNewStatusFieldCorrectly() {
            // Arrange
            EmployeeStatus expectedStatus = EmployeeStatus.ACTIVE;
            ChangeEmployeeStatusRequest request = new ChangeEmployeeStatusRequest(expectedStatus);

            // Act
            EmployeeStatus actualStatus = request.newStatus();

            // Assert
            assertThat(actualStatus, is(equalTo(expectedStatus)));
            assertThat(actualStatus, is(sameInstance(expectedStatus)));
        }

        @Test
        @DisplayName("Should return null when status is null")
        void shouldReturnNullWhenStatusIsNull() {
            // Arrange
            ChangeEmployeeStatusRequest request = new ChangeEmployeeStatusRequest(null);

            // Act
            EmployeeStatus status = request.newStatus();

            // Assert
            assertThat(status, is(nullValue()));
        }

        @Test
        @DisplayName("Should maintain consistent accessor behavior")
        void shouldMaintainConsistentAccessorBehavior() {
            // Arrange
            EmployeeStatus status = EmployeeStatus.ON_LEAVE;
            ChangeEmployeeStatusRequest request = new ChangeEmployeeStatusRequest(status);

            // Act & Assert - Multiple calls should return same value
            for (int i = 0; i < 10; i++) {
                assertThat(request.newStatus(), is(equalTo(status)));
                assertThat(request.newStatus(), is(sameInstance(status)));
            }
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @ParameterizedTest
        @EnumSource(EmployeeStatus.class)
        @DisplayName("Should validate successfully with all valid status values")
        void shouldValidateSuccessfullyWithAllValidStatusValues(EmployeeStatus status) {
            // Arrange
            ChangeEmployeeStatusRequest request = new ChangeEmployeeStatusRequest(status);

            // Act
            Set<ConstraintViolation<ChangeEmployeeStatusRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations, is(empty()));
        }

        @Test
        @DisplayName("Should fail validation when status is null")
        void shouldFailValidationWhenStatusIsNull() {
            // Arrange
            ChangeEmployeeStatusRequest request = new ChangeEmployeeStatusRequest(null);

            // Act
            Set<ConstraintViolation<ChangeEmployeeStatusRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations, hasSize(1));
            ConstraintViolation<ChangeEmployeeStatusRequest> violation = violations.iterator().next();
            assertThat(violation.getPropertyPath().toString(), is(equalTo("newStatus")));
            assertThat(violation.getMessage(), is(equalTo("New status cannot be null")));
        }

        @Test
        @DisplayName("Should validate specific status transitions")
        void shouldValidateSpecificStatusTransitions() {
            // Test common status transitions that would be valid
            EmployeeStatus[] validStatuses = {
                EmployeeStatus.ACTIVE,
                EmployeeStatus.TERMINATED,
                EmployeeStatus.ON_LEAVE
            };

            for (EmployeeStatus status : validStatuses) {
                // Arrange
                ChangeEmployeeStatusRequest request = new ChangeEmployeeStatusRequest(status);

                // Act
                Set<ConstraintViolation<ChangeEmployeeStatusRequest>> violations = validator.validate(request);

                // Assert
                assertThat("Status " + status + " should be valid", violations, is(empty()));
            }
        }

        @Test
        @DisplayName("Should provide correct violation details for null status")
        void shouldProvideCorrectViolationDetailsForNullStatus() {
            // Arrange
            ChangeEmployeeStatusRequest request = new ChangeEmployeeStatusRequest(null);

            // Act
            Set<ConstraintViolation<ChangeEmployeeStatusRequest>> violations = validator.validate(request);

            // Assert
            assertThat(violations, hasSize(1));
            ConstraintViolation<ChangeEmployeeStatusRequest> violation = violations.iterator().next();
            
            assertThat(violation.getRootBean(), is(sameInstance(request)));
            assertThat(violation.getLeafBean(), is(sameInstance(request)));
            assertThat(violation.getInvalidValue(), is(nullValue()));
            assertThat(violation.getPropertyPath().toString(), is(equalTo("newStatus")));
            assertThat(violation.getMessage(), is(equalTo("New status cannot be null")));
        }

        @Test
        @DisplayName("Should validate individual property")
        void shouldValidateIndividualProperty() {
            // Arrange
            ChangeEmployeeStatusRequest request = new ChangeEmployeeStatusRequest(EmployeeStatus.ACTIVE);

            // Act
            Set<ConstraintViolation<ChangeEmployeeStatusRequest>> violations = 
                validator.validateProperty(request, "newStatus");

            // Assert
            assertThat(violations, is(empty()));
        }

        @Test
        @DisplayName("Should validate individual property value")
        void shouldValidateIndividualPropertyValue() {
            // Act - Valid value
            Set<ConstraintViolation<ChangeEmployeeStatusRequest>> validViolations = 
                validator.validateValue(ChangeEmployeeStatusRequest.class, "newStatus", EmployeeStatus.ACTIVE);

            // Assert
            assertThat(validViolations, is(empty()));

            // Act - Invalid value (null)
            Set<ConstraintViolation<ChangeEmployeeStatusRequest>> invalidViolations = 
                validator.validateValue(ChangeEmployeeStatusRequest.class, "newStatus", null);

            // Assert
            assertThat(invalidViolations, hasSize(1));
            assertThat(invalidViolations.iterator().next().getMessage(), 
                is(equalTo("New status cannot be null")));
        }
    }

    @Nested
    @DisplayName("Record Equality and HashCode Tests")
    class RecordEqualityAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when status values are equal")
        void shouldBeEqualWhenStatusValuesAreEqual() {
            // Arrange
            ChangeEmployeeStatusRequest request1 = new ChangeEmployeeStatusRequest(EmployeeStatus.ACTIVE);
            ChangeEmployeeStatusRequest request2 = new ChangeEmployeeStatusRequest(EmployeeStatus.ACTIVE);

            // Act & Assert
            assertThat(request1, is(equalTo(request2)));
            assertThat(request1.hashCode(), is(equalTo(request2.hashCode())));
        }

        @Test
        @DisplayName("Should not be equal when status values are different")
        void shouldNotBeEqualWhenStatusValuesAreDifferent() {
            // Arrange
            ChangeEmployeeStatusRequest request1 = new ChangeEmployeeStatusRequest(EmployeeStatus.ACTIVE);
            ChangeEmployeeStatusRequest request2 = new ChangeEmployeeStatusRequest(EmployeeStatus.TERMINATED);

            // Act & Assert
            assertThat(request1, is(not(equalTo(request2))));
            assertThat(request1.hashCode(), is(not(equalTo(request2.hashCode()))));
        }

        @Test
        @DisplayName("Should be equal when both have null status")
        void shouldBeEqualWhenBothHaveNullStatus() {
            // Arrange
            ChangeEmployeeStatusRequest request1 = new ChangeEmployeeStatusRequest(null);
            ChangeEmployeeStatusRequest request2 = new ChangeEmployeeStatusRequest(null);

            // Act & Assert
            assertThat(request1, is(equalTo(request2)));
            assertThat(request1.hashCode(), is(equalTo(request2.hashCode())));
        }

        @Test
        @DisplayName("Should not be equal when one is null and other is not")
        void shouldNotBeEqualWhenOneIsNullAndOtherIsNot() {
            // Arrange
            ChangeEmployeeStatusRequest request1 = new ChangeEmployeeStatusRequest(null);
            ChangeEmployeeStatusRequest request2 = new ChangeEmployeeStatusRequest(EmployeeStatus.ACTIVE);

            // Act & Assert
            assertThat(request1, is(not(equalTo(request2))));
            assertThat(request1.hashCode(), is(not(equalTo(request2.hashCode()))));
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Arrange
            ChangeEmployeeStatusRequest request = new ChangeEmployeeStatusRequest(EmployeeStatus.ON_VACATION);

            // Act & Assert
            assertThat(request, is(equalTo(request)));
            assertThat(request.hashCode(), is(equalTo(request.hashCode())));
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Arrange
            ChangeEmployeeStatusRequest request = new ChangeEmployeeStatusRequest(EmployeeStatus.ACTIVE);

            // Act & Assert
            assertThat(request, is(not(equalTo(null))));
        }

        @Test
        @DisplayName("Should not be equal to different type")
        void shouldNotBeEqualToDifferentType() {
            // Arrange
            ChangeEmployeeStatusRequest request = new ChangeEmployeeStatusRequest(EmployeeStatus.ACTIVE);
            String differentType = "Not a ChangeEmployeeStatusRequest";

            // Act & Assert
            assertThat(request, is(not(equalTo(differentType))));
        }

        @Test
        @DisplayName("Should maintain hashCode consistency")
        void shouldMaintainHashCodeConsistency() {
            // Arrange
            ChangeEmployeeStatusRequest request = new ChangeEmployeeStatusRequest(EmployeeStatus.ACTIVE);
            int expectedHashCode = request.hashCode();

            // Act & Assert - Multiple calls should return same hash code
            for (int i = 0; i < 100; i++) {
                assertThat(request.hashCode(), is(equalTo(expectedHashCode)));
            }
        }
    }

    @Nested
    @DisplayName("Record ToString Tests")
    class RecordToStringTests {

        @Test
        @DisplayName("Should generate meaningful toString representation")
        void shouldGenerateMeaningfulToStringRepresentation() {
            // Arrange
            ChangeEmployeeStatusRequest request = new ChangeEmployeeStatusRequest(EmployeeStatus.ACTIVE);

            // Act
            String stringRepresentation = request.toString();

            // Assert
            assertThat(stringRepresentation, is(notNullValue()));
            assertThat(stringRepresentation, containsString("ChangeEmployeeStatusRequest"));
            assertThat(stringRepresentation, containsString("ACTIVE"));
        }

        @Test
        @DisplayName("Should handle null status in toString")
        void shouldHandleNullStatusInToString() {
            // Arrange
            ChangeEmployeeStatusRequest request = new ChangeEmployeeStatusRequest(null);

            // Act
            String stringRepresentation = request.toString();

            // Assert
            assertThat(stringRepresentation, is(notNullValue()));
            assertThat(stringRepresentation, containsString("ChangeEmployeeStatusRequest"));
            assertThat(stringRepresentation, containsString("null"));
        }

        @ParameterizedTest
        @EnumSource(EmployeeStatus.class)
        @DisplayName("Should include status value in toString for all statuses")
        void shouldIncludeStatusValueInToStringForAllStatuses(EmployeeStatus status) {
            // Arrange
            ChangeEmployeeStatusRequest request = new ChangeEmployeeStatusRequest(status);

            // Act
            String stringRepresentation = request.toString();

            // Assert
            assertThat(stringRepresentation, containsString(status.toString()));
        }

        @Test
        @DisplayName("Should maintain consistent toString output")
        void shouldMaintainConsistentToStringOutput() {
            // Arrange
            ChangeEmployeeStatusRequest request = new ChangeEmployeeStatusRequest(EmployeeStatus.ON_LEAVE);
            String expectedString = request.toString();

            // Act & Assert - Multiple calls should return same string
            for (int i = 0; i < 10; i++) {
                assertThat(request.toString(), is(equalTo(expectedString)));
            }
        }
    }

    @Nested
    @DisplayName("Record Immutability Tests")
    class RecordImmutabilityTests {

        @Test
        @DisplayName("Should be immutable - no setters available")
        void shouldBeImmutableNoSettersAvailable() {
            // Arrange
            ChangeEmployeeStatusRequest request = new ChangeEmployeeStatusRequest(EmployeeStatus.ACTIVE);
            EmployeeStatus originalStatus = request.newStatus();

            // Act & Assert - The newStatus field should remain unchanged
            // Records don't have setters, so the value should always be the same
            assertThat(request.newStatus(), is(equalTo(originalStatus)));
            assertThat(request.newStatus(), is(sameInstance(originalStatus)));
        }

        @Test
        @DisplayName("Should maintain immutability across multiple accesses")
        void shouldMaintainImmutabilityAcrossMultipleAccesses() {
            // Arrange
            ChangeEmployeeStatusRequest request = new ChangeEmployeeStatusRequest(EmployeeStatus.TERMINATED);

            // Act & Assert - Multiple accesses should return same value
            EmployeeStatus status1 = request.newStatus();
            EmployeeStatus status2 = request.newStatus();
            EmployeeStatus status3 = request.newStatus();

            assertThat(status1, is(sameInstance(status2)));
            assertThat(status2, is(sameInstance(status3)));
            assertThat(status1, is(equalTo(EmployeeStatus.TERMINATED)));
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should represent valid status change request")
        void shouldRepresentValidStatusChangeRequest() {
            // Arrange & Act
            ChangeEmployeeStatusRequest activeRequest = new ChangeEmployeeStatusRequest(EmployeeStatus.ACTIVE);
            ChangeEmployeeStatusRequest inactiveRequest = new ChangeEmployeeStatusRequest(EmployeeStatus.TERMINATED);
            ChangeEmployeeStatusRequest suspendedRequest = new ChangeEmployeeStatusRequest(EmployeeStatus.ON_VACATION);

            // Assert - All should be valid business requests
            Set<ConstraintViolation<ChangeEmployeeStatusRequest>> activeViolations = validator.validate(activeRequest);
            Set<ConstraintViolation<ChangeEmployeeStatusRequest>> inactiveViolations = validator.validate(inactiveRequest);
            Set<ConstraintViolation<ChangeEmployeeStatusRequest>> suspendedViolations = validator.validate(suspendedRequest);

            assertThat(activeViolations, is(empty()));
            assertThat(inactiveViolations, is(empty()));
            assertThat(suspendedViolations, is(empty()));
        }

        @Test
        @DisplayName("Should enforce business rule that status cannot be null")
        void shouldEnforceBusinessRuleThatStatusCannotBeNull() {
            // Arrange
            ChangeEmployeeStatusRequest invalidRequest = new ChangeEmployeeStatusRequest(null);

            // Act
            Set<ConstraintViolation<ChangeEmployeeStatusRequest>> violations = validator.validate(invalidRequest);

            // Assert - Business rule violation
            assertThat(violations, hasSize(1));
            assertThat(violations.iterator().next().getMessage(), 
                is(equalTo("New status cannot be null")));
        }

        @Test
        @DisplayName("Should support all defined employee status transitions")
        void shouldSupportAllDefinedEmployeeStatusTransitions() {
            // Test that we can create requests for all possible status values
            EmployeeStatus[] allStatuses = EmployeeStatus.values();
            
            assertThat("Should have at least one status defined", allStatuses.length, is(greaterThan(0)));

            for (EmployeeStatus status : allStatuses) {
                // Arrange & Act
                ChangeEmployeeStatusRequest request = new ChangeEmployeeStatusRequest(status);

                // Assert
                assertThat(request.newStatus(), is(equalTo(status)));
                
                Set<ConstraintViolation<ChangeEmployeeStatusRequest>> violations = validator.validate(request);
                assertThat("Status " + status + " should be valid", violations, is(empty()));
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle rapid record creation")
        void shouldHandleRapidRecordCreation() {
            // Act & Assert
            for (int i = 0; i < 1000; i++) {
                ChangeEmployeeStatusRequest request = new ChangeEmployeeStatusRequest(EmployeeStatus.ACTIVE);
                assertThat(request.newStatus(), is(equalTo(EmployeeStatus.ACTIVE)));
            }
        }

        @Test
        @DisplayName("Should handle records in collections")
        void shouldHandleRecordsInCollections() {
            // Arrange
            ChangeEmployeeStatusRequest request1 = new ChangeEmployeeStatusRequest(EmployeeStatus.ACTIVE);
            ChangeEmployeeStatusRequest request2 = new ChangeEmployeeStatusRequest(EmployeeStatus.TERMINATED);
            ChangeEmployeeStatusRequest request3 = new ChangeEmployeeStatusRequest(EmployeeStatus.ON_VACATION);

            java.util.List<ChangeEmployeeStatusRequest> requests = java.util.Arrays.asList(request1, request2, request3);

            // Act & Assert
            assertThat(requests, hasSize(3));
            assertThat(requests, containsInAnyOrder(request1, request2, request3));
            
            // Verify we can find specific requests
            assertThat(requests.contains(request1), is(true));
            assertThat(requests.contains(new ChangeEmployeeStatusRequest(EmployeeStatus.ACTIVE)), is(true));
        }

        @Test
        @DisplayName("Should work correctly with different enum constants")
        void shouldWorkCorrectlyWithDifferentEnumConstants() {
            // Test with enum constants obtained in different ways
            EmployeeStatus status1 = EmployeeStatus.ACTIVE;
            EmployeeStatus status2 = EmployeeStatus.valueOf("ACTIVE");
            EmployeeStatus status3 = EmployeeStatus.values()[0]; // Assuming ACTIVE is first

            ChangeEmployeeStatusRequest request1 = new ChangeEmployeeStatusRequest(status1);
            ChangeEmployeeStatusRequest request2 = new ChangeEmployeeStatusRequest(status2);
            ChangeEmployeeStatusRequest request3 = new ChangeEmployeeStatusRequest(status3);

            // These should be equal regardless of how the enum was obtained
            assertThat(request1, is(equalTo(request2)));
            assertThat(status1, is(sameInstance(status2))); // Enum constants are singletons
            assertThat(request1, is(equalTo(request3)));
        }
    }
}

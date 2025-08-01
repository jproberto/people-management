package com.itau.hr.people_management.unit.application.employee.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.itau.hr.people_management.application.employee.dto.ChangeEmployeeStatusRequest;
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

    @Test
    @DisplayName("Should create record with valid status")
    void shouldCreateRecordWithValidStatus() {
        // Act
        ChangeEmployeeStatusRequest request = new ChangeEmployeeStatusRequest(EmployeeStatus.ACTIVE);

        // Assert
        assertThat(request.newStatus(), is(EmployeeStatus.ACTIVE));
    }

    @Test
    @DisplayName("Should create record with null status")
    void shouldCreateRecordWithNullStatus() {
        // Act
        ChangeEmployeeStatusRequest request = new ChangeEmployeeStatusRequest(null);

        // Assert
        assertThat(request.newStatus(), is(nullValue()));
    }

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
        assertThat(violation.getPropertyPath().toString(), is("newStatus"));
        assertThat(violation.getMessage(), is("New status cannot be null"));
    }

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
    }
}
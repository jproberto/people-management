package com.itau.hr.people_management.domain.employee.criteria;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.itau.hr.people_management.domain.employee.enumeration.EmployeeStatus;

@DisplayName("EmployeeSearchCriteria Domain Tests")
class EmployeeSearchCriteriaTest {

    @Test
    @DisplayName("Should create criteria with all fields using builder")
    void shouldCreateCriteriaWithAllFieldsUsingBuilder() {
        // Arrange
        UUID departmentId = UUID.randomUUID();
        UUID positionId = UUID.randomUUID();
        String name = "John Doe";
        String emailAddress = "john.doe@example.com";
        String departmentName = "Engineering";
        String positionTitle = "Software Developer";
        String positionLevel = "Senior";
        EmployeeStatus employeeStatus = EmployeeStatus.ACTIVE;

        // Act
        EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder()
            .name(name)
            .emailAddress(emailAddress)
            .employeeStatus(employeeStatus)
            .departmentId(departmentId)
            .departmentName(departmentName)
            .positionId(positionId)
            .positionTitle(positionTitle)
            .positionLevel(positionLevel)
            .build();

        // Assert
        assertThat(criteria.getName().get(), is(name));
        assertThat(criteria.getEmailAddress().get(), is(emailAddress));
        assertThat(criteria.getEmployeeStatus().get(), is(employeeStatus));
        assertThat(criteria.getDepartmentId().get(), is(departmentId));
        assertThat(criteria.getDepartmentName().get(), is(departmentName));
        assertThat(criteria.getPositionId().get(), is(positionId));
        assertThat(criteria.getPositionTitle().get(), is(positionTitle));
        assertThat(criteria.getPositionLevel().get(), is(positionLevel));
    }

    @Test
    @DisplayName("Should create empty criteria using builder")
    void shouldCreateEmptyCriteriaUsingBuilder() {
        // Act
        EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder().build();

        // Assert
        assertThat(criteria.getName().isPresent(), is(false));
        assertThat(criteria.getEmailAddress().isPresent(), is(false));
        assertThat(criteria.getEmployeeStatus().isPresent(), is(false));
        assertThat(criteria.getDepartmentId().isPresent(), is(false));
        assertThat(criteria.getDepartmentName().isPresent(), is(false));
        assertThat(criteria.getPositionId().isPresent(), is(false));
        assertThat(criteria.getPositionTitle().isPresent(), is(false));
        assertThat(criteria.getPositionLevel().isPresent(), is(false));
    }

    @Test
    @DisplayName("Should return empty Optional when values are null")
    void shouldReturnEmptyOptionalWhenValuesAreNull() {
        // Act
        EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder()
            .name(null)
            .emailAddress(null)
            .employeeStatus(null)
            .departmentId(null)
            .departmentName(null)
            .positionId(null)
            .positionTitle(null)
            .positionLevel(null)
            .build();

        // Assert
        assertThat(criteria.getName(), is(Optional.empty()));
        assertThat(criteria.getEmailAddress(), is(Optional.empty()));
        assertThat(criteria.getEmployeeStatus(), is(Optional.empty()));
        assertThat(criteria.getDepartmentId(), is(Optional.empty()));
        assertThat(criteria.getDepartmentName(), is(Optional.empty()));
        assertThat(criteria.getPositionId(), is(Optional.empty()));
        assertThat(criteria.getPositionTitle(), is(Optional.empty()));
        assertThat(criteria.getPositionLevel(), is(Optional.empty()));
    }

    @Test
    @DisplayName("Should allow changing values after creation using setters")
    void shouldAllowChangingValuesAfterCreationUsingSetters() {
        // Arrange
        EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder().build();
        String newName = "Jane Smith";
        EmployeeStatus newStatus = EmployeeStatus.TERMINATED;

        // Act
        criteria.setName(newName);
        criteria.setEmployeeStatus(newStatus);

        // Assert
        assertThat(criteria.getName().get(), is(newName));
        assertThat(criteria.getEmployeeStatus().get(), is(newStatus));
    }

    @Test
    @DisplayName("Should allow setting values to null using setters")
    void shouldAllowSettingValuesToNullUsingSetters() {
        // Arrange
        EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder()
            .name("Initial Name")
            .employeeStatus(EmployeeStatus.ACTIVE)
            .build();

        // Act
        criteria.setName(null);
        criteria.setEmployeeStatus(null);

        // Assert
        assertThat(criteria.getName().isPresent(), is(false));
        assertThat(criteria.getEmployeeStatus().isPresent(), is(false));
    }

    @Test
    @DisplayName("Should handle empty and whitespace string values")
    void shouldHandleEmptyAndWhitespaceStringValues() {
        // Act
        EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder()
            .name("")
            .emailAddress("  ")
            .departmentName("\t")
            .positionTitle("\n")
            .positionLevel("\r")
            .build();

        // Assert
        assertThat(criteria.getName().get(), is(""));
        assertThat(criteria.getEmailAddress().get(), is("  "));
        assertThat(criteria.getDepartmentName().get(), is("\t"));
        assertThat(criteria.getPositionTitle().get(), is("\n"));
        assertThat(criteria.getPositionLevel().get(), is("\r"));
    }
}
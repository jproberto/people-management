package com.itau.hr.people_management.unit.domain.department.entity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.domain.department.entity.Department;
import com.itau.hr.people_management.domain.shared.message.DomainMessageSource;

@ExtendWith(MockitoExtension.class)
@DisplayName("Department Domain Entity Tests")
class DepartmentTest {

    @Mock
    private DomainMessageSource messageSource;

    private UUID validId;
    private String validName;
    private String validCostCenterCode;

    @BeforeEach
    void setUp() {
        Department.setMessageSource(messageSource);
        validId = UUID.randomUUID();
        validName = "Information Technology";
        validCostCenterCode = "IT001";
    }

    @Test
    @DisplayName("Should create department with valid parameters")
    void shouldCreateDepartmentWithValidParameters() {
        // Act
        Department department = Department.create(validId, validName, validCostCenterCode);

        // Assert
        assertThat(department.getId(), is(validId));
        assertThat(department.getName(), is(validName));
        assertThat(department.getCostCenterCode(), is(validCostCenterCode));
    }

    @Test
    @DisplayName("Should trim name and cost center code")
    void shouldTrimNameAndCostCenterCode() {
        // Arrange
        String nameWithSpaces = "  IT Department  ";
        String codeWithSpaces = "  IT001  ";

        // Act
        Department department = Department.create(validId, nameWithSpaces, codeWithSpaces);

        // Assert
        assertThat(department.getName(), is("IT Department"));
        assertThat(department.getCostCenterCode(), is("IT001"));
    }

    @Test
    @DisplayName("Should throw exception when id is null")
    void shouldThrowExceptionWhenIdIsNull() {
        // Arrange
        when(messageSource.getMessage("validation.department.id.null")).thenReturn("Department ID cannot be null");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            Department.create(null, validName, validCostCenterCode)
        );

        assertThat(exception.getMessage(), is("Department ID cannot be null"));
    }

    @Test
    @DisplayName("Should throw exception when name is blank")
    void shouldThrowExceptionWhenNameIsBlank() {
        // Arrange
        when(messageSource.getMessage("validation.department.name.blank")).thenReturn("Department name cannot be blank");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            Department.create(validId, "  ", validCostCenterCode)
        );

        assertThat(exception.getMessage(), is("Department name cannot be blank"));
    }

    @Test
    @DisplayName("Should throw exception when name is too short")
    void shouldThrowExceptionWhenNameIsTooShort() {
        // Arrange
        when(messageSource.getMessage("validation.department.name.length", 2))
            .thenReturn("Department name must be at least 2 characters long");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            Department.create(validId, "A", validCostCenterCode)
        );

        assertThat(exception.getMessage(), is("Department name must be at least 2 characters long"));
    }

    @Test
    @DisplayName("Should throw exception when cost center code is blank")
    void shouldThrowExceptionWhenCostCenterCodeIsBlank() {
        // Arrange
        when(messageSource.getMessage("validation.department.costcentercode.blank"))
            .thenReturn("Cost center code cannot be blank");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            Department.create(validId, validName, "  ")
        );

        assertThat(exception.getMessage(), is("Cost center code cannot be blank"));
    }

    @Test
    @DisplayName("Should be equal when IDs are the same")
    void shouldBeEqualWhenIdsAreTheSame() {
        // Arrange
        Department department1 = Department.create(validId, validName, validCostCenterCode);
        Department department2 = Department.create(validId, "Different Name", "DIFF001");

        // Act & Assert
        assertThat(department1, is(equalTo(department2)));
        assertThat(department1.hashCode(), is(equalTo(department2.hashCode())));
    }

    @Test
    @DisplayName("Should not be equal when IDs are different")
    void shouldNotBeEqualWhenIdsAreDifferent() {
        // Arrange
        UUID differentId = UUID.randomUUID();
        Department department1 = Department.create(validId, validName, validCostCenterCode);
        Department department2 = Department.create(differentId, validName, validCostCenterCode);

        // Act & Assert
        assertThat(department1, is(not(equalTo(department2))));
    }

    @Test
    @DisplayName("Should include ID and name in toString")
    void shouldIncludeIdAndNameInToString() {
        // Arrange
        Department department = Department.create(validId, validName, validCostCenterCode);

        // Act
        String result = department.toString();

        // Assert
        assertThat(result, containsString(validId.toString()));
        assertThat(result, containsString(validName));
    }
}
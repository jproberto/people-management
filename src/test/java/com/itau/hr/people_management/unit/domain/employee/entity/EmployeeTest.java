package com.itau.hr.people_management.unit.domain.employee.entity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.domain.department.entity.Department;
import com.itau.hr.people_management.domain.employee.entity.Employee;
import com.itau.hr.people_management.domain.employee.enumeration.EmployeeStatus;
import com.itau.hr.people_management.domain.employee.event.EmployeeStatusChangedEvent;
import com.itau.hr.people_management.domain.position.entity.Position;
import com.itau.hr.people_management.domain.shared.message.DomainMessageSource;
import com.itau.hr.people_management.domain.shared.vo.Email;
import com.itau.hr.people_management.infrastructure.outbox.holder.DomainEventsHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("Employee Domain Entity Tests")
class EmployeeTest {

    @Mock
    private DomainMessageSource messageSource;

    @Mock
    private Email email;

    @Mock
    private Department department;

    @Mock
    private Position position;

    private UUID validId;
    private String validName;
    private LocalDate validHireDate;
    private EmployeeStatus validStatus;

    @BeforeEach
    void setUp() {
        Employee.setMessageSource(messageSource);
        validId = UUID.randomUUID();
        validName = "John Doe";
        validHireDate = LocalDate.now().minusDays(30);
        validStatus = EmployeeStatus.ACTIVE;
    }

    @Test
    @DisplayName("Should create employee with valid parameters")
    void shouldCreateEmployeeWithValidParameters() {
        // Act
        Employee employee = Employee.create(validId, validName, email, validHireDate, validStatus, department, position);

        // Assert
        assertThat(employee.getId(), is(validId));
        assertThat(employee.getName(), is(validName));
        assertThat(employee.getEmail(), is(email));
        assertThat(employee.getHireDate(), is(validHireDate));
        assertThat(employee.getStatus(), is(validStatus));
        assertThat(employee.getDepartment(), is(department));
        assertThat(employee.getPosition(), is(position));
    }

    @Test
    @DisplayName("Should throw exception when required fields are null")
    void shouldThrowExceptionWhenRequiredFieldsAreNull() {
        // Arrange
        when(messageSource.getMessage("validation.employee.id.null")).thenReturn("ID cannot be null");
        when(messageSource.getMessage("validation.employee.name.blank")).thenReturn("Name cannot be blank");
        when(messageSource.getMessage("validation.employee.email.null")).thenReturn("Email cannot be null");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            Employee.create(null, validName, email, validHireDate, validStatus, department, position));

        assertThrows(IllegalArgumentException.class, () ->
            Employee.create(validId, null, email, validHireDate, validStatus, department, position));

        assertThrows(IllegalArgumentException.class, () ->
            Employee.create(validId, validName, null, validHireDate, validStatus, department, position));
    }

    @Test
    @DisplayName("Should throw exception when name is blank")
    void shouldThrowExceptionWhenNameIsBlank() {
        // Arrange
        when(messageSource.getMessage(anyString())).thenReturn("Invalid name");
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            Employee.create(validId, " ", email, validHireDate, validStatus, department, position));
    }

    @ParameterizedTest
    @MethodSource("invalidNamesProvider")
    @DisplayName("Should throw exception when name is invalid")
    void shouldThrowExceptionWhenNameIsInvalid(String invalidName) {
        // Arrange
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("Invalid name");
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            Employee.create(validId, invalidName, email, validHireDate, validStatus, department, position));
    }

    private static Stream<String> invalidNamesProvider() {
        return Stream.of(
            "A",            // too short
            "A".repeat(101) // too long
        );
    }

    @Test
    @DisplayName("Should throw exception when hire date is future")
    void shouldThrowExceptionWhenHireDateIsFuture() {
        // Arrange
        LocalDate futureDate = LocalDate.now().plusDays(1);
        when(messageSource.getMessage("validation.employee.hiredate.future"))
            .thenReturn("Hire date cannot be in the future");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            Employee.create(validId, validName, email, futureDate, validStatus, department, position));
    }

    @Test
    @DisplayName("Should change status and publish event")
    void shouldChangeStatusAndPublishEvent() {
        try (MockedStatic<DomainEventsHolder> mockedHolder = mockStatic(DomainEventsHolder.class)) {
            // Arrange
            Employee employee = Employee.create(validId, validName, email, validHireDate, EmployeeStatus.ACTIVE, department, position);

            // Act
            employee.changeStatus(EmployeeStatus.TERMINATED);

            // Assert
            assertThat(employee.getStatus(), is(EmployeeStatus.TERMINATED));
            mockedHolder.verify(() -> DomainEventsHolder.addEvent(argThat(event -> {
                EmployeeStatusChangedEvent statusEvent = (EmployeeStatusChangedEvent) event;
                return statusEvent.employeeId().equals(validId) &&
                       statusEvent.oldStatus().equals(EmployeeStatus.ACTIVE) &&
                       statusEvent.newStatus().equals(EmployeeStatus.TERMINATED);
            })));
        }
    }

    @Test
    @DisplayName("Should not allow status change for terminated employee")
    void shouldNotAllowStatusChangeForTerminatedEmployee() {
        // Arrange
        Employee terminatedEmployee = Employee.create(validId, validName, email, validHireDate, 
            EmployeeStatus.TERMINATED, department, position);
        when(messageSource.getMessage("validation.employee.old.status.terminated"))
            .thenReturn("Cannot change status of terminated employee");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            terminatedEmployee.changeStatus(EmployeeStatus.ACTIVE));
    }

    @Test
    @DisplayName("Should reactivate terminated employee")
    void shouldReactivateTerminatedEmployee() {
        try (MockedStatic<DomainEventsHolder> mockedHolder = mockStatic(DomainEventsHolder.class)) {
            // Arrange
            Employee terminatedEmployee = Employee.create(validId, validName, email, validHireDate, 
                EmployeeStatus.TERMINATED, department, position);

            // Act
            terminatedEmployee.reactivate();

            // Assert
            assertThat(terminatedEmployee.getStatus(), is(EmployeeStatus.ACTIVE));
            mockedHolder.verify(() -> DomainEventsHolder.addEvent(any(EmployeeStatusChangedEvent.class)));
        }
    }

    @Test
    @DisplayName("Should not allow reactivation of non-terminated employee")
    void shouldNotAllowReactivationOfNonTerminatedEmployee() {
        // Arrange
        Employee activeEmployee = Employee.create(validId, validName, email, validHireDate, 
            EmployeeStatus.ACTIVE, department, position);
        when(messageSource.getMessage("validation.employee.status.not.terminated"))
            .thenReturn("Employee is not terminated");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, activeEmployee::reactivate);
    }

    @Test
    @DisplayName("Should be equal when IDs are the same")
    void shouldBeEqualWhenIdsAreTheSame() {
        // Arrange
        Employee employee1 = Employee.create(validId, validName, email, validHireDate, validStatus, department, position);
        Employee employee2 = Employee.create(validId, "Different Name", email, validHireDate, validStatus, department, position);

        // Act & Assert
        assertThat(employee1, is(equalTo(employee2)));
        assertThat(employee1.hashCode(), is(equalTo(employee2.hashCode())));
    }

    @Test
    @DisplayName("Should not be equal when IDs are different")
    void shouldNotBeEqualWhenIdsAreDifferent() {
        // Arrange
        UUID differentId = UUID.randomUUID();
        Employee employee1 = Employee.create(validId, validName, email, validHireDate, validStatus, department, position);
        Employee employee2 = Employee.create(differentId, validName, email, validHireDate, validStatus, department, position);

        // Act & Assert
        assertThat(employee1, is(not(equalTo(employee2))));
    }

    @Test
    @DisplayName("Should include ID and name in toString")
    void shouldIncludeIdAndNameInToString() {
        // Arrange
        Employee employee = Employee.create(validId, validName, email, validHireDate, validStatus, department, position);

        // Act
        String result = employee.toString();

        // Assert
        assertThat(result, containsString(validId.toString()));
        assertThat(result, containsString(validName));
    }
}
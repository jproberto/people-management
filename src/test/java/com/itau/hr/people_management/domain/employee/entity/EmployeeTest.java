package com.itau.hr.people_management.domain.employee.entity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.domain.department.entity.Department;
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

    @Nested
    @DisplayName("Employee Creation Tests")
    class EmployeeCreationTests {

        @Test
        @DisplayName("Should create employee with valid parameters")
        void shouldCreateEmployeeWithValidParameters() {
            // Act
            Employee employee = Employee.create(validId, validName, email, validHireDate, validStatus, department, position);

            // Assert
            assertThat(employee, is(notNullValue()));
            assertThat(employee.getId(), is(equalTo(validId)));
            assertThat(employee.getName(), is(equalTo(validName)));
            assertThat(employee.getEmail(), is(equalTo(email)));
            assertThat(employee.getHireDate(), is(equalTo(validHireDate)));
            assertThat(employee.getStatus(), is(equalTo(validStatus)));
            assertThat(employee.getDepartment(), is(equalTo(department)));
            assertThat(employee.getPosition(), is(equalTo(position)));
        }

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            // Arrange
            when(messageSource.getMessage("validation.employee.id.null")).thenReturn("ID cannot be null");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Employee.create(null, validName, email, validHireDate, validStatus, department, position)
            );

            assertThat(exception.getMessage(), is(equalTo("ID cannot be null")));
            verify(messageSource).getMessage("validation.employee.id.null");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", " ", "  ", "\t", "\n"})
        @DisplayName("Should throw exception when name is blank")
        void shouldThrowExceptionWhenNameIsBlank(String invalidName) {
            // Arrange
            when(messageSource.getMessage("validation.employee.name.blank")).thenReturn("Name cannot be blank");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Employee.create(validId, invalidName, email, validHireDate, validStatus, department, position)
            );

            assertThat(exception.getMessage(), is(equalTo("Name cannot be blank")));
        }

        @Test
        @DisplayName("Should throw exception when name is null")
        void shouldThrowExceptionWhenNameIsNull() {
            // Arrange
            when(messageSource.getMessage("validation.employee.name.blank")).thenReturn("Name cannot be blank");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Employee.create(validId, null, email, validHireDate, validStatus, department, position)
            );

            assertThat(exception.getMessage(), is(equalTo("Name cannot be blank")));
        }

        @ParameterizedTest
        @ValueSource(strings = {"A", "ThisNameIsWayTooLongAndExceedsByOneTheMaximumAllowedLengthOfOneHundredCharactersForAnEmployeeBigName!"})
        @DisplayName("Should throw exception when name length is invalid")
        void shouldThrowExceptionWhenNameLengthIsInvalid(String invalidName) {
            // Arrange
            when(messageSource.getMessage("validation.employee.name.length", 2, 100))
                .thenReturn("Name must be between 2 and 100 characters");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Employee.create(validId, invalidName, email, validHireDate, validStatus, department, position)
            );

            assertThat(exception.getMessage(), is(equalTo("Name must be between 2 and 100 characters")));
        }

        @Test
        @DisplayName("Should throw exception when email is null")
        void shouldThrowExceptionWhenEmailIsNull() {
            // Arrange
            when(messageSource.getMessage("validation.employee.email.null")).thenReturn("Email cannot be null");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Employee.create(validId, validName, null, validHireDate, validStatus, department, position)
            );

            assertThat(exception.getMessage(), is(equalTo("Email cannot be null")));
        }

        @Test
        @DisplayName("Should throw exception when hire date is null")
        void shouldThrowExceptionWhenHireDateIsNull() {
            // Arrange
            when(messageSource.getMessage("validation.employee.hiredate.null")).thenReturn("Hire date cannot be null");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Employee.create(validId, validName, email, null, validStatus, department, position)
            );

            assertThat(exception.getMessage(), is(equalTo("Hire date cannot be null")));
        }

        @Test
        @DisplayName("Should throw exception when hire date is in the future")
        void shouldThrowExceptionWhenHireDateIsInTheFuture() {
            // Arrange
            LocalDate futureDate = LocalDate.now().plusDays(1);
            when(messageSource.getMessage("validation.employee.hiredate.future")).thenReturn("Hire date cannot be in the future");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Employee.create(validId, validName, email, futureDate, validStatus, department, position)
            );

            assertThat(exception.getMessage(), is(equalTo("Hire date cannot be in the future")));
        }

        @Test
        @DisplayName("Should allow hire date to be today")
        void shouldAllowHireDateToBeToday() {
            // Arrange
            LocalDate today = LocalDate.now();

            // Act & Assert
            assertDoesNotThrow(() ->
                Employee.create(validId, validName, email, today, validStatus, department, position)
            );
        }

        @Test
        @DisplayName("Should throw exception when status is null")
        void shouldThrowExceptionWhenStatusIsNull() {
            // Arrange
            when(messageSource.getMessage("validation.employee.status.null")).thenReturn("Status cannot be null");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Employee.create(validId, validName, email, validHireDate, null, department, position)
            );

            assertThat(exception.getMessage(), is(equalTo("Status cannot be null")));
        }

        @Test
        @DisplayName("Should throw exception when department is null")
        void shouldThrowExceptionWhenDepartmentIsNull() {
            // Arrange
            when(messageSource.getMessage("validation.employee.department.null")).thenReturn("Department cannot be null");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Employee.create(validId, validName, email, validHireDate, validStatus, null, position)
            );

            assertThat(exception.getMessage(), is(equalTo("Department cannot be null")));
        }

        @Test
        @DisplayName("Should throw exception when position is null")
        void shouldThrowExceptionWhenPositionIsNull() {
            // Arrange
            when(messageSource.getMessage("validation.employee.position.null")).thenReturn("Position cannot be null");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Employee.create(validId, validName, email, validHireDate, validStatus, department, null)
            );

            assertThat(exception.getMessage(), is(equalTo("Position cannot be null")));
        }
    }

    @Nested
    @DisplayName("Status Change Tests")
    class StatusChangeTests {

        private Employee employee;

        @BeforeEach
        void setUp() {
            employee = Employee.create(validId, validName, email, validHireDate, EmployeeStatus.ACTIVE, department, position);
        }

        @Test
        @DisplayName("Should change status from ACTIVE to INACTIVE")
        void shouldChangeStatusFromActiveToInactive() {
            try (MockedStatic<DomainEventsHolder> mockedHolder = mockStatic(DomainEventsHolder.class)) {
                // Act
                employee.changeStatus(EmployeeStatus.INACTIVE);

                // Assert
                assertThat(employee.getStatus(), is(equalTo(EmployeeStatus.INACTIVE)));
                
                mockedHolder.verify(() -> DomainEventsHolder.addEvent(any(EmployeeStatusChangedEvent.class)));
            }
        }

        @Test
        @DisplayName("Should change status from ACTIVE to TERMINATED")
        void shouldChangeStatusFromActiveToTerminated() {
            try (MockedStatic<DomainEventsHolder> mockedHolder = mockStatic(DomainEventsHolder.class)) {
                // Act
                employee.changeStatus(EmployeeStatus.TERMINATED);

                // Assert
                assertThat(employee.getStatus(), is(equalTo(EmployeeStatus.TERMINATED)));
                
                mockedHolder.verify(() -> DomainEventsHolder.addEvent(any(EmployeeStatusChangedEvent.class)));
            }
        }

        @Test
        @DisplayName("Should throw exception when changing status to null")
        void shouldThrowExceptionWhenChangingStatusToNull() {
            // Arrange
            when(messageSource.getMessage("validation.employee.status.null")).thenReturn("Status cannot be null");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                employee.changeStatus(null)
            );

            assertThat(exception.getMessage(), is(equalTo("Status cannot be null")));
        }

        @Test
        @DisplayName("Should throw exception when trying to change status of terminated employee")
        void shouldThrowExceptionWhenTryingToChangeStatusOfTerminatedEmployee() {
            // Arrange
            Employee terminatedEmployee = Employee.create(validId, validName, email, validHireDate, 
                EmployeeStatus.TERMINATED, department, position);
            when(messageSource.getMessage("validation.employee.old.status.terminated"))
                .thenReturn("Cannot change status of terminated employee");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                terminatedEmployee.changeStatus(EmployeeStatus.ACTIVE)
            );

            assertThat(exception.getMessage(), is(equalTo("Cannot change status of terminated employee")));
        }

        @Test
        @DisplayName("Should publish domain event when status changes")
        void shouldPublishDomainEventWhenStatusChanges() {
            try (MockedStatic<DomainEventsHolder> mockedHolder = mockStatic(DomainEventsHolder.class)) {
                // Act
                employee.changeStatus(EmployeeStatus.INACTIVE);

                // Assert
                mockedHolder.verify(() -> DomainEventsHolder.addEvent(argThat(event -> {
                    EmployeeStatusChangedEvent statusEvent = (EmployeeStatusChangedEvent) event;
                    return statusEvent.employeeId().equals(validId) &&
                           statusEvent.oldStatus().equals(EmployeeStatus.ACTIVE) &&
                           statusEvent.newStatus().equals(EmployeeStatus.INACTIVE);
                })));
            }
        }
    }

    @Nested
    @DisplayName("Reactivation Tests")
    class ReactivationTests {

        @Test
        @DisplayName("Should reactivate terminated employee")
        void shouldReactivateTerminatedEmployee() {
            // Arrange
            Employee terminatedEmployee = Employee.create(validId, validName, email, validHireDate, 
                EmployeeStatus.TERMINATED, department, position);

            try (MockedStatic<DomainEventsHolder> mockedHolder = mockStatic(DomainEventsHolder.class)) {
                // Act
                terminatedEmployee.reactivate();

                // Assert
                assertThat(terminatedEmployee.getStatus(), is(equalTo(EmployeeStatus.ACTIVE)));
                
                mockedHolder.verify(() -> DomainEventsHolder.addEvent(any(EmployeeStatusChangedEvent.class)));
            }
        }

        @Test
        @DisplayName("Should throw exception when trying to reactivate non-terminated employee")
        void shouldThrowExceptionWhenTryingToReactivateNonTerminatedEmployee() {
            // Arrange
            Employee activeEmployee = Employee.create(validId, validName, email, validHireDate, 
                EmployeeStatus.ACTIVE, department, position);
            when(messageSource.getMessage("validation.employee.status.not.terminated"))
                .thenReturn("Employee is not terminated");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, activeEmployee::reactivate);

            assertThat(exception.getMessage(), is(equalTo("Employee is not terminated")));
        }

        @Test
        @DisplayName("Should publish domain event when employee is reactivated")
        void shouldPublishDomainEventWhenEmployeeIsReactivated() {
            // Arrange
            Employee terminatedEmployee = Employee.create(validId, validName, email, validHireDate, 
                EmployeeStatus.TERMINATED, department, position);

            try (MockedStatic<DomainEventsHolder> mockedHolder = mockStatic(DomainEventsHolder.class)) {
                // Act
                terminatedEmployee.reactivate();

                // Assert
                mockedHolder.verify(() -> DomainEventsHolder.addEvent(argThat(event -> {
                    EmployeeStatusChangedEvent statusEvent = (EmployeeStatusChangedEvent) event;
                    return statusEvent.employeeId().equals(validId) &&
                           statusEvent.oldStatus().equals(EmployeeStatus.TERMINATED) &&
                           statusEvent.newStatus().equals(EmployeeStatus.ACTIVE);
                })));
            }
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

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
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Arrange
            Employee employee = Employee.create(validId, validName, email, validHireDate, validStatus, department, position);

            // Act & Assert
            assertThat(employee, is(not(equalTo(null))));
        }

        @Test
        @DisplayName("Should not be equal to object of different class")
        void shouldNotBeEqualToObjectOfDifferentClass() {
            // Arrange
            Employee employee = Employee.create(validId, validName, email, validHireDate, validStatus, department, position);
            String differentObject = "Not an Employee";

            // Act & Assert
            assertThat(employee, is(not(equalTo(differentObject))));
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

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

    @Nested
    @DisplayName("Message Source Configuration Tests")
    class MessageSourceConfigurationTests {

        @Test
        @DisplayName("Should set message source")
        void shouldSetMessageSource() {
            // Arrange
            DomainMessageSource newMessageSource = mock(DomainMessageSource.class);

            // Act & Assert
            assertDoesNotThrow(() -> Employee.setMessageSource(newMessageSource));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle minimum valid name length")
        void shouldHandleMinimumValidNameLength() {
            // Arrange
            String minName = "Jo";

            // Act & Assert
            assertDoesNotThrow(() ->
                Employee.create(validId, minName, email, validHireDate, validStatus, department, position)
            );
        }

        @Test
        @DisplayName("Should handle maximum valid name length")
        void shouldHandleMaximumValidNameLength() {
            // Arrange
            String maxName = "A".repeat(100);

            // Act & Assert
            assertDoesNotThrow(() ->
                Employee.create(validId, maxName, email, validHireDate, validStatus, department, position)
            );
        }

        @Test
        @DisplayName("Should handle hire date exactly one day in the past")
        void shouldHandleHireDateExactlyOneDayInThePast() {
            // Arrange
            LocalDate yesterday = LocalDate.now().minusDays(1);

            // Act & Assert
            assertDoesNotThrow(() ->
                Employee.create(validId, validName, email, yesterday, validStatus, department, position)
            );
        }
    }
}
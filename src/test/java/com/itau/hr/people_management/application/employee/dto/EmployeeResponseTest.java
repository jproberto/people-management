package com.itau.hr.people_management.application.employee.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.UUID;

import org.checkerframework.checker.units.qual.t;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.application.department.dto.DepartmentResponse;
import com.itau.hr.people_management.application.position.dto.PositionResponse;
import com.itau.hr.people_management.domain.department.entity.Department;
import com.itau.hr.people_management.domain.employee.entity.Employee;
import com.itau.hr.people_management.domain.employee.enumeration.EmployeeStatus;
import com.itau.hr.people_management.domain.position.entity.Position;
import com.itau.hr.people_management.domain.shared.vo.Email;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeResponse Unit Tests")
class EmployeeResponseTest {

    @Mock
    private Employee employee;

    @Mock
    private Email email;

    @Mock
    private Department department;

    @Mock
    private Position position;

    private UUID employeeId;
    private String employeeName;
    private String emailAddress;
    private LocalDate hireDate;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        employeeName = "John Doe";
        emailAddress = "john.doe@itau.com.br";
        hireDate = LocalDate.of(2023, 1, 15);
    }

    @Nested
    @DisplayName("Constructor with Employee - Success Tests")
    class ConstructorWithEmployeeSuccessTests {

        @Test
        @DisplayName("Should create EmployeeResponse with complete employee data")
        void shouldCreateEmployeeResponseWithCompleteEmployeeData() {
            // Arrange
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(email);
            when(email.getAddress()).thenReturn(emailAddress);
            when(employee.getHireDate()).thenReturn(hireDate);
            when(employee.getStatus()).thenReturn(EmployeeStatus.ACTIVE);
            when(employee.getDepartment()).thenReturn(department);
            when(employee.getPosition()).thenReturn(position);

            // Act
            EmployeeResponse response = new EmployeeResponse(employee);

            // Assert
            assertThat(response.getId(), is(equalTo(employeeId)));
            assertThat(response.getName(), is(equalTo(employeeName)));
            assertThat(response.getEmail(), is(equalTo(emailAddress)));
            assertThat(response.getHireDate(), is(equalTo(hireDate)));
            assertThat(response.getEmployeeStatus(), is(equalTo("ACTIVE")));
            assertThat(response.getDepartment(), is(notNullValue()));
            assertThat(response.getPosition(), is(notNullValue()));
        }

        @Test
        @DisplayName("Should create EmployeeResponse with minimal employee data")
        void shouldCreateEmployeeResponseWithMinimalEmployeeData() {
            // Arrange
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(null);
            when(employee.getHireDate()).thenReturn(hireDate);
            when(employee.getStatus()).thenReturn(null);
            when(employee.getDepartment()).thenReturn(null);
            when(employee.getPosition()).thenReturn(null);

            // Act
            EmployeeResponse response = new EmployeeResponse(employee);

            // Assert
            assertThat(response.getId(), is(equalTo(employeeId)));
            assertThat(response.getName(), is(equalTo(employeeName)));
            assertThat(response.getEmail(), is(nullValue()));
            assertThat(response.getHireDate(), is(equalTo(hireDate)));
            assertThat(response.getEmployeeStatus(), is(nullValue()));
            assertThat(response.getDepartment(), is(nullValue()));
            assertThat(response.getPosition(), is(nullValue()));
        }

        @Test
        @DisplayName("Should handle all employee status values correctly")
        void shouldHandleAllEmployeeStatusValuesCorrectly() {
            // Arrange
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(null);
            when(employee.getHireDate()).thenReturn(hireDate);
            when(employee.getDepartment()).thenReturn(null);
            when(employee.getPosition()).thenReturn(null);

            for (EmployeeStatus status : EmployeeStatus.values()) {
                // Arrange
                when(employee.getStatus()).thenReturn(status);

                // Act
                EmployeeResponse response = new EmployeeResponse(employee);

                // Assert
                assertThat(response.getEmployeeStatus(), is(equalTo(status.name())));
            }
        }

        @Test
        @DisplayName("Should call employee getters exactly once")
        void shouldCallEmployeeGettersExactlyOnce() {
            // Arrange
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(email);
            when(email.getAddress()).thenReturn(emailAddress);
            when(employee.getHireDate()).thenReturn(hireDate);
            when(employee.getStatus()).thenReturn(EmployeeStatus.ACTIVE);
            when(employee.getDepartment()).thenReturn(department);
            when(employee.getPosition()).thenReturn(position);

            // Act
            new EmployeeResponse(employee);

            // Assert
            verify(employee, times(1)).getId();
            verify(employee, times(1)).getName();
            verify(employee, times(2)).getEmail();
            verify(employee, times(1)).getHireDate();
            verify(employee, times(2)).getStatus();
            verify(employee, times(2)).getDepartment();
            verify(employee, times(2)).getPosition();
            verify(email, times(1)).getAddress();
        }

        @Test
        @DisplayName("Should create new DepartmentResponse when department is present")
        void shouldCreateNewDepartmentResponseWhenDepartmentIsPresent() {
            // Arrange
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(null);
            when(employee.getHireDate()).thenReturn(hireDate);
            when(employee.getStatus()).thenReturn(null);
            when(employee.getDepartment()).thenReturn(department);
            when(employee.getPosition()).thenReturn(null);

            // Act
            EmployeeResponse response = new EmployeeResponse(employee);

            // Assert
            assertThat(response.getDepartment(), is(notNullValue()));
            assertThat(response.getDepartment(), is(instanceOf(DepartmentResponse.class)));
        }

        @Test
        @DisplayName("Should create new PositionResponse when position is present")
        void shouldCreateNewPositionResponseWhenPositionIsPresent() {
            // Arrange
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(null);
            when(employee.getHireDate()).thenReturn(hireDate);
            when(employee.getStatus()).thenReturn(null);
            when(employee.getDepartment()).thenReturn(null);
            when(employee.getPosition()).thenReturn(position);

            // Act
            EmployeeResponse response = new EmployeeResponse(employee);

            // Assert
            assertThat(response.getPosition(), is(notNullValue()));
            assertThat(response.getPosition(), is(instanceOf(PositionResponse.class)));
        }

        @Test
        @DisplayName("Should handle empty string values correctly")
        void shouldHandleEmptyStringValuesCorrectly() {
            // Arrange
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn("");
            when(employee.getEmail()).thenReturn(email);
            when(email.getAddress()).thenReturn("");
            when(employee.getHireDate()).thenReturn(hireDate);
            when(employee.getStatus()).thenReturn(EmployeeStatus.ACTIVE);
            when(employee.getDepartment()).thenReturn(null);
            when(employee.getPosition()).thenReturn(null);

            // Act
            EmployeeResponse response = new EmployeeResponse(employee);

            // Assert
            assertThat(response.getName(), is(equalTo("")));
            assertThat(response.getEmail(), is(equalTo("")));
        }

        @Test
        @DisplayName("Should handle special characters in employee data")
        void shouldHandleSpecialCharactersInEmployeeData() {
            // Arrange
            String specialName = "José María Fernández-González";
            String specialEmail = "josé.maría@empresa-teste.com.br";
            
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(specialName);
            when(employee.getEmail()).thenReturn(email);
            when(email.getAddress()).thenReturn(specialEmail);
            when(employee.getHireDate()).thenReturn(hireDate);
            when(employee.getStatus()).thenReturn(EmployeeStatus.ACTIVE);
            when(employee.getDepartment()).thenReturn(null);
            when(employee.getPosition()).thenReturn(null);

            // Act
            EmployeeResponse response = new EmployeeResponse(employee);

            // Assert
            assertThat(response.getName(), is(equalTo(specialName)));
            assertThat(response.getEmail(), is(equalTo(specialEmail)));
        }

        @Test
        @DisplayName("Should preserve exact hire date")
        void shouldPreserveExactHireDate() {
            // Arrange
            LocalDate[] testDates = {
                LocalDate.of(2020, 1, 1),
                LocalDate.of(2023, 12, 31),
                LocalDate.of(2024, 2, 29), // Leap year
                LocalDate.now(),
                LocalDate.now().minusYears(10),
                LocalDate.now().plusDays(30)
            };

            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(null);
            when(employee.getStatus()).thenReturn(null);
            when(employee.getDepartment()).thenReturn(null);
            when(employee.getPosition()).thenReturn(null);

            for (LocalDate testDate : testDates) {
                // Arrange
                when(employee.getHireDate()).thenReturn(testDate);

                // Act
                EmployeeResponse response = new EmployeeResponse(employee);

                // Assert
                assertThat(response.getHireDate(), is(equalTo(testDate)));
            }
        }
    }

    @Nested
    @DisplayName("Constructor with Employee - Null Handling Tests")
    class ConstructorWithEmployeeNullHandlingTests {

        @Test
        @DisplayName("Should throw IllegalArgumentException when employee is null")
        void shouldThrowIllegalArgumentExceptionWhenEmployeeIsNull() {
            // Act & Assert
            IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () -> {
                new EmployeeResponse(null);
            });

            assertThat(thrownException.getMessage(), is(equalTo("Employee cannot be null")));
        }

        @Test
        @DisplayName("Should handle null email gracefully")
        void shouldHandleNullEmailGracefully() {
            // Arrange
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(null);
            when(employee.getHireDate()).thenReturn(hireDate);
            when(employee.getStatus()).thenReturn(EmployeeStatus.ACTIVE);
            when(employee.getDepartment()).thenReturn(null);
            when(employee.getPosition()).thenReturn(null);

            // Act
            EmployeeResponse response = new EmployeeResponse(employee);

            // Assert
            assertThat(response.getEmail(), is(nullValue()));
            verify(employee).getEmail();
            verify(email, never()).getAddress();
        }

        @Test
        @DisplayName("Should handle null employee status gracefully")
        void shouldHandleNullEmployeeStatusGracefully() {
            // Arrange
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(null);
            when(employee.getHireDate()).thenReturn(hireDate);
            when(employee.getStatus()).thenReturn(null);
            when(employee.getDepartment()).thenReturn(null);
            when(employee.getPosition()).thenReturn(null);

            // Act
            EmployeeResponse response = new EmployeeResponse(employee);

            // Assert
            assertThat(response.getEmployeeStatus(), is(nullValue()));
        }

        @Test
        @DisplayName("Should handle null department gracefully")
        void shouldHandleNullDepartmentGracefully() {
            // Arrange
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(null);
            when(employee.getHireDate()).thenReturn(hireDate);
            when(employee.getStatus()).thenReturn(null);
            when(employee.getDepartment()).thenReturn(null);
            when(employee.getPosition()).thenReturn(null);

            // Act
            EmployeeResponse response = new EmployeeResponse(employee);

            // Assert
            assertThat(response.getDepartment(), is(nullValue()));
        }

        @Test
        @DisplayName("Should handle null position gracefully")
        void shouldHandleNullPositionGracefully() {
            // Arrange
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(null);
            when(employee.getHireDate()).thenReturn(hireDate);
            when(employee.getStatus()).thenReturn(null);
            when(employee.getDepartment()).thenReturn(null);
            when(employee.getPosition()).thenReturn(null);

            // Act
            EmployeeResponse response = new EmployeeResponse(employee);

            // Assert
            assertThat(response.getPosition(), is(nullValue()));
        }

        @Test
        @DisplayName("Should handle all null optional fields together")
        void shouldHandleAllNullOptionalFieldsTogether() {
            // Arrange
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(null);
            when(employee.getHireDate()).thenReturn(hireDate);
            when(employee.getStatus()).thenReturn(null);
            when(employee.getDepartment()).thenReturn(null);
            when(employee.getPosition()).thenReturn(null);

            // Act
            EmployeeResponse response = new EmployeeResponse(employee);

            // Assert
            assertThat(response.getId(), is(equalTo(employeeId)));
            assertThat(response.getName(), is(equalTo(employeeName)));
            assertThat(response.getEmail(), is(nullValue()));
            assertThat(response.getHireDate(), is(equalTo(hireDate)));
            assertThat(response.getEmployeeStatus(), is(nullValue()));
            assertThat(response.getDepartment(), is(nullValue()));
            assertThat(response.getPosition(), is(nullValue()));
        }
    }

    @Nested
    @DisplayName("Constructor with Employee - Email Handling Tests")
    class ConstructorWithEmployeeEmailHandlingTests {

        @Test
        @DisplayName("Should extract email address when email object is present")
        void shouldExtractEmailAddressWhenEmailObjectIsPresent() {
            // Arrange
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(email);
            when(email.getAddress()).thenReturn(emailAddress);
            when(employee.getHireDate()).thenReturn(hireDate);
            when(employee.getStatus()).thenReturn(null);
            when(employee.getDepartment()).thenReturn(null);
            when(employee.getPosition()).thenReturn(null);

            // Act
            EmployeeResponse response = new EmployeeResponse(employee);

            // Assert
            assertThat(response.getEmail(), is(equalTo(emailAddress)));
            verify(employee, times(2)).getEmail();
            verify(email).getAddress();
        }

        @Test
        @DisplayName("Should handle null email address from email object")
        void shouldHandleNullEmailAddressFromEmailObject() {
            // Arrange
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(email);
            when(email.getAddress()).thenReturn(null);
            when(employee.getHireDate()).thenReturn(hireDate);
            when(employee.getStatus()).thenReturn(null);
            when(employee.getDepartment()).thenReturn(null);
            when(employee.getPosition()).thenReturn(null);

            // Act
            EmployeeResponse response = new EmployeeResponse(employee);

            // Assert
            assertThat(response.getEmail(), is(nullValue()));
            verify(employee, times(2)).getEmail();
            verify(email).getAddress();
        }

        @Test
        @DisplayName("Should not call getAddress when email object is null")
        void shouldNotCallGetAddressWhenEmailObjectIsNull() {
            // Arrange
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(null);
            when(employee.getHireDate()).thenReturn(hireDate);
            when(employee.getStatus()).thenReturn(null);
            when(employee.getDepartment()).thenReturn(null);
            when(employee.getPosition()).thenReturn(null);

            // Act
            EmployeeResponse response = new EmployeeResponse(employee);

            // Assert
            assertThat(response.getEmail(), is(nullValue()));
            verify(employee).getEmail();
            verify(email, never()).getAddress();
        }

        @Test
        @DisplayName("Should handle various email formats correctly")
        void shouldHandleVariousEmailFormatsCorrectly() {
            // Arrange
            String[] emailFormats = {
                "simple@example.com",
                "user.name@company.co.uk",
                "user+tag@domain.org",
                "123456@numbers.com",
                "test-email@sub.domain.com",
                "a@b.co"
            };

            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(email);
            when(employee.getHireDate()).thenReturn(hireDate);
            when(employee.getStatus()).thenReturn(null);
            when(employee.getDepartment()).thenReturn(null);
            when(employee.getPosition()).thenReturn(null);

            for (String emailFormat : emailFormats) {
                // Arrange
                when(email.getAddress()).thenReturn(emailFormat);

                // Act
                EmployeeResponse response = new EmployeeResponse(employee);

                // Assert
                assertThat(response.getEmail(), is(equalTo(emailFormat)));
            }
        }
    }

    @Nested
    @DisplayName("Constructor with Employee - Status Handling Tests")
    class ConstructorWithEmployeeStatusHandlingTests {

        @Test
        @DisplayName("Should convert status to string using name() method")
        void shouldConvertStatusToStringUsingNameMethod() {
            // Arrange
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(null);
            when(employee.getHireDate()).thenReturn(hireDate);
            when(employee.getStatus()).thenReturn(EmployeeStatus.ACTIVE);
            when(employee.getDepartment()).thenReturn(null);
            when(employee.getPosition()).thenReturn(null);

            // Act
            EmployeeResponse response = new EmployeeResponse(employee);

            // Assert
            assertThat(response.getEmployeeStatus(), is(equalTo("ACTIVE")));
            assertThat(response.getEmployeeStatus(), is(equalTo(EmployeeStatus.ACTIVE.name())));
        }

        @Test
        @DisplayName("Should handle all possible employee status values")
        void shouldHandleAllPossibleEmployeeStatusValues() {
            // Arrange
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(null);
            when(employee.getHireDate()).thenReturn(hireDate);
            when(employee.getDepartment()).thenReturn(null);
            when(employee.getPosition()).thenReturn(null);

            for (EmployeeStatus status : EmployeeStatus.values()) {
                // Arrange
                when(employee.getStatus()).thenReturn(status);

                // Act
                EmployeeResponse response = new EmployeeResponse(employee);

                // Assert
                assertThat(response.getEmployeeStatus(), is(equalTo(status.name())));
                assertThat(response.getEmployeeStatus(), is(notNullValue()));
            }
        }

        @Test
        @DisplayName("Should return null when employee status is null")
        void shouldReturnNullWhenEmployeeStatusIsNull() {
            // Arrange
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(null);
            when(employee.getHireDate()).thenReturn(hireDate);
            when(employee.getStatus()).thenReturn(null);
            when(employee.getDepartment()).thenReturn(null);
            when(employee.getPosition()).thenReturn(null);

            // Act
            EmployeeResponse response = new EmployeeResponse(employee);

            // Assert
            assertThat(response.getEmployeeStatus(), is(nullValue()));
        }
    }

    @Nested
    @DisplayName("Constructor with Employee - Department and Position Tests")
    class ConstructorWithEmployeeDepartmentAndPositionTests {

        @Test
        @DisplayName("Should create DepartmentResponse when department is present")
        void shouldCreateDepartmentResponseWhenDepartmentIsPresent() {
            // Arrange
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(null);
            when(employee.getHireDate()).thenReturn(hireDate);
            when(employee.getStatus()).thenReturn(null);
            when(employee.getDepartment()).thenReturn(department);
            when(employee.getPosition()).thenReturn(null);

            // Act
            EmployeeResponse response = new EmployeeResponse(employee);

            // Assert
            assertThat(response.getDepartment(), is(notNullValue()));
            assertThat(response.getDepartment(), is(instanceOf(DepartmentResponse.class)));
            verify(employee, times(2)).getDepartment();
        }

        @Test
        @DisplayName("Should create PositionResponse when position is present")
        void shouldCreatePositionResponseWhenPositionIsPresent() {
            // Arrange
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(null);
            when(employee.getHireDate()).thenReturn(hireDate);
            when(employee.getStatus()).thenReturn(null);
            when(employee.getDepartment()).thenReturn(null);
            when(employee.getPosition()).thenReturn(position);

            // Act
            EmployeeResponse response = new EmployeeResponse(employee);

            // Assert
            assertThat(response.getPosition(), is(notNullValue()));
            assertThat(response.getPosition(), is(instanceOf(PositionResponse.class)));
            verify(employee, times(2)).getPosition();
        }

        @Test
        @DisplayName("Should create both responses when both department and position are present")
        void shouldCreateBothResponsesWhenBothDepartmentAndPositionArePresent() {
            // Arrange
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(null);
            when(employee.getHireDate()).thenReturn(hireDate);
            when(employee.getStatus()).thenReturn(null);
            when(employee.getDepartment()).thenReturn(department);
            when(employee.getPosition()).thenReturn(position);

            // Act
            EmployeeResponse response = new EmployeeResponse(employee);

            // Assert
            assertThat(response.getDepartment(), is(notNullValue()));
            assertThat(response.getDepartment(), is(instanceOf(DepartmentResponse.class)));
            assertThat(response.getPosition(), is(notNullValue()));
            assertThat(response.getPosition(), is(instanceOf(PositionResponse.class)));
        }

        @Test
        @DisplayName("Should set null when both department and position are null")
        void shouldSetNullWhenBothDepartmentAndPositionAreNull() {
            // Arrange
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(null);
            when(employee.getHireDate()).thenReturn(hireDate);
            when(employee.getStatus()).thenReturn(null);
            when(employee.getDepartment()).thenReturn(null);
            when(employee.getPosition()).thenReturn(null);

            // Act
            EmployeeResponse response = new EmployeeResponse(employee);

            // Assert
            assertThat(response.getDepartment(), is(nullValue()));
            assertThat(response.getPosition(), is(nullValue()));
        }
    }

    @Nested
    @DisplayName("Constructor with Employee - Edge Cases Tests")
    class ConstructorWithEmployeeEdgeCasesTests {

        @Test
        @DisplayName("Should handle employee with only required fields")
        void shouldHandleEmployeeWithOnlyRequiredFields() {
            // Arrange
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(null);
            when(employee.getHireDate()).thenReturn(hireDate);
            when(employee.getStatus()).thenReturn(null);
            when(employee.getDepartment()).thenReturn(null);
            when(employee.getPosition()).thenReturn(null);

            // Act
            EmployeeResponse response = new EmployeeResponse(employee);

            // Assert
            assertThat(response.getId(), is(equalTo(employeeId)));
            assertThat(response.getName(), is(equalTo(employeeName)));
            assertThat(response.getHireDate(), is(equalTo(hireDate)));
            assertThat(response.getEmail(), is(nullValue()));
            assertThat(response.getEmployeeStatus(), is(nullValue()));
            assertThat(response.getDepartment(), is(nullValue()));
            assertThat(response.getPosition(), is(nullValue()));
        }

        @Test
        @DisplayName("Should handle employee with maximum data")
        void shouldHandleEmployeeWithMaximumData() {
            // Arrange
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(email);
            when(email.getAddress()).thenReturn(emailAddress);
            when(employee.getHireDate()).thenReturn(hireDate);
            when(employee.getStatus()).thenReturn(EmployeeStatus.ACTIVE);
            when(employee.getDepartment()).thenReturn(department);
            when(employee.getPosition()).thenReturn(position);

            // Act
            EmployeeResponse response = new EmployeeResponse(employee);

            // Assert
            assertThat(response.getId(), is(equalTo(employeeId)));
            assertThat(response.getName(), is(equalTo(employeeName)));
            assertThat(response.getEmail(), is(equalTo(emailAddress)));
            assertThat(response.getHireDate(), is(equalTo(hireDate)));
            assertThat(response.getEmployeeStatus(), is(equalTo("ACTIVE")));
            assertThat(response.getDepartment(), is(notNullValue()));
            assertThat(response.getPosition(), is(notNullValue()));
        }

        @Test
        @DisplayName("Should be consistent across multiple instantiations with same employee")
        void shouldBeConsistentAcrossMultipleInstantiationsWithSameEmployee() {
            // Arrange
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(email);
            when(email.getAddress()).thenReturn(emailAddress);
            when(employee.getHireDate()).thenReturn(hireDate);
            when(employee.getStatus()).thenReturn(EmployeeStatus.ACTIVE);
            when(employee.getDepartment()).thenReturn(department);
            when(employee.getPosition()).thenReturn(position);

            // Act
            EmployeeResponse response1 = new EmployeeResponse(employee);
            EmployeeResponse response2 = new EmployeeResponse(employee);

            // Assert
            assertThat(response1.getId(), is(equalTo(response2.getId())));
            assertThat(response1.getName(), is(equalTo(response2.getName())));
            assertThat(response1.getEmail(), is(equalTo(response2.getEmail())));
            assertThat(response1.getHireDate(), is(equalTo(response2.getHireDate())));
            assertThat(response1.getEmployeeStatus(), is(equalTo(response2.getEmployeeStatus())));
            // Note: Department and Position responses are new instances, so we don't compare them
        }

        @Test
        @DisplayName("Should handle extreme date values")
        void shouldHandleExtremeDateValues() {
            // Arrange
            LocalDate[] extremeDates = {
                LocalDate.MIN,
                LocalDate.MAX,
                LocalDate.of(1900, 1, 1),
                LocalDate.of(2100, 12, 31)
            };

            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(null);
            when(employee.getStatus()).thenReturn(null);
            when(employee.getDepartment()).thenReturn(null);
            when(employee.getPosition()).thenReturn(null);

            for (LocalDate extremeDate : extremeDates) {
                // Arrange
                when(employee.getHireDate()).thenReturn(extremeDate);

                // Act
                EmployeeResponse response = new EmployeeResponse(employee);

                // Assert
                assertThat(response.getHireDate(), is(equalTo(extremeDate)));
            }
        }

        @Test
        @DisplayName("Should handle very long name values")
        void shouldHandleVeryLongNameValues() {
            // Arrange
            String longName = "Very Long Employee Name ".repeat(100);
            
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(longName);
            when(employee.getEmail()).thenReturn(null);
            when(employee.getHireDate()).thenReturn(hireDate);
            when(employee.getStatus()).thenReturn(null);
            when(employee.getDepartment()).thenReturn(null);
            when(employee.getPosition()).thenReturn(null);

            // Act
            EmployeeResponse response = new EmployeeResponse(employee);

            // Assert
            assertThat(response.getName(), is(equalTo(longName)));
        }
    }
}
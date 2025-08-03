package com.itau.hr.people_management.unit.application.employee.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.application.department.dto.DepartmentResponse;
import com.itau.hr.people_management.application.employee.dto.EmployeeResponse;
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

    @Test
    @DisplayName("Should create response with complete employee data")
    void shouldCreateResponseWithCompleteEmployeeData() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        String name = "John Doe";
        String emailAddress = "john.doe@example.com";
        
        when(employee.getId()).thenReturn(employeeId);
        when(employee.getName()).thenReturn(name);
        when(employee.getEmail()).thenReturn(email);
        when(email.getAddress()).thenReturn(emailAddress);
        when(employee.getStatus()).thenReturn(EmployeeStatus.ACTIVE);
        when(employee.getDepartment()).thenReturn(department);
        when(employee.getPosition()).thenReturn(position);

        // Act
        EmployeeResponse response = new EmployeeResponse(employee);

        // Assert
        assertThat(response.getId(), is(employeeId));
        assertThat(response.getName(), is(name));
        assertThat(response.getEmail(), is(emailAddress));
        assertThat(response.getEmployeeStatus(), is("ACTIVE"));
        assertThat(response.getDepartment(), is(instanceOf(DepartmentResponse.class)));
        assertThat(response.getPosition(), is(instanceOf(PositionResponse.class)));
    }

    @Test
    @DisplayName("Should handle null email object")
    void shouldHandleNullEmailObject() {
        // Arrange
        when(employee.getId()).thenReturn(UUID.randomUUID());
        when(employee.getName()).thenReturn("John Doe");
        when(employee.getEmail()).thenReturn(null);
        when(employee.getStatus()).thenReturn(EmployeeStatus.ACTIVE);
        when(employee.getDepartment()).thenReturn(null);
        when(employee.getPosition()).thenReturn(null);

        // Act
        EmployeeResponse response = new EmployeeResponse(employee);

        // Assert
        assertThat(response.getEmail(), is(nullValue()));
    }

    @Test
    @DisplayName("Should handle null status")
    void shouldHandleNullStatus() {
        // Arrange
        when(employee.getId()).thenReturn(UUID.randomUUID());
        when(employee.getName()).thenReturn("John Doe");
        when(employee.getEmail()).thenReturn(null);
        when(employee.getStatus()).thenReturn(null);
        when(employee.getDepartment()).thenReturn(null);
        when(employee.getPosition()).thenReturn(null);

        // Act
        EmployeeResponse response = new EmployeeResponse(employee);

        // Assert
        assertThat(response.getEmployeeStatus(), is(nullValue()));
    }

    @Test
    @DisplayName("Should handle null department and position")
    void shouldHandleNullDepartmentAndPosition() {
        // Arrange
        when(employee.getId()).thenReturn(UUID.randomUUID());
        when(employee.getName()).thenReturn("John Doe");
        when(employee.getEmail()).thenReturn(null);
        when(employee.getStatus()).thenReturn(null);
        when(employee.getDepartment()).thenReturn(null);
        when(employee.getPosition()).thenReturn(null);

        // Act
        EmployeeResponse response = new EmployeeResponse(employee);

        // Assert
        assertThat(response.getDepartment(), is(nullValue()));
        assertThat(response.getPosition(), is(nullValue()));
    }

    @Test
    @DisplayName("Should throw exception when employee is null")
    void shouldThrowExceptionWhenEmployeeIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new EmployeeResponse(null);
        });
        
        assertThat(exception.getMessage(), is("Employee cannot be null"));
    }

    @Test
    @DisplayName("Should convert status to string using name method")
    void shouldConvertStatusToStringUsingNameMethod() {
        // Arrange
        when(employee.getId()).thenReturn(UUID.randomUUID());
        when(employee.getName()).thenReturn("John Doe");
        when(employee.getEmail()).thenReturn(null);
        when(employee.getStatus()).thenReturn(EmployeeStatus.TERMINATED);
        when(employee.getDepartment()).thenReturn(null);
        when(employee.getPosition()).thenReturn(null);

        // Act
        EmployeeResponse response = new EmployeeResponse(employee);

        // Assert
        assertThat(response.getEmployeeStatus(), is("TERMINATED"));
    }
}
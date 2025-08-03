package com.itau.hr.people_management.unit.application.department.dto;

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
import com.itau.hr.people_management.domain.department.entity.Department;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentResponse Unit Tests")
class DepartmentResponseTest {

    @Mock
    private Department department;

    @Test
    @DisplayName("Should create response from department entity")
    void shouldCreateResponseFromDepartmentEntity() {
        // Arrange
        UUID id = UUID.randomUUID();
        String name = "IT Department";
        String code = "IT001";
        
        when(department.getId()).thenReturn(id);
        when(department.getName()).thenReturn(name);
        when(department.getCostCenterCode()).thenReturn(code);

        // Act
        DepartmentResponse response = new DepartmentResponse(department);

        // Assert
        assertThat(response.getId(), is(id));
        assertThat(response.getName(), is(name));
        assertThat(response.getCostCenterCode(), is(code));
    }

    @Test
    @DisplayName("Should throw exception when department is null")
    void shouldThrowExceptionWhenDepartmentIsNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            new DepartmentResponse(null);
        });
    }

    @Test
    @DisplayName("Should handle null values from department")
    void shouldHandleNullValuesFromDepartment() {
        // Arrange
        when(department.getId()).thenReturn(null);
        when(department.getName()).thenReturn(null);
        when(department.getCostCenterCode()).thenReturn(null);

        // Act
        DepartmentResponse response = new DepartmentResponse(department);

        // Assert
        assertThat(response.getId(), is(nullValue()));
        assertThat(response.getName(), is(nullValue()));
        assertThat(response.getCostCenterCode(), is(nullValue()));
    }
}
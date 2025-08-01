package com.itau.hr.people_management.unit.application.department.usecase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.application.department.dto.CreateDepartmentRequest;
import com.itau.hr.people_management.application.department.dto.DepartmentResponse;
import com.itau.hr.people_management.application.department.usecase.CreateDepartmentUseCase;
import com.itau.hr.people_management.domain.department.entity.Department;
import com.itau.hr.people_management.domain.department.repository.DepartmentRepository;
import com.itau.hr.people_management.domain.shared.exception.ConflictException;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateDepartmentUseCase Unit Tests")
class CreateDepartmentUseCaseTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private Department savedDepartment;

    private CreateDepartmentUseCase useCase;
    private CreateDepartmentRequest request;

    @BeforeEach
    void setUp() {
        useCase = new CreateDepartmentUseCase(departmentRepository);
        request = new CreateDepartmentRequest();
        request.setName("IT Department");
        request.setCostCenterCode("IT001");
    }

    @Test
    @DisplayName("Should create department when cost center code is unique")
    void shouldCreateDepartmentWhenCostCenterCodeIsUnique() {
        // Arrange
        UUID departmentId = UUID.randomUUID();
        when(departmentRepository.findByCostCenterCode("IT001")).thenReturn(Optional.empty());
        when(departmentRepository.save(any(Department.class))).thenReturn(savedDepartment);
        when(savedDepartment.getId()).thenReturn(departmentId);
        when(savedDepartment.getName()).thenReturn("IT Department");
        when(savedDepartment.getCostCenterCode()).thenReturn("IT001");

        // Act
        DepartmentResponse response = useCase.execute(request);

        // Assert
        assertThat(response.getId(), is(departmentId));
        assertThat(response.getName(), is("IT Department"));
        assertThat(response.getCostCenterCode(), is("IT001"));
        verify(departmentRepository).findByCostCenterCode("IT001");
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    @DisplayName("Should throw ConflictException when cost center code already exists")
    void shouldThrowConflictExceptionWhenCostCenterCodeAlreadyExists() {
        // Arrange
        Department existingDepartment = mock(Department.class);
        when(departmentRepository.findByCostCenterCode("IT001")).thenReturn(Optional.of(existingDepartment));

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            useCase.execute(request);
        });

        assertThat(exception.getMessageKey(), is("error.department.costcenter.exists"));
        assertThat(exception.getArgs(), is(arrayContaining("IT001")));
        verify(departmentRepository, never()).save(any(Department.class));
    }

    @Test
    @DisplayName("Should create department with generated UUID")
    void shouldCreateDepartmentWithGeneratedUuid() {
        // Arrange
        when(departmentRepository.findByCostCenterCode("IT001")).thenReturn(Optional.empty());
        when(departmentRepository.save(any(Department.class))).thenReturn(savedDepartment);
        when(savedDepartment.getId()).thenReturn(UUID.randomUUID());
        when(savedDepartment.getName()).thenReturn("IT Department");
        when(savedDepartment.getCostCenterCode()).thenReturn("IT001");

        // Act
        DepartmentResponse response = useCase.execute(request);

        // Assert
        assertThat(response.getId(), is(notNullValue()));
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    @DisplayName("Should return response based on saved department")
    void shouldReturnResponseBasedOnSavedDepartment() {
        // Arrange
        UUID savedId = UUID.randomUUID();
        when(departmentRepository.findByCostCenterCode("IT001")).thenReturn(Optional.empty());
        when(departmentRepository.save(any(Department.class))).thenReturn(savedDepartment);
        when(savedDepartment.getId()).thenReturn(savedId);
        when(savedDepartment.getName()).thenReturn("Saved Department");
        when(savedDepartment.getCostCenterCode()).thenReturn("SAVED001");

        // Act
        DepartmentResponse response = useCase.execute(request);

        // Assert
        assertThat(response.getId(), is(savedId));
        assertThat(response.getName(), is("Saved Department"));
        assertThat(response.getCostCenterCode(), is("SAVED001"));
    }
}
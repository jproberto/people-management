package com.itau.hr.people_management.unit.application.department.usecase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.application.department.dto.DepartmentResponse;
import com.itau.hr.people_management.application.department.usecase.GetDepartmentUseCase;
import com.itau.hr.people_management.domain.department.entity.Department;
import com.itau.hr.people_management.domain.department.repository.DepartmentRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetDepartmentUseCase Unit Tests")
class GetDepartmentUseCaseTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private Department department;

    private GetDepartmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetDepartmentUseCase(departmentRepository);
    }

    @Test
    @DisplayName("Should return empty list when no departments exist")
    void shouldReturnEmptyListWhenNoDepartmentsExist() {
        // Arrange
        when(departmentRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<DepartmentResponse> result = useCase.getAll();

        // Assert
        assertThat(result, is(empty()));
        verify(departmentRepository).findAll();
    }

    @Test
    @DisplayName("Should return mapped department responses")
    void shouldReturnMappedDepartmentResponses() {
        // Arrange
        UUID departmentId = UUID.randomUUID();
        when(department.getId()).thenReturn(departmentId);
        when(department.getName()).thenReturn("IT Department");
        when(department.getCostCenterCode()).thenReturn("IT001");
        when(departmentRepository.findAll()).thenReturn(List.of(department));

        // Act
        List<DepartmentResponse> result = useCase.getAll();

        // Assert
        assertThat(result, hasSize(1));
        DepartmentResponse response = result.get(0);
        assertThat(response.getId(), is(departmentId));
        assertThat(response.getName(), is("IT Department"));
        assertThat(response.getCostCenterCode(), is("IT001"));
    }

    @Test
    @DisplayName("Should preserve order from repository")
    void shouldPreserveOrderFromRepository() {
        // Arrange
        Department dept1 = mock(Department.class);
        Department dept2 = mock(Department.class);
        
        when(dept1.getId()).thenReturn(UUID.randomUUID());
        when(dept1.getName()).thenReturn("First");
        when(dept1.getCostCenterCode()).thenReturn("FIRST");
        
        when(dept2.getId()).thenReturn(UUID.randomUUID());
        when(dept2.getName()).thenReturn("Second");
        when(dept2.getCostCenterCode()).thenReturn("SECOND");
        
        when(departmentRepository.findAll()).thenReturn(List.of(dept1, dept2));

        // Act
        List<DepartmentResponse> result = useCase.getAll();

        // Assert
        assertThat(result, hasSize(2));
        assertThat(result.get(0).getName(), is("First"));
        assertThat(result.get(1).getName(), is("Second"));
    }
}
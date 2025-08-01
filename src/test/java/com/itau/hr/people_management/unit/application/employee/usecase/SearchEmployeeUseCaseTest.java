package com.itau.hr.people_management.unit.application.employee.usecase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.application.employee.dto.EmployeeResponse;
import com.itau.hr.people_management.application.employee.usecase.SearchEmployeeUseCase;
import com.itau.hr.people_management.domain.employee.criteria.EmployeeSearchCriteria;
import com.itau.hr.people_management.domain.employee.entity.Employee;
import com.itau.hr.people_management.domain.employee.repository.EmployeeRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchEmployeeUseCase Unit Tests")
class SearchEmployeeUseCaseTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeSearchCriteria searchCriteria;

    @Mock
    private Employee employee;

    private SearchEmployeeUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SearchEmployeeUseCase(employeeRepository);
    }

    @Test
    @DisplayName("Should return mapped employee responses")
    void shouldReturnMappedEmployeeResponses() {
        // Arrange
        when(employeeRepository.search(searchCriteria)).thenReturn(List.of(employee));

        // Act
        List<EmployeeResponse> result = useCase.execute(searchCriteria);

        // Assert
        assertThat(result, hasSize(1));
        assertThat(result.get(0), is(instanceOf(EmployeeResponse.class)));
        verify(employeeRepository).search(searchCriteria);
    }

    @Test
    @DisplayName("Should return empty list when no employees found")
    void shouldReturnEmptyListWhenNoEmployeesFound() {
        // Arrange
        when(employeeRepository.search(searchCriteria)).thenReturn(Collections.emptyList());

        // Act
        List<EmployeeResponse> result = useCase.execute(searchCriteria);

        // Assert
        assertThat(result, is(empty()));
        verify(employeeRepository).search(searchCriteria);
    }

    @Test
    @DisplayName("Should handle null search criteria")
    void shouldHandleNullSearchCriteria() {
        // Arrange
        when(employeeRepository.search((EmployeeSearchCriteria) null)).thenReturn(List.of(employee));

        // Act
        List<EmployeeResponse> result = useCase.execute(null);

        // Assert
        assertThat(result, hasSize(1));
        verify(employeeRepository).search(null);
    }

    @Test
    @DisplayName("Should throw exception when employee mapping fails")
    void shouldThrowExceptionWhenEmployeeMappingFails() {
        // Arrange
        List<Employee> employees = new ArrayList<>();
        employees.add(null);
        when(employeeRepository.search(searchCriteria)).thenReturn(employees);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            useCase.execute(searchCriteria);
        });
    }

    @Test
    @DisplayName("Should preserve order from repository")
    void shouldPreserveOrderFromRepository() {
        // Arrange
        Employee employee1 = mock(Employee.class);
        Employee employee2 = mock(Employee.class);
        when(employeeRepository.search(searchCriteria)).thenReturn(List.of(employee1, employee2));

        // Act
        List<EmployeeResponse> result = useCase.execute(searchCriteria);

        // Assert
        assertThat(result, hasSize(2));
        verify(employeeRepository).search(searchCriteria);
    }
}
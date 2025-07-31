package com.itau.hr.people_management.application.employee.usecase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.application.employee.dto.EmployeeResponse;
import com.itau.hr.people_management.domain.employee.entity.Employee;
import com.itau.hr.people_management.domain.employee.repository.EmployeeRepository;
import com.itau.hr.people_management.domain.shared.exception.NotFoundException;
import com.itau.hr.people_management.domain.shared.message.DomainMessageSource;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetEmployeeUseCase Unit Tests")
class GetEmployeeUseCaseTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DomainMessageSource messageSource;

    @Mock
    private Employee employee;

    private GetEmployeeUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetEmployeeUseCase(employeeRepository, messageSource);
    }

    @Test
    @DisplayName("Should get employee by ID successfully")
    void shouldGetEmployeeByIdSuccessfully() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        when(employee.getId()).thenReturn(employeeId);

        // Act
        EmployeeResponse response = useCase.getById(employeeId);

        // Assert
        assertThat(response.getId(), is(employeeId));
        verify(employeeRepository).findById(employeeId);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when ID is null")
    void shouldThrowIllegalArgumentExceptionWhenIdIsNull() {
        // Arrange
        String errorMessage = "Employee ID cannot be null";
        when(messageSource.getMessage("validation.employee.id.null")).thenReturn(errorMessage);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            useCase.getById(null);
        });

        assertThat(exception.getMessage(), is(errorMessage));
        verify(employeeRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw NotFoundException when employee does not exist")
    void shouldThrowNotFoundExceptionWhenEmployeeDoesNotExist() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            useCase.getById(employeeId);
        });

        assertThat(exception.getMessageKey(), containsString("error.employee.notfound"));
        assertThat(exception.getArgs()[0], is(employeeId));
    }

    @Test
    @DisplayName("Should return all employees successfully")
    void shouldReturnAllEmployeesSuccessfully() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        when(employee.getId()).thenReturn(employeeId);
        when(employeeRepository.findAll()).thenReturn(List.of(employee));

        // Act
        List<EmployeeResponse> responses = useCase.getAll();

        // Assert
        assertThat(responses, hasSize(1));
        assertThat(responses.get(0).getId(), is(employeeId));
        verify(employeeRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no employees exist")
    void shouldReturnEmptyListWhenNoEmployeesExist() {
        // Arrange
        when(employeeRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<EmployeeResponse> responses = useCase.getAll();

        // Assert
        assertThat(responses, is(empty()));
        verify(employeeRepository).findAll();
    }
}
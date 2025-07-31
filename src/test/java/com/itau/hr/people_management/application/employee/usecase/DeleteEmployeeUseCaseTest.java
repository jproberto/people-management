package com.itau.hr.people_management.application.employee.usecase;

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

import com.itau.hr.people_management.domain.employee.entity.Employee;
import com.itau.hr.people_management.domain.employee.repository.EmployeeRepository;
import com.itau.hr.people_management.domain.shared.exception.NotFoundException;
import com.itau.hr.people_management.domain.shared.message.DomainMessageSource;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteEmployeeUseCase Unit Tests")
class DeleteEmployeeUseCaseTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DomainMessageSource messageSource;

    @Mock
    private Employee employee;

    private DeleteEmployeeUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteEmployeeUseCase(employeeRepository, messageSource);
    }

    @Test
    @DisplayName("Should delete employee successfully")
    void shouldDeleteEmployeeSuccessfully() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        // Act
        useCase.execute(employeeId);

        // Assert
        verify(employeeRepository).findById(employeeId);
        verify(employeeRepository).delete(employee);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when ID is null")
    void shouldThrowIllegalArgumentExceptionWhenIdIsNull() {
        // Arrange
        String errorMessage = "Employee ID cannot be null";
        when(messageSource.getMessage("validation.employee.id.null")).thenReturn(errorMessage);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            useCase.execute(null);
        });

        assertThat(exception.getMessage(), is(errorMessage));
        verify(employeeRepository, never()).findById(any());
        verify(employeeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw NotFoundException when employee does not exist")
    void shouldThrowNotFoundExceptionWhenEmployeeDoesNotExist() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            useCase.execute(employeeId);
        });

        assertThat(exception.getMessageKey(), containsString("error.employee.notfound"));
        assertThat(exception.getArgs()[0], is(employeeId));
        verify(employeeRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should not delete when findById throws exception")
    void shouldNotDeleteWhenFindByIdThrowsException() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        when(employeeRepository.findById(employeeId)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            useCase.execute(employeeId);
        });

        verify(employeeRepository, never()).delete(any());
    }
}
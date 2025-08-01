package com.itau.hr.people_management.unit.application.employee.usecase;

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
import org.springframework.context.ApplicationEventPublisher;

import com.itau.hr.people_management.application.employee.usecase.ReactivateEmployeeUseCase;
import com.itau.hr.people_management.domain.employee.entity.Employee;
import com.itau.hr.people_management.domain.employee.repository.EmployeeRepository;
import com.itau.hr.people_management.domain.shared.exception.NotFoundException;
import com.itau.hr.people_management.domain.shared.message.DomainMessageSource;
import com.itau.hr.people_management.infrastructure.outbox.listener.TransactionCompletedEvent;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReactivateEmployeeUseCase Unit Tests")
class ReactivateEmployeeUseCaseTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private DomainMessageSource messageSource;

    @Mock
    private Employee employee;

    private ReactivateEmployeeUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ReactivateEmployeeUseCase(employeeRepository, eventPublisher, messageSource);
    }

    @Test
    @DisplayName("Should reactivate employee successfully")
    void shouldReactivateEmployeeSuccessfully() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        // Act
        useCase.execute(employeeId);

        // Assert
        verify(employeeRepository).findById(employeeId);
        verify(employee).reactivate();
        verify(employeeRepository).save(employee);
        verify(eventPublisher).publishEvent(any(TransactionCompletedEvent.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when employeeId is null")
    void shouldThrowIllegalArgumentExceptionWhenEmployeeIdIsNull() {
        // Arrange
        String expectedMessage = "Employee ID cannot be null";
        when(messageSource.getMessage("validation.employee.id.null")).thenReturn(expectedMessage);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            useCase.execute(null);
        });

        assertThat(exception.getMessage(), is(expectedMessage));
        verify(employeeRepository, never()).findById(any());
        verify(employeeRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
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
        verify(employeeRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Should not publish event when save fails")
    void shouldNotPublishEventWhenSaveFails() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        doThrow(new RuntimeException("Save failed")).when(employeeRepository).save(employee);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            useCase.execute(employeeId);
        });

        verify(employee).reactivate();
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Should not save when reactivate fails")
    void shouldNotSaveWhenReactivateFails() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        doThrow(new RuntimeException("Invalid reactivation")).when(employee).reactivate();

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            useCase.execute(employeeId);
        });

        verify(employeeRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}
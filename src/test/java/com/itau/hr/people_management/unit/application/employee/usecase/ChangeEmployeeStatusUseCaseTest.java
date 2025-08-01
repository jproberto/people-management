package com.itau.hr.people_management.unit.application.employee.usecase;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.itau.hr.people_management.application.employee.usecase.ChangeEmployeeStatusUseCase;
import com.itau.hr.people_management.domain.employee.entity.Employee;
import com.itau.hr.people_management.domain.employee.enumeration.EmployeeStatus;
import com.itau.hr.people_management.domain.employee.repository.EmployeeRepository;
import com.itau.hr.people_management.domain.shared.exception.NotFoundException;
import com.itau.hr.people_management.domain.shared.message.DomainMessageSource;
import com.itau.hr.people_management.infrastructure.outbox.listener.TransactionCompletedEvent;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChangeEmployeeStatusUseCase Unit Tests")
class ChangeEmployeeStatusUseCaseTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private DomainMessageSource messageSource;

    @Mock
    private Employee employee;

    private ChangeEmployeeStatusUseCase useCase;
    private UUID employeeId;

    @BeforeEach
    void setUp() {
        useCase = new ChangeEmployeeStatusUseCase(employeeRepository, eventPublisher, messageSource);
        employeeId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should change employee status successfully")
    void shouldChangeEmployeeStatusSuccessfully() {
        // Arrange
        EmployeeStatus newStatus = EmployeeStatus.ON_LEAVE;
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

        // Act
        useCase.execute(employeeId, newStatus);

        // Assert
        verify(employeeRepository).findById(employeeId);
        verify(employee).changeStatus(newStatus);
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
            useCase.execute(null, EmployeeStatus.ON_LEAVE);
        });

        assertThat(exception.getMessage(), is(expectedMessage));
        verify(employeeRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when newStatus is null")
    void shouldThrowIllegalArgumentExceptionWhenNewStatusIsNull() {
        // Arrange
        String expectedMessage = "Employee status cannot be null";
        when(messageSource.getMessage("validation.employee.status.null")).thenReturn(expectedMessage);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            useCase.execute(employeeId, null);
        });

        assertThat(exception.getMessage(), is(expectedMessage));
        verify(employeeRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw NotFoundException when employee does not exist")
    void shouldThrowNotFoundExceptionWhenEmployeeDoesNotExist() {
        // Arrange
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            useCase.execute(employeeId, EmployeeStatus.ON_LEAVE);
        });

        assertThat(exception.getMessageKey(), containsString("error.employee.notfound"));
        verify(employeeRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Should not publish event when save fails")
    void shouldNotPublishEventWhenSaveFails() {
        // Arrange
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        doThrow(new RuntimeException("Save failed")).when(employeeRepository).save(employee);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            useCase.execute(employeeId, EmployeeStatus.ON_LEAVE);
        });

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Should not save when changeStatus fails")
    void shouldNotSaveWhenChangeStatusFails() {
        // Arrange
        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
        doThrow(new RuntimeException("Invalid transition")).when(employee).changeStatus(any());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            useCase.execute(employeeId, EmployeeStatus.ON_LEAVE);
        });

        verify(employeeRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }
}
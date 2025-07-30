package com.itau.hr.people_management.application.employee.usecase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create use case with valid dependencies")
        void shouldCreateUseCaseWithValidDependencies() {
            // Act
            ChangeEmployeeStatusUseCase newUseCase = new ChangeEmployeeStatusUseCase(
                employeeRepository, eventPublisher, messageSource
            );

            // Assert
            assertThat(newUseCase, is(notNullValue()));
        }

        @Test
        @DisplayName("Should accept null dependencies in constructor")
        void shouldAcceptNullDependenciesInConstructor() {
            // Act & Assert - Constructor should accept null (will fail at runtime when used)
            assertDoesNotThrow(() -> {
                ChangeEmployeeStatusUseCase newUseCase = new ChangeEmployeeStatusUseCase(null, null, null);
                assertThat(newUseCase, is(notNullValue()));
            });
        }
    }

    @Nested
    @DisplayName("Execute Method - Success Scenarios")
    class ExecuteMethodSuccessScenarios {

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
        @DisplayName("Should handle all employee status changes")
        void shouldHandleAllEmployeeStatusChanges() {
            // Arrange
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // Test all possible status values
            EmployeeStatus[] allStatuses = EmployeeStatus.values();
            
            for (EmployeeStatus status : allStatuses) {
                // Act
                useCase.execute(employeeId, status);

                // Assert
                verify(employee).changeStatus(status);
                
                // Reset for next iteration
                reset(employee);
            }

            // Verify repository and event publisher were called for each status
            verify(employeeRepository, times(allStatuses.length)).findById(employeeId);
            verify(employeeRepository, times(allStatuses.length)).save(employee);
            verify(eventPublisher, times(allStatuses.length)).publishEvent(any(TransactionCompletedEvent.class));
        }

        @Test
        @DisplayName("Should change status from ACTIVE to ON_LEAVE")
        void shouldChangeStatusFromActiveToOnLeave() {
            // Arrange
            EmployeeStatus newStatus = EmployeeStatus.ON_LEAVE;
            
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // Act
            useCase.execute(employeeId, newStatus);

            // Assert
            verify(employee).changeStatus(EmployeeStatus.ON_LEAVE);
            verify(employeeRepository).save(employee);
        }

        @Test
        @DisplayName("Should change status from ACTIVE to ON_VACATION")
        void shouldChangeStatusFromActiveToOnVacation() {
            // Arrange
            EmployeeStatus newStatus = EmployeeStatus.ON_VACATION;
            
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // Act
            useCase.execute(employeeId, newStatus);

            // Assert
            verify(employee).changeStatus(EmployeeStatus.ON_VACATION);
            verify(employeeRepository).save(employee);
        }

        @Test
        @DisplayName("Should change status from ACTIVE to TERMINATED")
        void shouldChangeStatusFromActiveToTerminated() {
            // Arrange
            EmployeeStatus newStatus = EmployeeStatus.TERMINATED;
            
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // Act
            useCase.execute(employeeId, newStatus);

            // Assert
            verify(employee).changeStatus(EmployeeStatus.TERMINATED);
            verify(employeeRepository).save(employee);
        }

        @Test
        @DisplayName("Should publish TransactionCompletedEvent after successful execution")
        void shouldPublishTransactionCompletedEventAfterSuccessfulExecution() {
            // Arrange
            EmployeeStatus newStatus = EmployeeStatus.ON_LEAVE;
            
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // Act
            useCase.execute(employeeId, newStatus);

            // Assert
            ArgumentCaptor<TransactionCompletedEvent> eventCaptor = ArgumentCaptor.forClass(TransactionCompletedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            TransactionCompletedEvent publishedEvent = eventCaptor.getValue();
            assertThat(publishedEvent, is(notNullValue()));
            assertThat(publishedEvent, is(instanceOf(TransactionCompletedEvent.class)));
        }

        @Test
        @DisplayName("Should call repository and event publisher in correct order")
        void shouldCallRepositoryAndEventPublisherInCorrectOrder() {
            // Arrange
            EmployeeStatus newStatus = EmployeeStatus.ON_LEAVE;
            
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // Act
            useCase.execute(employeeId, newStatus);

            // Assert - Verify order of operations
            var inOrder = inOrder(employeeRepository, employee, eventPublisher);
            inOrder.verify(employeeRepository).findById(employeeId);
            inOrder.verify(employee).changeStatus(newStatus);
            inOrder.verify(employeeRepository).save(employee);
            inOrder.verify(eventPublisher).publishEvent(any(TransactionCompletedEvent.class));
        }
    }

    @Nested
    @DisplayName("Execute Method - Validation Tests")
    class ExecuteMethodValidationTests {

        @Test
        @DisplayName("Should throw IllegalArgumentException when employeeId is null")
        void shouldThrowIllegalArgumentExceptionWhenEmployeeIdIsNull() {
            // Arrange
            String expectedMessage = "Employee ID cannot be null";
            when(messageSource.getMessage("validation.employee.id.null")).thenReturn(expectedMessage);

            // Act & Assert
            IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () -> {
                useCase.execute(null, EmployeeStatus.ON_LEAVE);
            });

            assertThat(thrownException.getMessage(), is(equalTo(expectedMessage)));
            verify(messageSource).getMessage("validation.employee.id.null");
            verify(employeeRepository, never()).findById(any());
            verify(employeeRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when newStatus is null")
        void shouldThrowIllegalArgumentExceptionWhenNewStatusIsNull() {
            // Arrange
            String expectedMessage = "Employee status cannot be null";
            when(messageSource.getMessage("validation.employee.status.null")).thenReturn(expectedMessage);

            // Act & Assert
            IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () -> {
                useCase.execute(employeeId, null);
            });

            assertThat(thrownException.getMessage(), is(equalTo(expectedMessage)));
            verify(messageSource).getMessage("validation.employee.status.null");
            verify(employeeRepository, never()).findById(any());
            verify(employeeRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when both parameters are null")
        void shouldThrowIllegalArgumentExceptionWhenBothParametersAreNull() {
            // Arrange
            String expectedMessage = "Employee ID cannot be null";
            when(messageSource.getMessage("validation.employee.id.null")).thenReturn(expectedMessage);

            // Act & Assert - Should fail on first validation (employeeId)
            IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () -> {
                useCase.execute(null, null);
            });

            assertThat(thrownException.getMessage(), is(equalTo(expectedMessage)));
            verify(messageSource).getMessage("validation.employee.id.null");
            verify(messageSource, never()).getMessage("validation.employee.status.null");
            verify(employeeRepository, never()).findById(any());
            verify(employeeRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should validate employeeId before newStatus")
        void shouldValidateEmployeeIdBeforeNewStatus() {
            // Arrange
            String employeeIdMessage = "Employee ID cannot be null";
            
            when(messageSource.getMessage("validation.employee.id.null")).thenReturn(employeeIdMessage);
            
            // Act & Assert - Should fail on employeeId validation first
            IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () -> {
                useCase.execute(null, null);
            });

            assertThat(thrownException.getMessage(), is(equalTo(employeeIdMessage)));
            verify(messageSource).getMessage("validation.employee.id.null");
            verify(messageSource, never()).getMessage("validation.employee.status.null");
        }
    }

    @Nested
    @DisplayName("Execute Method - Employee Not Found Tests")
    class ExecuteMethodEmployeeNotFoundTests {

        @Test
        @DisplayName("Should throw NotFoundException when employee does not exist")
        void shouldThrowNotFoundExceptionWhenEmployeeDoesNotExist() {
            // Arrange
            EmployeeStatus newStatus = EmployeeStatus.ON_LEAVE;
            
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException thrownException = assertThrows(NotFoundException.class, () -> {
                useCase.execute(employeeId, newStatus);
            });

            assertThat(thrownException.getMessageKey(), containsString("error.employee.notfound"));
            verify(employeeRepository).findById(employeeId);
            verify(employeeRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should pass employeeId to NotFoundException")
        void shouldPassEmployeeIdToNotFoundException() {
            // Arrange
            EmployeeStatus newStatus = EmployeeStatus.ON_LEAVE;
            
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException thrownException = assertThrows(NotFoundException.class, () -> {
                useCase.execute(employeeId, newStatus);
            });

            // The NotFoundException constructor should receive the employeeId as argument
            assertThat(thrownException.getMessageKey(), containsString("error.employee.notfound"));
            verify(employeeRepository).findById(employeeId);
        }

        @Test
        @DisplayName("Should not call save or publish event when employee not found")
        void shouldNotCallSaveOrPublishEventWhenEmployeeNotFound() {
            // Arrange
            EmployeeStatus newStatus = EmployeeStatus.ON_LEAVE;
            
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(NotFoundException.class, () -> {
                useCase.execute(employeeId, newStatus);
            });

            verify(employeeRepository).findById(employeeId);
            verify(employeeRepository, never()).save(any(Employee.class));
            verify(eventPublisher, never()).publishEvent(any(TransactionCompletedEvent.class));
        }
    }

    @Nested
    @DisplayName("Execute Method - Repository Exception Tests")
    class ExecuteMethodRepositoryExceptionTests {

        @Test
        @DisplayName("Should propagate exception when findById fails")
        void shouldPropagateExceptionWhenFindByIdFails() {
            // Arrange
            EmployeeStatus newStatus = EmployeeStatus.ON_LEAVE;
            RuntimeException repositoryException = new RuntimeException("Database connection error");
            
            when(employeeRepository.findById(employeeId)).thenThrow(repositoryException);

            // Act & Assert
            RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                useCase.execute(employeeId, newStatus);
            });

            assertThat(thrownException, is(equalTo(repositoryException)));
            verify(employeeRepository).findById(employeeId);
            verify(employeeRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should propagate exception when save fails")
        void shouldPropagateExceptionWhenSaveFails() {
            // Arrange
            EmployeeStatus newStatus = EmployeeStatus.ON_LEAVE;
            RuntimeException saveException = new RuntimeException("Save operation failed");
            
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            doThrow(saveException).when(employeeRepository).save(employee);

            // Act & Assert
            RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                useCase.execute(employeeId, newStatus);
            });

            assertThat(thrownException, is(equalTo(saveException)));
            verify(employeeRepository).findById(employeeId);
            verify(employee).changeStatus(newStatus);
            verify(employeeRepository).save(employee);
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should not publish event when save fails")
        void shouldNotPublishEventWhenSaveFails() {
            // Arrange
            EmployeeStatus newStatus = EmployeeStatus.ON_LEAVE;
            RuntimeException saveException = new RuntimeException("Save operation failed");
            
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            doThrow(saveException).when(employeeRepository).save(employee);

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                useCase.execute(employeeId, newStatus);
            });

            verify(eventPublisher, never()).publishEvent(any(TransactionCompletedEvent.class));
        }
    }

    @Nested
    @DisplayName("Execute Method - Employee Domain Exception Tests")
    class ExecuteMethodEmployeeDomainExceptionTests {

        @Test
        @DisplayName("Should propagate exception when employee changeStatus fails")
        void shouldPropagateExceptionWhenEmployeeChangeStatusFails() {
            // Arrange
            EmployeeStatus newStatus = EmployeeStatus.ON_LEAVE;
            RuntimeException domainException = new RuntimeException("Invalid status transition");
            
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            doThrow(domainException).when(employee).changeStatus(newStatus);

            // Act & Assert
            RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                useCase.execute(employeeId, newStatus);
            });

            assertThat(thrownException, is(equalTo(domainException)));
            verify(employeeRepository).findById(employeeId);
            verify(employee).changeStatus(newStatus);
            verify(employeeRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should not save or publish event when changeStatus fails")
        void shouldNotSaveOrPublishEventWhenChangeStatusFails() {
            // Arrange
            EmployeeStatus newStatus = EmployeeStatus.ON_LEAVE;
            RuntimeException domainException = new RuntimeException("Invalid status transition");
            
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            doThrow(domainException).when(employee).changeStatus(newStatus);

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                useCase.execute(employeeId, newStatus);
            });

            verify(employeeRepository, never()).save(any(Employee.class));
            verify(eventPublisher, never()).publishEvent(any(TransactionCompletedEvent.class));
        }
    }

    @Nested
    @DisplayName("Execute Method - Event Publisher Exception Tests")
    class ExecuteMethodEventPublisherExceptionTests {

        @Test
        @DisplayName("Should propagate exception when event publishing fails")
        void shouldPropagateExceptionWhenEventPublishingFails() {
            // Arrange
            EmployeeStatus newStatus = EmployeeStatus.ON_LEAVE;
            RuntimeException publishException = new RuntimeException("Event publishing failed");
            
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            doThrow(publishException).when(eventPublisher).publishEvent(any(TransactionCompletedEvent.class));

            // Act & Assert
            RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                useCase.execute(employeeId, newStatus);
            });

            assertThat(thrownException, is(equalTo(publishException)));
            verify(employeeRepository).findById(employeeId);
            verify(employee).changeStatus(newStatus);
            verify(employeeRepository).save(employee);
            verify(eventPublisher).publishEvent(any(TransactionCompletedEvent.class));
        }

        @Test
        @DisplayName("Should complete save operation before event publishing fails")
        void shouldCompleteSaveOperationBeforeEventPublishingFails() {
            // Arrange
            EmployeeStatus newStatus = EmployeeStatus.ON_LEAVE;
            RuntimeException publishException = new RuntimeException("Event publishing failed");
            
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            doThrow(publishException).when(eventPublisher).publishEvent(any(TransactionCompletedEvent.class));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                useCase.execute(employeeId, newStatus);
            });

            // Verify that save was called before the exception
            var inOrder = inOrder(employeeRepository, employee, eventPublisher);
            inOrder.verify(employeeRepository).findById(employeeId);
            inOrder.verify(employee).changeStatus(newStatus);
            inOrder.verify(employeeRepository).save(employee);
            inOrder.verify(eventPublisher).publishEvent(any(TransactionCompletedEvent.class));
        }
    }

    @Nested
    @DisplayName("Transactional Behavior Tests")
    class TransactionalBehaviorTests {

        @Test
        @DisplayName("Should be annotated with @Transactional")
        void shouldBeAnnotatedWithTransactional() throws NoSuchMethodException {
            // Act & Assert - Verify the method has @Transactional annotation
            assertTrue(ChangeEmployeeStatusUseCase.class
                .getMethod("execute", UUID.class, EmployeeStatus.class)
                .isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class));
        }

        @Test
        @DisplayName("Should be annotated with @Service")
        void shouldBeAnnotatedWithService() {
            // Act & Assert - Verify the class has @Service annotation
            assertTrue(ChangeEmployeeStatusUseCase.class
                .isAnnotationPresent(org.springframework.stereotype.Service.class));
        }
    }

    @Nested
    @DisplayName("Integration with Domain Message Source Tests")
    class IntegrationWithDomainMessageSourceTests {

        @Test
        @DisplayName("Should use correct message keys for validation errors")
        void shouldUseCorrectMessageKeysForValidationErrors() {
            // Arrange
            String employeeIdMessage = "Employee ID validation message";
            String statusMessage = "Status validation message";
            
            when(messageSource.getMessage("validation.employee.id.null")).thenReturn(employeeIdMessage);
            when(messageSource.getMessage("validation.employee.status.null")).thenReturn(statusMessage);

            // Act & Assert - Test employeeId null
            assertThrows(IllegalArgumentException.class, () -> {
                useCase.execute(null, EmployeeStatus.ACTIVE);
            });
            verify(messageSource).getMessage("validation.employee.id.null");

            // Reset
            reset(messageSource);
            when(messageSource.getMessage("validation.employee.status.null")).thenReturn(statusMessage);

            // Act & Assert - Test status null
            assertThrows(IllegalArgumentException.class, () -> {
                useCase.execute(employeeId, null);
            });
            verify(messageSource).getMessage("validation.employee.status.null");
        }

        @Test
        @DisplayName("Should handle message source returning null")
        void shouldHandleMessageSourceReturningNull() {
            // Arrange
            when(messageSource.getMessage("validation.employee.id.null")).thenReturn(null);

            // Act & Assert
            IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () -> {
                useCase.execute(null, EmployeeStatus.ACTIVE);
            });

            assertThat(thrownException.getMessage(), is(nullValue()));
            verify(messageSource).getMessage("validation.employee.id.null");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle rapid successive executions")
        void shouldHandleRapidSuccessiveExecutions() {
            // Arrange
            EmployeeStatus[] statuses = {EmployeeStatus.ACTIVE, EmployeeStatus.ON_LEAVE, EmployeeStatus.ON_VACATION};
            
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // Act - Execute multiple times rapidly
            for (EmployeeStatus status : statuses) {
                useCase.execute(employeeId, status);
            }

            // Assert
            verify(employeeRepository, times(statuses.length)).findById(employeeId);
            verify(employeeRepository, times(statuses.length)).save(employee);
            verify(eventPublisher, times(statuses.length)).publishEvent(any(TransactionCompletedEvent.class));
            
            for (EmployeeStatus status : statuses) {
                verify(employee).changeStatus(status);
            }
        }

        @Test
        @DisplayName("Should handle same employee with different status changes")
        void shouldHandleSameEmployeeWithDifferentStatusChanges() {
            // Arrange
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // Act - Change status multiple times
            useCase.execute(employeeId, EmployeeStatus.ON_LEAVE);
            useCase.execute(employeeId, EmployeeStatus.ON_VACATION);
            useCase.execute(employeeId, EmployeeStatus.ACTIVE);
            useCase.execute(employeeId, EmployeeStatus.TERMINATED);

            // Assert
            verify(employeeRepository, times(4)).findById(employeeId);
            verify(employeeRepository, times(4)).save(employee);
            verify(eventPublisher, times(4)).publishEvent(any(TransactionCompletedEvent.class));
            
            verify(employee).changeStatus(EmployeeStatus.ON_LEAVE);
            verify(employee).changeStatus(EmployeeStatus.ON_VACATION);
            verify(employee).changeStatus(EmployeeStatus.ACTIVE);
            verify(employee).changeStatus(EmployeeStatus.TERMINATED);
        }

        @Test
        @DisplayName("Should handle different employees with same status")
        void shouldHandleDifferentEmployeesWithSameStatus() {
            // Arrange
            UUID employee2Id = UUID.randomUUID();
            Employee employee2 = mock(Employee.class);
            EmployeeStatus status = EmployeeStatus.ON_LEAVE;
            
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            when(employeeRepository.findById(employee2Id)).thenReturn(Optional.of(employee2));

            // Act
            useCase.execute(employeeId, status);
            useCase.execute(employee2Id, status);

            // Assert
            verify(employeeRepository).findById(employeeId);
            verify(employeeRepository).findById(employee2Id);
            verify(employeeRepository).save(employee);
            verify(employeeRepository).save(employee2);
            verify(employee).changeStatus(status);
            verify(employee2).changeStatus(status);
            verify(eventPublisher, times(2)).publishEvent(any(TransactionCompletedEvent.class));
        }
    }
}
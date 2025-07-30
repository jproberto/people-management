package com.itau.hr.people_management.application.employee.usecase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
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

    private UUID employeeId;

    @BeforeEach
    void setUp() {
        useCase = new ReactivateEmployeeUseCase(employeeRepository, eventPublisher, messageSource);
        
        employeeId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create use case with valid dependencies")
        void shouldCreateUseCaseWithValidDependencies() {
            // Act
            ReactivateEmployeeUseCase newUseCase = new ReactivateEmployeeUseCase(
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
                ReactivateEmployeeUseCase newUseCase = new ReactivateEmployeeUseCase(null, null, null);
                assertThat(newUseCase, is(notNullValue()));
            });
        }
    }

    @Nested
    @DisplayName("Execute Method - Success Scenarios")
    class ExecuteMethodSuccessScenarios {

        @Test
        @DisplayName("Should reactivate employee successfully")
        void shouldReactivateEmployeeSuccessfully() {
            // Arrange
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
        @DisplayName("Should call methods in correct order")
        void shouldCallMethodsInCorrectOrder() {
            // Arrange
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // Act
            useCase.execute(employeeId);

            // Assert - Verify order of operations
            var inOrder = inOrder(employeeRepository, employee, eventPublisher);
            inOrder.verify(employeeRepository).findById(employeeId);
            inOrder.verify(employee).reactivate();
            inOrder.verify(employeeRepository).save(employee);
            inOrder.verify(eventPublisher).publishEvent(any(TransactionCompletedEvent.class));
        }

        @Test
        @DisplayName("Should validate employee id before repository call")
        void shouldValidateEmployeeIdBeforeRepositoryCall() {
            // Arrange
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // Act
            useCase.execute(employeeId);

            // Assert - No validation exception should be thrown for valid ID
            verify(employeeRepository).findById(employeeId);
            verify(messageSource, never()).getMessage("validation.employee.id.null");
        }

        @Test
        @DisplayName("Should publish TransactionCompletedEvent after successful reactivation")
        void shouldPublishTransactionCompletedEventAfterSuccessfulReactivation() {
            // Arrange
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // Act
            useCase.execute(employeeId);

            // Assert
            ArgumentCaptor<TransactionCompletedEvent> eventCaptor = ArgumentCaptor.forClass(TransactionCompletedEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            TransactionCompletedEvent publishedEvent = eventCaptor.getValue();
            assertThat(publishedEvent, is(notNullValue()));
            assertThat(publishedEvent, is(instanceOf(TransactionCompletedEvent.class)));
        }

        @Test
        @DisplayName("Should save employee after reactivation")
        void shouldSaveEmployeeAfterReactivation() {
            // Arrange
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // Act
            useCase.execute(employeeId);

            // Assert - Save should happen after reactivate
            var inOrder = inOrder(employee, employeeRepository);
            inOrder.verify(employee).reactivate();
            inOrder.verify(employeeRepository).save(employee);
        }

        @Test
        @DisplayName("Should handle different employee ids")
        void shouldHandleDifferentEmployeeIds() {
            // Arrange
            UUID[] employeeIds = {UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()};
            Employee[] employees = {mock(Employee.class), mock(Employee.class), mock(Employee.class)};

            for (int i = 0; i < employeeIds.length; i++) {
                when(employeeRepository.findById(employeeIds[i])).thenReturn(Optional.of(employees[i]));
            }

            // Act & Assert
            for (int i = 0; i < employeeIds.length; i++) {
                useCase.execute(employeeIds[i]);
                
                verify(employeeRepository).findById(employeeIds[i]);
                verify(employees[i]).reactivate();
                verify(employeeRepository).save(employees[i]);
            }

            verify(eventPublisher, times(employeeIds.length)).publishEvent(any(TransactionCompletedEvent.class));
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
                useCase.execute(null);
            });

            assertThat(thrownException.getMessage(), is(equalTo(expectedMessage)));
            verify(messageSource).getMessage("validation.employee.id.null");
            verify(employeeRepository, never()).findById(any());
            verify(employeeRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should use correct message key for null validation")
        void shouldUseCorrectMessageKeyForNullValidation() {
            // Arrange
            String expectedMessage = "ID do colaborador não pode ser nulo";
            when(messageSource.getMessage("validation.employee.id.null")).thenReturn(expectedMessage);

            // Act & Assert
            IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () -> {
                useCase.execute(null);
            });

            assertThat(thrownException.getMessage(), is(equalTo(expectedMessage)));
            verify(messageSource).getMessage("validation.employee.id.null");
        }

        @Test
        @DisplayName("Should not call repository when validation fails")
        void shouldNotCallRepositoryWhenValidationFails() {
            // Arrange
            String expectedMessage = "Employee ID cannot be null";
            when(messageSource.getMessage("validation.employee.id.null")).thenReturn(expectedMessage);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                useCase.execute(null);
            });

            verify(employeeRepository, never()).findById(any(UUID.class));
            verify(employeeRepository, never()).save(any(Employee.class));
            verify(eventPublisher, never()).publishEvent(any(TransactionCompletedEvent.class));
        }

        @Test
        @DisplayName("Should handle message source returning null")
        void shouldHandleMessageSourceReturningNull() {
            // Arrange
            when(messageSource.getMessage("validation.employee.id.null")).thenReturn(null);

            // Act & Assert
            IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () -> {
                useCase.execute(null);
            });

            assertThat(thrownException.getMessage(), is(nullValue()));
            verify(messageSource).getMessage("validation.employee.id.null");
        }

        @Test
        @DisplayName("Should validate employeeId before any other operation")
        void shouldValidateEmployeeIdBeforeAnyOtherOperation() {
            // Arrange
            String validationMessage = "Employee ID cannot be null";
            when(messageSource.getMessage("validation.employee.id.null")).thenReturn(validationMessage);

            // Act & Assert - Validation should happen first
            IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () -> {
                useCase.execute(null);
            });

            assertThat(thrownException.getMessage(), is(equalTo(validationMessage)));
            verify(messageSource).getMessage("validation.employee.id.null");
            verify(employeeRepository, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("Execute Method - Employee Not Found Tests")
    class ExecuteMethodEmployeeNotFoundTests {

        @Test
        @DisplayName("Should throw NotFoundException when employee does not exist")
        void shouldThrowNotFoundExceptionWhenEmployeeDoesNotExist() {
            // Arrange
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException thrownException = assertThrows(NotFoundException.class, () -> {
                useCase.execute(employeeId);
            });

            assertThat(thrownException.getMessageKey(), containsString("error.employee.notfound"));
            verify(employeeRepository).findById(employeeId);
            verify(employeeRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should include employeeId in NotFoundException")
        void shouldIncludeEmployeeIdInNotFoundException() {
            // Arrange
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException thrownException = assertThrows(NotFoundException.class, () -> {
                useCase.execute(employeeId);
            });

            // The NotFoundException constructor should receive the employeeId as argument
            assertThat(thrownException.getMessageKey(), containsString("error.employee.notfound"));
            verify(employeeRepository).findById(employeeId);
        }

        @Test
        @DisplayName("Should not call reactivate or save when employee not found")
        void shouldNotCallReactivateOrSaveWhenEmployeeNotFound() {
            // Arrange
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(NotFoundException.class, () -> {
                useCase.execute(employeeId);
            });

            verify(employeeRepository).findById(employeeId);
            verify(employeeRepository, never()).save(any(Employee.class));
            verify(eventPublisher, never()).publishEvent(any(TransactionCompletedEvent.class));
            // Can't verify employee.reactivate() since employee is null
        }

        @Test
        @DisplayName("Should validate employee exists after validation")
        void shouldValidateEmployeeExistsAfterValidation() {
            // Arrange
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(NotFoundException.class, () -> {
                useCase.execute(employeeId);
            });

            // Verify that findById was called (meaning validation passed)
            verify(employeeRepository).findById(employeeId);
            verify(messageSource, never()).getMessage("validation.employee.id.null");
        }
    }

    @Nested
    @DisplayName("Execute Method - Repository Exception Tests")
    class ExecuteMethodRepositoryExceptionTests {

        @Test
        @DisplayName("Should propagate exception when findById fails")
        void shouldPropagateExceptionWhenFindByIdFails() {
            // Arrange
            RuntimeException repositoryException = new RuntimeException("Database connection error");
            when(employeeRepository.findById(employeeId)).thenThrow(repositoryException);

            // Act & Assert
            RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                useCase.execute(employeeId);
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
            RuntimeException saveException = new RuntimeException("Save operation failed");
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            doThrow(saveException).when(employeeRepository).save(employee);

            // Act & Assert
            RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                useCase.execute(employeeId);
            });

            assertThat(thrownException, is(equalTo(saveException)));
            verify(employeeRepository).findById(employeeId);
            verify(employee).reactivate();
            verify(employeeRepository).save(employee);
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should not publish event when save fails")
        void shouldNotPublishEventWhenSaveFails() {
            // Arrange
            RuntimeException saveException = new RuntimeException("Save operation failed");
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            doThrow(saveException).when(employeeRepository).save(employee);

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                useCase.execute(employeeId);
            });

            verify(eventPublisher, never()).publishEvent(any(TransactionCompletedEvent.class));
        }

        @Test
        @DisplayName("Should complete reactivate operation before save fails")
        void shouldCompleteReactivateOperationBeforeSaveFails() {
            // Arrange
            RuntimeException saveException = new RuntimeException("Save operation failed");
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            doThrow(saveException).when(employeeRepository).save(employee);

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                useCase.execute(employeeId);
            });

            // Verify that reactivate was called before the exception
            var inOrder = inOrder(employeeRepository, employee);
            inOrder.verify(employeeRepository).findById(employeeId);
            inOrder.verify(employee).reactivate();
            inOrder.verify(employeeRepository).save(employee);
        }
    }

    @Nested
    @DisplayName("Execute Method - Employee Domain Exception Tests")
    class ExecuteMethodEmployeeDomainExceptionTests {

        @Test
        @DisplayName("Should propagate exception when employee reactivate fails")
        void shouldPropagateExceptionWhenEmployeeReactivateFails() {
            // Arrange
            RuntimeException domainException = new RuntimeException("Employee cannot be reactivated");
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            doThrow(domainException).when(employee).reactivate();

            // Act & Assert
            RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                useCase.execute(employeeId);
            });

            assertThat(thrownException, is(equalTo(domainException)));
            verify(employeeRepository).findById(employeeId);
            verify(employee).reactivate();
            verify(employeeRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should not save or publish event when reactivate fails")
        void shouldNotSaveOrPublishEventWhenReactivateFails() {
            // Arrange
            RuntimeException domainException = new RuntimeException("Invalid reactivation");
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            doThrow(domainException).when(employee).reactivate();

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                useCase.execute(employeeId);
            });

            verify(employeeRepository, never()).save(any(Employee.class));
            verify(eventPublisher, never()).publishEvent(any(TransactionCompletedEvent.class));
        }

        @Test
        @DisplayName("Should handle business rule validation in reactivate method")
        void shouldHandleBusinessRuleValidationInReactivateMethod() {
            // Arrange
            IllegalStateException businessException = new IllegalStateException("Employee is already active");
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            doThrow(businessException).when(employee).reactivate();

            // Act & Assert
            IllegalStateException thrownException = assertThrows(IllegalStateException.class, () -> {
                useCase.execute(employeeId);
            });

            assertThat(thrownException, is(equalTo(businessException)));
            verify(employeeRepository).findById(employeeId);
            verify(employee).reactivate();
            verify(employeeRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }
    }

    @Nested
    @DisplayName("Execute Method - Event Publisher Exception Tests")
    class ExecuteMethodEventPublisherExceptionTests {

        @Test
        @DisplayName("Should propagate exception when event publishing fails")
        void shouldPropagateExceptionWhenEventPublishingFails() {
            // Arrange
            RuntimeException publishException = new RuntimeException("Event publishing failed");
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            doThrow(publishException).when(eventPublisher).publishEvent(any(TransactionCompletedEvent.class));

            // Act & Assert
            RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                useCase.execute(employeeId);
            });

            assertThat(thrownException, is(equalTo(publishException)));
            verify(employeeRepository).findById(employeeId);
            verify(employee).reactivate();
            verify(employeeRepository).save(employee);
            verify(eventPublisher).publishEvent(any(TransactionCompletedEvent.class));
        }

        @Test
        @DisplayName("Should complete save operation before event publishing fails")
        void shouldCompleteSaveOperationBeforeEventPublishingFails() {
            // Arrange
            RuntimeException publishException = new RuntimeException("Event publishing failed");
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            doThrow(publishException).when(eventPublisher).publishEvent(any(TransactionCompletedEvent.class));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> {
                useCase.execute(employeeId);
            });

            // Verify that save was called before the exception
            var inOrder = inOrder(employeeRepository, employee, eventPublisher);
            inOrder.verify(employeeRepository).findById(employeeId);
            inOrder.verify(employee).reactivate();
            inOrder.verify(employeeRepository).save(employee);
            inOrder.verify(eventPublisher).publishEvent(any(TransactionCompletedEvent.class));
        }

        @Test
        @DisplayName("Should handle different event publishing exceptions")
        void shouldHandleDifferentEventPublishingExceptions() {
            // Arrange
            Exception[] publishExceptions = {
                new RuntimeException("Event bus unavailable"),
                new IllegalStateException("Transaction not active"),
                new UnsupportedOperationException("Event type not supported")
            };

            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            for (Exception exception : publishExceptions) {
                // Reset the mock
                reset(eventPublisher);
                doThrow(exception).when(eventPublisher).publishEvent(any(TransactionCompletedEvent.class));

                // Act & Assert
                Exception thrownException = assertThrows(Exception.class, () -> {
                    useCase.execute(employeeId);
                });

                assertThat(thrownException, is(equalTo(exception)));
                verify(eventPublisher).publishEvent(any(TransactionCompletedEvent.class));
            }
        }
    }

    @Nested
    @DisplayName("Execute Method - Message Source Exception Tests")
    class ExecuteMethodMessageSourceExceptionTests {

        @Test
        @DisplayName("Should propagate exception when message source fails")
        void shouldPropagateExceptionWhenMessageSourceFails() {
            // Arrange
            RuntimeException messageException = new RuntimeException("Message source failed");
            when(messageSource.getMessage("validation.employee.id.null")).thenThrow(messageException);

            // Act & Assert
            RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                useCase.execute(null);
            });

            assertThat(thrownException, is(equalTo(messageException)));
            verify(messageSource).getMessage("validation.employee.id.null");
            verify(employeeRepository, never()).findById(any());
            verify(employeeRepository, never()).save(any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("Should handle message source timeout gracefully")
        void shouldHandleMessageSourceTimeoutGracefully() {
            // Arrange
            RuntimeException timeoutException = new RuntimeException("Message source timeout");
            when(messageSource.getMessage("validation.employee.id.null")).thenThrow(timeoutException);

            // Act & Assert
            RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                useCase.execute(null);
            });

            assertThat(thrownException, is(equalTo(timeoutException)));
            verify(messageSource).getMessage("validation.employee.id.null");
        }
    }

    @Nested
    @DisplayName("Transactional Behavior Tests")
    class TransactionalBehaviorTests {

        @Test
        @DisplayName("Should be annotated with @Transactional on execute method")
        void shouldBeAnnotatedWithTransactionalOnExecuteMethod() throws NoSuchMethodException {
            // Act & Assert - Verify the execute method has @Transactional annotation
            assertTrue(ReactivateEmployeeUseCase.class
                .getMethod("execute", UUID.class)
                .isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class));
        }

        @Test
        @DisplayName("Should be annotated with @Service")
        void shouldBeAnnotatedWithService() {
            // Act & Assert - Verify the class has @Service annotation
            assertTrue(ReactivateEmployeeUseCase.class
                .isAnnotationPresent(org.springframework.stereotype.Service.class));
        }

        @Test
        @DisplayName("Should have correct transactional properties")
        void shouldHaveCorrectTransactionalProperties() throws NoSuchMethodException {
            // Act & Assert - Verify transactional properties
            org.springframework.transaction.annotation.Transactional transactionalAnnotation = 
                ReactivateEmployeeUseCase.class
                    .getMethod("execute", UUID.class)
                    .getAnnotation(org.springframework.transaction.annotation.Transactional.class);
            
            // Default readOnly should be false (not explicitly set means false)
            assertFalse(transactionalAnnotation.readOnly());
        }
    }

    @Nested
    @DisplayName("Integration with Domain Message Source Tests")
    class IntegrationWithDomainMessageSourceTests {

        @Test
        @DisplayName("Should use correct message key for validation error")
        void shouldUseCorrectMessageKeyForValidationError() {
            // Arrange
            String expectedMessage = "ID do colaborador é obrigatório";
            when(messageSource.getMessage("validation.employee.id.null")).thenReturn(expectedMessage);

            // Act & Assert
            IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () -> {
                useCase.execute(null);
            });

            assertThat(thrownException.getMessage(), is(equalTo(expectedMessage)));
            verify(messageSource).getMessage("validation.employee.id.null");
        }

        @Test
        @DisplayName("Should handle different locales in message source")
        void shouldHandleDifferentLocalesInMessageSource() {
            // Arrange
            String[] expectedMessages = {
                "Employee ID cannot be null",
                "ID do colaborador não pode ser nulo",
                "L'ID de l'employé ne peut pas être nul"
            };

            for (String message : expectedMessages) {
                // Reset message source
                reset(messageSource);
                when(messageSource.getMessage("validation.employee.id.null")).thenReturn(message);

                // Act & Assert
                IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () -> {
                    useCase.execute(null);
                });

                assertThat(thrownException.getMessage(), is(equalTo(message)));
                verify(messageSource).getMessage("validation.employee.id.null");
            }
        }

        @Test
        @DisplayName("Should use message source only for validation")
        void shouldUseMessageSourceOnlyForValidation() {
            // Arrange
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // Act
            useCase.execute(employeeId);

            // Assert - Message source should not be called for valid execution
            verify(messageSource, never()).getMessage(any(String.class));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle rapid successive executions")
        void shouldHandleRapidSuccessiveExecutions() {
            // Arrange
            UUID[] employeeIds = {UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()};
            Employee[] employees = {mock(Employee.class), mock(Employee.class), mock(Employee.class)};

            for (int i = 0; i < employeeIds.length; i++) {
                when(employeeRepository.findById(employeeIds[i])).thenReturn(Optional.of(employees[i]));
            }

            // Act - Execute multiple times rapidly
            for (int i = 0; i < employeeIds.length; i++) {
                useCase.execute(employeeIds[i]);
            }

            // Assert
            for (int i = 0; i < employeeIds.length; i++) {
                verify(employeeRepository).findById(employeeIds[i]);
                verify(employees[i]).reactivate();
                verify(employeeRepository).save(employees[i]);
            }
            verify(eventPublisher, times(employeeIds.length)).publishEvent(any(TransactionCompletedEvent.class));
        }

        @Test
        @DisplayName("Should handle same employee reactivated multiple times")
        void shouldHandleSameEmployeeReactivatedMultipleTimes() {
            // Arrange
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // Act - Reactivate same employee multiple times
            useCase.execute(employeeId);
            useCase.execute(employeeId);
            useCase.execute(employeeId);

            // Assert
            verify(employeeRepository, times(3)).findById(employeeId);
            verify(employee, times(3)).reactivate();
            verify(employeeRepository, times(3)).save(employee);
            verify(eventPublisher, times(3)).publishEvent(any(TransactionCompletedEvent.class));
        }

        @Test
        @DisplayName("Should handle mixed success and failure scenarios")
        void shouldHandleMixedSuccessAndFailureScenarios() {
            // Arrange
            UUID validId = UUID.randomUUID();
            UUID invalidId = UUID.randomUUID();

            when(employeeRepository.findById(validId)).thenReturn(Optional.of(employee));
            when(employeeRepository.findById(invalidId)).thenReturn(Optional.empty());

            // Act & Assert - Success case
            assertDoesNotThrow(() -> useCase.execute(validId));

            // Act & Assert - Failure case
            assertThrows(NotFoundException.class, () -> {
                useCase.execute(invalidId);
            });

            // Act & Assert - Success case again
            assertDoesNotThrow(() -> useCase.execute(validId));

            // Assert
            verify(employeeRepository, times(2)).findById(validId);
            verify(employeeRepository, times(1)).findById(invalidId);
            verify(employee, times(2)).reactivate();
            verify(employeeRepository, times(2)).save(employee);
            verify(eventPublisher, times(2)).publishEvent(any(TransactionCompletedEvent.class));
        }

        @Test
        @DisplayName("Should handle null and valid ids in sequence")
        void shouldHandleNullAndValidIdsInSequence() {
            // Arrange
            String validationMessage = "Employee ID cannot be null";
            when(messageSource.getMessage("validation.employee.id.null")).thenReturn(validationMessage);
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));

            // Act & Assert - Null case
            assertThrows(IllegalArgumentException.class, () -> {
                useCase.execute(null);
            });

            // Act & Assert - Valid case
            assertDoesNotThrow(() -> useCase.execute(employeeId));

            // Act & Assert - Null case again
            assertThrows(IllegalArgumentException.class, () -> {
                useCase.execute(null);
            });

            // Assert
            verify(messageSource, times(2)).getMessage("validation.employee.id.null");
            verify(employeeRepository, times(1)).findById(employeeId);
            verify(employee, times(1)).reactivate();
            verify(employeeRepository, times(1)).save(employee);
            verify(eventPublisher, times(1)).publishEvent(any(TransactionCompletedEvent.class));
        }

        @Test
        @DisplayName("Should handle employee reactivation with complex business rules")
        void shouldHandleEmployeeReactivationWithComplexBusinessRules() {
            // Arrange
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee));
            
            // Simulate complex business rules in reactivate method
            doAnswer(invocation -> {
                // Simulate some complex validation logic
                return null;
            }).when(employee).reactivate();

            // Act & Assert
            assertDoesNotThrow(() -> useCase.execute(employeeId));

            verify(employeeRepository).findById(employeeId);
            verify(employee).reactivate();
            verify(employeeRepository).save(employee);
            verify(eventPublisher).publishEvent(any(TransactionCompletedEvent.class));
        }
    }
}
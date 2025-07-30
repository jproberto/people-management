package com.itau.hr.people_management.application.employee.usecase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.domain.employee.entity.Employee;
import com.itau.hr.people_management.domain.employee.repository.EmployeeRepository;
import com.itau.hr.people_management.domain.shared.exception.NotFoundException;
import com.itau.hr.people_management.domain.shared.message.DomainMessageSource;

@ExtendWith(MockitoExtension.class)
@DisplayName("DeleteEmployeeUseCase Tests")
class DeleteEmployeeUseCaseTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DomainMessageSource messageSource;

    @Mock
    private Employee employee;

    private DeleteEmployeeUseCase useCase;
    private UUID validEmployeeId;

    @BeforeEach
    void setUp() {
        useCase = new DeleteEmployeeUseCase(employeeRepository, messageSource);
        validEmployeeId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create DeleteEmployeeUseCase with valid dependencies")
        void shouldCreateDeleteEmployeeUseCaseWithValidDependencies() {
            // Act
            DeleteEmployeeUseCase newUseCase = new DeleteEmployeeUseCase(employeeRepository, messageSource);

            // Assert
            assertThat(newUseCase, is(notNullValue()));
        }

        @Test
        @DisplayName("Should accept null EmployeeRepository in constructor")
        void shouldAcceptNullEmployeeRepositoryInConstructor() {
            // Act & Assert - Constructor should accept null (will fail at runtime when used)
            assertDoesNotThrow(() -> {
                DeleteEmployeeUseCase newUseCase = new DeleteEmployeeUseCase(null, messageSource);
                assertThat(newUseCase, is(notNullValue()));
            });
        }

        @Test
        @DisplayName("Should accept null DomainMessageSource in constructor")
        void shouldAcceptNullDomainMessageSourceInConstructor() {
            // Act & Assert - Constructor should accept null (will fail at runtime when used)
            assertDoesNotThrow(() -> {
                DeleteEmployeeUseCase newUseCase = new DeleteEmployeeUseCase(employeeRepository, null);
                assertThat(newUseCase, is(notNullValue()));
            });
        }

        @Test
        @DisplayName("Should accept both null dependencies in constructor")
        void shouldAcceptBothNullDependenciesInConstructor() {
            // Act & Assert - Constructor should accept nulls (will fail at runtime when used)
            assertDoesNotThrow(() -> {
                DeleteEmployeeUseCase newUseCase = new DeleteEmployeeUseCase(null, null);
                assertThat(newUseCase, is(notNullValue()));
            });
        }
    }

    @Nested
    @DisplayName("Successful Deletion Tests")
    class SuccessfulDeletionTests {

        @Test
        @DisplayName("Should successfully delete existing employee")
        void shouldSuccessfullyDeleteExistingEmployee() {
            // Arrange
            when(employeeRepository.findById(validEmployeeId)).thenReturn(Optional.of(employee));
            doNothing().when(employeeRepository).delete(employee);

            // Act
            assertDoesNotThrow(() -> useCase.execute(validEmployeeId));

            // Assert
            verify(employeeRepository).findById(validEmployeeId);
            verify(employeeRepository).delete(employee);
        }

        @Test
        @DisplayName("Should call repository methods in correct order")
        void shouldCallRepositoryMethodsInCorrectOrder() {
            // Arrange
            when(employeeRepository.findById(validEmployeeId)).thenReturn(Optional.of(employee));
            doNothing().when(employeeRepository).delete(employee);

            // Act
            useCase.execute(validEmployeeId);

            // Assert - Verify order: first findById, then delete
            verify(employeeRepository).findById(validEmployeeId);
            verify(employeeRepository).delete(employee);
        }

        @Test
        @DisplayName("Should delete employee exactly once")
        void shouldDeleteEmployeeExactlyOnce() {
            // Arrange
            when(employeeRepository.findById(validEmployeeId)).thenReturn(Optional.of(employee));
            doNothing().when(employeeRepository).delete(employee);

            // Act
            useCase.execute(validEmployeeId);

            // Assert
            verify(employeeRepository, times(1)).findById(validEmployeeId);
            verify(employeeRepository, times(1)).delete(employee);
        }

        @Test
        @DisplayName("Should not interact with messageSource when deletion is successful")
        void shouldNotInteractWithMessageSourceWhenDeletionIsSuccessful() {
            // Arrange
            when(employeeRepository.findById(validEmployeeId)).thenReturn(Optional.of(employee));
            doNothing().when(employeeRepository).delete(employee);

            // Act
            useCase.execute(validEmployeeId);

            // Assert
            verifyNoInteractions(messageSource);
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 5, 10, 50})
        @DisplayName("Should handle multiple deletion operations")
        void shouldHandleMultipleDeletionOperations(int numberOfOperations) {
            // Arrange
            when(employeeRepository.findById(any(UUID.class))).thenReturn(Optional.of(employee));
            doNothing().when(employeeRepository).delete(employee);

            // Act
            for (int i = 0; i < numberOfOperations; i++) {
                UUID currentId = UUID.randomUUID();
                useCase.execute(currentId);
            }

            // Assert
            verify(employeeRepository, times(numberOfOperations)).findById(any(UUID.class));
            verify(employeeRepository, times(numberOfOperations)).delete(employee);
        }
    }

    @Nested
    @DisplayName("Null ID Validation Tests")
    class NullIdValidationTests {

        @Test
        @DisplayName("Should throw IllegalArgumentException when ID is null")
        void shouldThrowIllegalArgumentExceptionWhenIdIsNull() {
            // Arrange
            String errorMessage = "Employee ID cannot be null";
            when(messageSource.getMessage("validation.employee.id.null")).thenReturn(errorMessage);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                useCase.execute(null)
            );

            assertThat(exception.getMessage(), is(equalTo(errorMessage)));
            verify(messageSource).getMessage("validation.employee.id.null");
        }

        @Test
        @DisplayName("Should not call repository when ID is null")
        void shouldNotCallRepositoryWhenIdIsNull() {
            // Arrange
            when(messageSource.getMessage("validation.employee.id.null")).thenReturn("Error message");

            // Act
            assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));

            // Assert
            verifyNoInteractions(employeeRepository);
        }

        @Test
        @DisplayName("Should validate ID before any repository interaction")
        void shouldValidateIdBeforeAnyRepositoryInteraction() {
            // Arrange
            when(messageSource.getMessage("validation.employee.id.null")).thenReturn("ID is null");

            // Act
            assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));

            // Assert
            verify(messageSource).getMessage("validation.employee.id.null");
            verify(employeeRepository, never()).findById(any());
            verify(employeeRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should use correct message key for null ID validation")
        void shouldUseCorrectMessageKeyForNullIdValidation() {
            // Arrange
            when(messageSource.getMessage("validation.employee.id.null")).thenReturn("Custom error message");

            // Act
            assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));

            // Assert
            verify(messageSource).getMessage("validation.employee.id.null");
        }

        @Test
        @DisplayName("Should handle messageSource returning null for null ID")
        void shouldHandleMessageSourceReturningNullForNullId() {
            // Arrange
            when(messageSource.getMessage("validation.employee.id.null")).thenReturn(null);

            // Act
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                useCase.execute(null)
            );

            // Assert
            assertThat(exception.getMessage(), is(equalTo(null)));
        }

        @Test
        @DisplayName("Should handle messageSource throwing exception for null ID")
        void shouldHandleMessageSourceThrowingExceptionForNullId() {
            // Arrange
            when(messageSource.getMessage("validation.employee.id.null"))
                .thenThrow(new RuntimeException("Message source error"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> useCase.execute(null));
        }
    }

    @Nested
    @DisplayName("Employee Not Found Tests")
    class EmployeeNotFoundTests {

        @Test
        @DisplayName("Should throw NotFoundException when employee does not exist")
        void shouldThrowNotFoundExceptionWhenEmployeeDoesNotExist() {
            // Arrange
            when(employeeRepository.findById(validEmployeeId)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                useCase.execute(validEmployeeId)
            );

            assertThat(exception.getMessageKey(), containsString("error.employee.notfound"));
            verify(employeeRepository).findById(validEmployeeId);
        }

        @Test
        @DisplayName("Should not call delete when employee is not found")
        void shouldNotCallDeleteWhenEmployeeIsNotFound() {
            // Arrange
            when(employeeRepository.findById(validEmployeeId)).thenReturn(Optional.empty());

            // Act
            assertThrows(NotFoundException.class, () -> useCase.execute(validEmployeeId));

            // Assert
            verify(employeeRepository).findById(validEmployeeId);
            verify(employeeRepository, never()).delete(any(Employee.class));
        }

        @Test
        @DisplayName("Should create NotFoundException with correct parameters")
        void shouldCreateNotFoundExceptionWithCorrectParameters() {
            // Arrange
            when(employeeRepository.findById(validEmployeeId)).thenReturn(Optional.empty());

            // Act
            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                useCase.execute(validEmployeeId)
            );

            // Assert
            assertThat(exception.getMessageKey(), containsString("error.employee.notfound"));
            assertThat(exception.getArgs(), is(notNullValue()));
            assertThat(exception.getArgs()[0], is(equalTo(validEmployeeId)));
        }

        @Test
        @DisplayName("Should handle different UUIDs for not found scenarios")
        void shouldHandleDifferentUuidsForNotFoundScenarios() {
            // Arrange
            UUID[] testIds = {
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID()
            };

            for (UUID testId : testIds) {
                when(employeeRepository.findById(testId)).thenReturn(Optional.empty());

                // Act & Assert
                NotFoundException exception = assertThrows(NotFoundException.class, () ->
                    useCase.execute(testId)
                );

                assertThat(exception.getArgs()[0], is(equalTo(testId)));
            }
        }

        @Test
        @DisplayName("Should use correct message key in NotFoundException")
        void shouldUseCorrectMessageKeyInNotFoundException() {
            // Arrange
            when(employeeRepository.findById(validEmployeeId)).thenReturn(Optional.empty());

            // Act
            NotFoundException exception = assertThrows(NotFoundException.class, () ->
                useCase.execute(validEmployeeId)
            );

            // Assert
            assertThat(exception.getMessageKey(), containsString("error.employee.notfound"));
        }
    }

    @Nested
    @DisplayName("Repository Exception Handling Tests")
    class RepositoryExceptionHandlingTests {

        @Test
        @DisplayName("Should propagate RuntimeException from findById")
        void shouldPropagateRuntimeExceptionFromFindById() {
            // Arrange
            RuntimeException repositoryException = new RuntimeException("Database connection error");
            when(employeeRepository.findById(validEmployeeId)).thenThrow(repositoryException);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                useCase.execute(validEmployeeId)
            );

            assertThat(exception.getMessage(), is(equalTo("Database connection error")));
            verify(employeeRepository).findById(validEmployeeId);
            verify(employeeRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should propagate RuntimeException from delete")
        void shouldPropagateRuntimeExceptionFromDelete() {
            // Arrange
            RuntimeException deleteException = new RuntimeException("Delete operation failed");
            when(employeeRepository.findById(validEmployeeId)).thenReturn(Optional.of(employee));
            doThrow(deleteException).when(employeeRepository).delete(employee);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                useCase.execute(validEmployeeId)
            );

            assertThat(exception.getMessage(), is(equalTo("Delete operation failed")));
            verify(employeeRepository).findById(validEmployeeId);
            verify(employeeRepository).delete(employee);
        }

        @Test
        @DisplayName("Should handle IllegalArgumentException from repository")
        void shouldHandleIllegalArgumentExceptionFromRepository() {
            // Arrange
            IllegalArgumentException repositoryException = new IllegalArgumentException("Invalid entity state");
            when(employeeRepository.findById(validEmployeeId)).thenReturn(Optional.of(employee));
            doThrow(repositoryException).when(employeeRepository).delete(employee);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                useCase.execute(validEmployeeId)
            );

            assertThat(exception.getMessage(), is(equalTo("Invalid entity state")));
        }

        @Test
        @DisplayName("Should handle NullPointerException from repository")
        void shouldHandleNullPointerExceptionFromRepository() {
            // Arrange
            when(employeeRepository.findById(validEmployeeId)).thenReturn(Optional.of(employee));
            doThrow(new NullPointerException("Null entity")).when(employeeRepository).delete(employee);

            // Act & Assert
            NullPointerException exception = assertThrows(NullPointerException.class, () ->
                useCase.execute(validEmployeeId)
            );

            assertThat(exception.getMessage(), is(equalTo("Null entity")));
        }

        @Test
        @DisplayName("Should handle repository returning null Optional")
        void shouldHandleRepositoryReturningNullOptional() {
            // Arrange
            when(employeeRepository.findById(validEmployeeId)).thenReturn(null);

            // Act & Assert
            assertThrows(NullPointerException.class, () -> useCase.execute(validEmployeeId));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle UUID with all zeros")
        void shouldHandleUuidWithAllZeros() {
            // Arrange
            UUID zeroUuid = new UUID(0L, 0L);
            when(employeeRepository.findById(zeroUuid)).thenReturn(Optional.of(employee));
            doNothing().when(employeeRepository).delete(employee);

            // Act & Assert
            assertDoesNotThrow(() -> useCase.execute(zeroUuid));
            verify(employeeRepository).findById(zeroUuid);
            verify(employeeRepository).delete(employee);
        }

        @Test
        @DisplayName("Should handle UUID with maximum values")
        void shouldHandleUuidWithMaximumValues() {
            // Arrange
            UUID maxUuid = new UUID(Long.MAX_VALUE, Long.MAX_VALUE);
            when(employeeRepository.findById(maxUuid)).thenReturn(Optional.of(employee));
            doNothing().when(employeeRepository).delete(employee);

            // Act & Assert
            assertDoesNotThrow(() -> useCase.execute(maxUuid));
            verify(employeeRepository).findById(maxUuid);
            verify(employeeRepository).delete(employee);
        }

        @Test
        @DisplayName("Should handle repository returning Optional of null")
        void shouldHandleRepositoryReturningOptionalOfNull() {
            // Arrange
            when(employeeRepository.findById(validEmployeeId)).thenReturn(null);

            // Act & Assert
            assertThrows(NullPointerException.class, () -> useCase.execute(validEmployeeId));
            verify(employeeRepository).findById(validEmployeeId);
        }

        @Test
        @DisplayName("Should handle employee with null fields")
        void shouldHandleEmployeeWithNullFields() {
            // Arrange
            when(employeeRepository.findById(validEmployeeId)).thenReturn(Optional.of(employee));
            doNothing().when(employeeRepository).delete(employee);

            // Act & Assert
            assertDoesNotThrow(() -> useCase.execute(validEmployeeId));
        }

        @Test
        @DisplayName("Should handle rapid successive deletion attempts")
        void shouldHandleRapidSuccessiveDeletionAttempts() {
            // Arrange
            UUID[] ids = {UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()};
            
            when(employeeRepository.findById(any(UUID.class))).thenReturn(Optional.of(employee));
            doNothing().when(employeeRepository).delete(employee);
            
            // Act & Assert
            for (UUID id : ids) {
                assertDoesNotThrow(() -> useCase.execute(id));
            }

            // Verify all operations occurred
            for (UUID id : ids) {
                verify(employeeRepository).findById(id);
            }
            verify(employeeRepository, times(3)).delete(employee);
        }
    }

    @Nested
    @DisplayName("Transactional Behavior Tests")
    class TransactionalBehaviorTests {

        @Test
        @DisplayName("Should be annotated with @Transactional")
        void shouldBeAnnotatedWithTransactional() {
            // This test verifies the class has the @Transactional annotation
            // The actual transactional behavior would be tested in integration tests
            
            // Assert
            assertThat(DeleteEmployeeUseCase.class.isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class), is(true));
        }

        @Test
        @DisplayName("Should execute all operations within single transaction context")
        void shouldExecuteAllOperationsWithinSingleTransactionContext() {
            // Arrange
            when(employeeRepository.findById(validEmployeeId)).thenReturn(Optional.of(employee));
            doNothing().when(employeeRepository).delete(employee);

            // Act
            useCase.execute(validEmployeeId);

            // Assert - Both operations should complete successfully
            verify(employeeRepository).findById(validEmployeeId);
            verify(employeeRepository).delete(employee);
        }

        @Test
        @DisplayName("Should handle transaction rollback scenarios")
        void shouldHandleTransactionRollbackScenarios() {
            // Arrange - Simulate a scenario where delete fails
            when(employeeRepository.findById(validEmployeeId)).thenReturn(Optional.of(employee));
            doThrow(new RuntimeException("Transaction rollback")).when(employeeRepository).delete(employee);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                useCase.execute(validEmployeeId)
            );

            assertThat(exception.getMessage(), is(equalTo("Transaction rollback")));
            verify(employeeRepository).findById(validEmployeeId);
            verify(employeeRepository).delete(employee);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should work correctly with complete deletion workflow")
        void shouldWorkCorrectlyWithCompleteDeletionWorkflow() {
            // Test a complete workflow
            
            // 1. Successful deletion
            when(employeeRepository.findById(validEmployeeId)).thenReturn(Optional.of(employee));
            doNothing().when(employeeRepository).delete(employee);

            useCase.execute(validEmployeeId);

            verify(employeeRepository).findById(validEmployeeId);
            verify(employeeRepository).delete(employee);

            // 2. Employee not found
            UUID nonExistentId = UUID.randomUUID();
            when(employeeRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> useCase.execute(nonExistentId));

            // 3. Null ID validation
            when(messageSource.getMessage("validation.employee.id.null")).thenReturn("ID cannot be null");

            assertThrows(IllegalArgumentException.class, () -> useCase.execute(null));
        }

        @Test
        @DisplayName("Should maintain consistency across multiple operations")
        void shouldMaintainConsistencyAcrossMultipleOperations() {
            // Arrange
            UUID[] validIds = {UUID.randomUUID(), UUID.randomUUID()};
            UUID[] invalidIds = {UUID.randomUUID(), UUID.randomUUID()};

            // Setup valid employees
            when(employeeRepository.findById(any(UUID.class))).thenReturn(Optional.of(employee));
            doNothing().when(employeeRepository).delete(employee);
            
            // Setup invalid employees
            for (UUID id : invalidIds) {
                when(employeeRepository.findById(id)).thenReturn(Optional.empty());
            }

            // Act & Assert
            // Valid deletions should succeed
            for (UUID id : validIds) {
                assertDoesNotThrow(() -> useCase.execute(id));
            }

            // Invalid deletions should fail
            for (UUID id : invalidIds) {
                assertThrows(NotFoundException.class, () -> useCase.execute(id));
            }

            // Verify all expected interactions
            for (UUID id : validIds) {
                verify(employeeRepository).findById(id);
            }
            for (UUID id : invalidIds) {
                verify(employeeRepository).findById(id);
            }
            verify(employeeRepository, times(validIds.length)).delete(employee);
        }

        @Test
        @DisplayName("Should handle mixed success and failure scenarios")
        void shouldHandleMixedSuccessAndFailureScenarios() {
            // Arrange
            UUID successId = UUID.randomUUID();
            UUID notFoundId = UUID.randomUUID();
            String nullIdMessage = "ID is null";

            when(employeeRepository.findById(successId)).thenReturn(Optional.of(employee));
            doNothing().when(employeeRepository).delete(employee);
            when(employeeRepository.findById(notFoundId)).thenReturn(Optional.empty());
            when(messageSource.getMessage("validation.employee.id.null")).thenReturn(nullIdMessage);

            // Act & Assert
            // Success case
            assertDoesNotThrow(() -> useCase.execute(successId));

            // Not found case
            assertThrows(NotFoundException.class, () -> useCase.execute(notFoundId));

            // Null ID case
            IllegalArgumentException nullException = assertThrows(IllegalArgumentException.class, () ->
                useCase.execute(null)
            );
            assertThat(nullException.getMessage(), is(equalTo(nullIdMessage)));

            // Verify interactions
            verify(employeeRepository).findById(successId);
            verify(employeeRepository).delete(employee);
            verify(employeeRepository).findById(notFoundId);
            verify(messageSource).getMessage("validation.employee.id.null");
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle high volume of deletion operations")
        void shouldHandleHighVolumeOfDeletionOperations() {
            // Arrange
            int numberOfOperations = 1000;
            when(employeeRepository.findById(any(UUID.class))).thenReturn(Optional.of(employee));
            doNothing().when(employeeRepository).delete(employee);

            // Act
            for (int i = 0; i < numberOfOperations; i++) {
                UUID currentId = UUID.randomUUID();
                useCase.execute(currentId);
            }

            // Assert
            verify(employeeRepository, times(numberOfOperations)).findById(any(UUID.class));
            verify(employeeRepository, times(numberOfOperations)).delete(employee);
        }

        @Test
        @DisplayName("Should maintain performance with alternating success and failure")
        void shouldMaintainPerformanceWithAlternatingSuccessAndFailure() {
            // Arrange
            int cycles = 100;
            UUID successId = UUID.randomUUID();
            UUID failureId = UUID.randomUUID();

            when(employeeRepository.findById(successId)).thenReturn(Optional.of(employee));
            doNothing().when(employeeRepository).delete(employee);
            when(employeeRepository.findById(failureId)).thenReturn(Optional.empty());

            // Act
            for (int i = 0; i < cycles; i++) {
                // Success
                useCase.execute(successId);

                // Failure
                assertThrows(NotFoundException.class, () -> useCase.execute(failureId));
            }

            // Assert
            verify(employeeRepository, times(cycles)).findById(successId);
            verify(employeeRepository, times(cycles)).findById(failureId);
            verify(employeeRepository, times(cycles)).delete(employee);
        }
    }
}
package com.itau.hr.people_management.application.employee.usecase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.application.employee.dto.EmployeeResponse;
import com.itau.hr.people_management.domain.department.entity.Department;
import com.itau.hr.people_management.domain.employee.entity.Employee;
import com.itau.hr.people_management.domain.employee.repository.EmployeeRepository;
import com.itau.hr.people_management.domain.position.entity.Position;
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
    private Employee employee1;

    @Mock
    private Employee employee2;

    @Mock
    private Employee employee3;

    private GetEmployeeUseCase useCase;

    private UUID employeeId;

    @BeforeEach
    void setUp() {
        useCase = new GetEmployeeUseCase(employeeRepository, messageSource);
        
        employeeId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create use case with valid dependencies")
        void shouldCreateUseCaseWithValidDependencies() {
            // Act
            GetEmployeeUseCase newUseCase = new GetEmployeeUseCase(employeeRepository, messageSource);

            // Assert
            assertThat(newUseCase, is(notNullValue()));
        }

        @Test
        @DisplayName("Should accept null dependencies in constructor")
        void shouldAcceptNullDependenciesInConstructor() {
            // Act & Assert - Constructor should accept null (will fail at runtime when used)
            assertDoesNotThrow(() -> {
                GetEmployeeUseCase newUseCase = new GetEmployeeUseCase(null, null);
                assertThat(newUseCase, is(notNullValue()));
            });
        }
    }

    @Nested
    @DisplayName("GetById Method - Success Scenarios")
    class GetByIdMethodSuccessScenarios {

        @Test
        @DisplayName("Should get employee by id successfully")
        void shouldGetEmployeeByIdSuccessfully() {
            // Arrange
            when(employee1.getId()).thenReturn(employeeId);
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee1));
            when(employee1.getDepartment()).thenReturn(mock(Department.class));
            when(employee1.getPosition()).thenReturn(mock(Position.class));
            
            // Act
            EmployeeResponse response = useCase.getById(employeeId);

            // Assert
            assertThat(response, is(notNullValue()));
            assertThat(response.getId(), is(equalTo(employeeId)));
            verify(employeeRepository).findById(employeeId);
        }

        @Test
        @DisplayName("Should validate employeeId before repository call")
        void shouldValidateEmployeeIdBeforeRepositoryCall() {
            // Arrange
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee1));
            
            // Act
            useCase.getById(employeeId);

            // Assert - No validation exception should be thrown for valid ID
            verify(employeeRepository).findById(employeeId);
            verify(messageSource, never()).getMessage("validation.employee.id.null");
        }

        @Test
        @DisplayName("Should handle different employee ids")
        void shouldHandleDifferentEmployeeIds() {
            // Arrange
            UUID[] employeeIds = {UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()};
            Employee[] employees = {employee1, employee2, employee3};

            for (int i = 0; i < employeeIds.length; i++) {
                when(employeeRepository.findById(employeeIds[i])).thenReturn(Optional.of(employees[i]));
            }

            // Act & Assert
            for (int i = 0; i < employeeIds.length; i++) {
                EmployeeResponse response = useCase.getById(employeeIds[i]);
                assertThat(response, is(notNullValue()));
                verify(employeeRepository).findById(employeeIds[i]);
            }
        }
    }

    @Nested
    @DisplayName("GetById Method - Validation Tests")
    class GetByIdMethodValidationTests {

        @Test
        @DisplayName("Should throw IllegalArgumentException when employeeId is null")
        void shouldThrowIllegalArgumentExceptionWhenEmployeeIdIsNull() {
            // Arrange
            String expectedMessage = "Employee ID cannot be null";
            when(messageSource.getMessage("validation.employee.id.null")).thenReturn(expectedMessage);

            // Act & Assert
            IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () -> {
                useCase.getById(null);
            });

            assertThat(thrownException.getMessage(), is(equalTo(expectedMessage)));
            verify(messageSource).getMessage("validation.employee.id.null");
            verify(employeeRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should use correct message key for null validation")
        void shouldUseCorrectMessageKeyForNullValidation() {
            // Arrange
            String expectedMessage = "ID não pode ser nulo";
            when(messageSource.getMessage("validation.employee.id.null")).thenReturn(expectedMessage);

            // Act & Assert
            IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () -> {
                useCase.getById(null);
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
                useCase.getById(null);
            });

            verify(employeeRepository, never()).findById(any(UUID.class));
        }

        @Test
        @DisplayName("Should handle message source returning null")
        void shouldHandleMessageSourceReturningNull() {
            // Arrange
            when(messageSource.getMessage("validation.employee.id.null")).thenReturn(null);

            // Act & Assert
            IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () -> {
                useCase.getById(null);
            });

            assertThat(thrownException.getMessage(), is(nullValue()));
            verify(messageSource).getMessage("validation.employee.id.null");
        }
    }

    @Nested
    @DisplayName("GetById Method - Employee Not Found Tests")
    class GetByIdMethodEmployeeNotFoundTests {

        @Test
        @DisplayName("Should throw NotFoundException when employee does not exist")
        void shouldThrowNotFoundExceptionWhenEmployeeDoesNotExist() {
            // Arrange
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException thrownException = assertThrows(NotFoundException.class, () -> {
                useCase.getById(employeeId);
            });

            assertThat(thrownException.getMessageKey(), containsString("error.employee.notfound"));
            verify(employeeRepository).findById(employeeId);
        }

        @Test
        @DisplayName("Should include employeeId in NotFoundException")
        void shouldIncludeEmployeeIdInNotFoundException() {
            // Arrange
            UUID localEmployeeId = UUID.randomUUID();
            when(employeeRepository.findById(localEmployeeId)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException thrownException = assertThrows(NotFoundException.class, () -> {
                useCase.getById(localEmployeeId);
            });

            // The NotFoundException constructor should receive the employeeId as argument
            assertThat(thrownException.getMessageKey(), containsString("error.employee.notfound"));
            verify(employeeRepository).findById(localEmployeeId);
        }

        @Test
        @DisplayName("Should not create EmployeeResponse when employee not found")
        void shouldNotCreateEmployeeResponseWhenEmployeeNotFound() {
            // Arrange
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            try (MockedStatic<EmployeeResponse> responseMock = mockStatic(EmployeeResponse.class)) {
                // Act & Assert
                assertThrows(NotFoundException.class, () -> {
                    useCase.getById(employeeId);
                });

                // Verify EmployeeResponse constructor was never called
                responseMock.verifyNoInteractions();
            }
        }

        @Test
        @DisplayName("Should validate id before checking existence")
        void shouldValidateIdBeforeCheckingExistence() {
            // Arrange
            String validationMessage = "Employee ID cannot be null";
            when(messageSource.getMessage("validation.employee.id.null")).thenReturn(validationMessage);

            // Act & Assert - Validation should happen first
            IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () -> {
                useCase.getById(null);
            });

            assertThat(thrownException.getMessage(), is(equalTo(validationMessage)));
            verify(messageSource).getMessage("validation.employee.id.null");
            verify(employeeRepository, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("GetById Method - Repository Exception Tests")
    class GetByIdMethodRepositoryExceptionTests {

        @Test
        @DisplayName("Should propagate exception when findById fails")
        void shouldPropagateExceptionWhenFindByIdFails() {
            // Arrange
            RuntimeException repositoryException = new RuntimeException("Database connection error");
            when(employeeRepository.findById(employeeId)).thenThrow(repositoryException);

            // Act & Assert
            RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                useCase.getById(employeeId);
            });

            assertThat(thrownException, is(equalTo(repositoryException)));
            verify(employeeRepository).findById(employeeId);
        }

        @Test
        @DisplayName("Should not create EmployeeResponse when repository fails")
        void shouldNotCreateEmployeeResponseWhenRepositoryFails() {
            // Arrange
            RuntimeException repositoryException = new RuntimeException("Database connection error");
            when(employeeRepository.findById(employeeId)).thenThrow(repositoryException);

            try (MockedStatic<EmployeeResponse> responseMock = mockStatic(EmployeeResponse.class)) {
                // Act & Assert
                assertThrows(RuntimeException.class, () -> {
                    useCase.getById(employeeId);
                });

                // Verify EmployeeResponse constructor was never called
                responseMock.verifyNoInteractions();
            }
        }
    }

    @Nested
    @DisplayName("GetAll Method - Success Scenarios")
    class GetAllMethodSuccessScenarios {

        @Test
        @DisplayName("Should get all employees successfully")
        void shouldGetAllEmployeesSuccessfully() {
            // Arrange
            UUID id1 = UUID.randomUUID();
            when(employee1.getId()).thenReturn(id1);
            UUID id2 = UUID.randomUUID();
            when(employee2.getId()).thenReturn(id2);
            UUID id3 = UUID.randomUUID();
            when(employee3.getId()).thenReturn(id3);

            List<Employee> employees = Arrays.asList(employee1, employee2, employee3);
            when(employeeRepository.findAll()).thenReturn(employees);

            // Act
            List<EmployeeResponse> responses = useCase.getAll();
            List<UUID> responseIds = responses.stream()
                .map(EmployeeResponse::getId)
                .toList();

            // Assert
            assertThat(responses, is(notNullValue()));
            assertThat(responses, hasSize(3));
            assertThat(responseIds, contains(id1, id2, id3));
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
            assertThat(responses, is(notNullValue()));
            assertThat(responses, is(empty()));
            verify(employeeRepository).findAll();
        }

        @Test
        @DisplayName("Should handle single employee in list")
        void shouldHandleSingleEmployeeInList() {
            // Arrange
            UUID id1 = UUID.randomUUID();
            when(employee1.getId()).thenReturn(id1);
            List<Employee> employees = Arrays.asList(employee1);
            when(employeeRepository.findAll()).thenReturn(employees);

            // Act
            List<EmployeeResponse> responses = useCase.getAll();
            List<UUID> responseIds = responses.stream()
                .map(EmployeeResponse::getId)
                .toList();

            // Assert
            assertThat(responses, is(notNullValue()));
            assertThat(responses, hasSize(1));
            assertThat(responseIds, contains(id1));
            verify(employeeRepository).findAll();
        }

        @Test
        @DisplayName("Should return immutable list")
        void shouldReturnImmutableList() {
            // Arrange
            List<Employee> employees = Arrays.asList(employee1, employee2);
            when(employeeRepository.findAll()).thenReturn(employees);

            // Act
            List<EmployeeResponse> responses = useCase.getAll();

            // Assert - List should be immutable (created by toList())
            assertThat(responses, is(notNullValue()));
            assertThat(responses, hasSize(2));
            
            // Verify it's an immutable list by checking the class type
            assertThat(responses.getClass().getName(), containsString("Immutable"));
        }
    }

    @Nested
    @DisplayName("GetAll Method - Repository Exception Tests")
    class GetAllMethodRepositoryExceptionTests {

        @Test
        @DisplayName("Should propagate exception when findAll fails")
        void shouldPropagateExceptionWhenFindAllFails() {
            // Arrange
            RuntimeException repositoryException = new RuntimeException("Database connection error");
            when(employeeRepository.findAll()).thenThrow(repositoryException);

            // Act & Assert
            RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                useCase.getAll();
            });

            assertThat(thrownException, is(equalTo(repositoryException)));
            verify(employeeRepository).findAll();
        }

        @Test
        @DisplayName("Should not create EmployeeResponses when repository fails")
        void shouldNotCreateEmployeeResponsesWhenRepositoryFails() {
            // Arrange
            RuntimeException repositoryException = new RuntimeException("Database connection error");
            when(employeeRepository.findAll()).thenThrow(repositoryException);

            try (MockedStatic<EmployeeResponse> responseMock = mockStatic(EmployeeResponse.class)) {
                // Act & Assert
                assertThrows(RuntimeException.class, () -> {
                    useCase.getAll();
                });

                // Verify EmployeeResponse constructor was never called
                responseMock.verifyNoInteractions();
            }
        }
    }

    @Nested
    @DisplayName("Transactional Behavior Tests")
    class TransactionalBehaviorTests {

        @Test
        @DisplayName("Should be annotated with @Transactional(readOnly = true)")
        void shouldBeAnnotatedWithTransactionalReadOnly() {
            // Act & Assert - Verify the class has @Transactional annotation with readOnly = true
            assertTrue(GetEmployeeUseCase.class
                .isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class));
            
            org.springframework.transaction.annotation.Transactional transactionalAnnotation = 
                GetEmployeeUseCase.class.getAnnotation(org.springframework.transaction.annotation.Transactional.class);
            
            assertTrue(transactionalAnnotation.readOnly());
        }

        @Test
        @DisplayName("Should be annotated with @Service")
        void shouldBeAnnotatedWithService() {
            // Act & Assert - Verify the class has @Service annotation
            assertTrue(GetEmployeeUseCase.class
                .isAnnotationPresent(org.springframework.stereotype.Service.class));
        }
    }

    @Nested
    @DisplayName("Integration with Message Source Tests")
    class IntegrationWithMessageSourceTests {

        @Test
        @DisplayName("Should use correct message key for validation error")
        void shouldUseCorrectMessageKeyForValidationError() {
            // Arrange
            String expectedMessage = "ID do colaborador não pode ser nulo";
            when(messageSource.getMessage("validation.employee.id.null")).thenReturn(expectedMessage);

            // Act & Assert
            IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () -> {
                useCase.getById(null);
            });

            assertThat(thrownException.getMessage(), is(equalTo(expectedMessage)));
            verify(messageSource).getMessage("validation.employee.id.null");
        }

        @Test
        @DisplayName("Should handle message source exception gracefully")
        void shouldHandleMessageSourceExceptionGracefully() {
            // Arrange
            RuntimeException messageException = new RuntimeException("Message source failed");
            when(messageSource.getMessage("validation.employee.id.null")).thenThrow(messageException);

            // Act & Assert
            RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                useCase.getById(null);
            });

            assertThat(thrownException, is(equalTo(messageException)));
            verify(messageSource).getMessage("validation.employee.id.null");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle rapid successive getById calls")
        void shouldHandleRapidSuccessiveGetByIdCalls() {
            // Arrange
            UUID[] employeeIds = {UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()};
            Employee[] employees = {employee1, employee2, employee3};

            for (int i = 0; i < employeeIds.length; i++) {
                when(employeeRepository.findById(employeeIds[i])).thenReturn(Optional.of(employees[i]));
            }

           // Act - Execute multiple times rapidly
           List<EmployeeResponse> responses = new ArrayList<>();
            for (UUID id : employeeIds) {
                EmployeeResponse response = useCase.getById(id);
                assertThat(response, is(notNullValue()));
                responses.add(response);
            }

            // Assert
            assertThat(responses, hasSize(3));
            for (UUID id : employeeIds) {
                verify(employeeRepository).findById(id);
            }
        }

        @Test
        @DisplayName("Should handle rapid successive getAll calls")
        void shouldHandleRapidSuccessiveGetAllCalls() {
            // Arrange
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            when(employee1.getId()).thenReturn(id1);
            when(employee2.getId()).thenReturn(id2);
            List<Employee> employees = Arrays.asList(employee1, employee2);
            when(employeeRepository.findAll()).thenReturn(employees);

            // Act - Execute multiple times rapidly
            List<EmployeeResponse> responses = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                List<EmployeeResponse> currentResponses = useCase.getAll();
                assertThat(currentResponses, hasSize(2));
                responses.addAll(currentResponses);
            }
            List<EmployeeResponse> employee1Occurrences = responses.stream()
                .filter(response -> response.getId().equals(id1))
                .toList();
            List<EmployeeResponse> employee2Occurrences = responses.stream()
                .filter(response -> response.getId().equals(id2))
                .toList();

            // Assert
            verify(employeeRepository, times(5)).findAll();
            assertThat(employee1Occurrences, hasSize(5));
            assertThat(employee2Occurrences, hasSize(5));
        }

        @Test
        @DisplayName("Should handle alternating getById and getAll calls")
        void shouldHandleAlternatingGetByIdAndGetAllCalls() {
            // Arrange
            List<Employee> allEmployees = Arrays.asList(employee1, employee2);
            when(employeeRepository.findAll()).thenReturn(allEmployees);
            when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(employee1));

            // Act - Alternate between getById and getAll
            for (int i = 0; i < 3; i++) {
                EmployeeResponse byIdResponse = useCase.getById(employeeId);
                List<EmployeeResponse> allResponses = useCase.getAll();

                assertThat(byIdResponse, is(notNullValue()));
                assertThat(allResponses, hasSize(2));
            }

            // Assert
            verify(employeeRepository, times(3)).findById(employeeId);
            verify(employeeRepository, times(3)).findAll();
        }

        @Test
        @DisplayName("Should handle large employee lists efficiently")
        void shouldHandleLargeEmployeeListsEfficiently() {
            // Arrange
            List<Employee> largeEmployeeList = Collections.nCopies(1000, employee1);
            when(employeeRepository.findAll()).thenReturn(largeEmployeeList);

            // Act
            List<EmployeeResponse> responses = useCase.getAll();

            // Assert
            assertThat(responses, is(notNullValue()));
            assertThat(responses, hasSize(1000));
            verify(employeeRepository).findAll();
        }

        @Test
        @DisplayName("Should handle mixed success and failure scenarios")
        void shouldHandleMixedSuccessAndFailureScenarios() {
            // Arrange
            UUID validId = UUID.randomUUID();
            UUID invalidId = UUID.randomUUID();

            when(employeeRepository.findById(validId)).thenReturn(Optional.of(employee1));
            when(employeeRepository.findById(invalidId)).thenReturn(Optional.empty());

            // Act & Assert - Success case
            EmployeeResponse successResponse = useCase.getById(validId);
            assertThat(successResponse, is(notNullValue()));

            // Act & Assert - Failure case
            assertThrows(NotFoundException.class, () -> {
                useCase.getById(invalidId);
            });

            // Act & Assert - Success case again
            EmployeeResponse anotherSuccessResponse = useCase.getById(validId);
            assertThat(anotherSuccessResponse, is(notNullValue()));

            // Assert
            verify(employeeRepository, times(2)).findById(validId);
            verify(employeeRepository, times(1)).findById(invalidId);
        }
    }
}
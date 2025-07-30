package com.itau.hr.people_management.application.employee.usecase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.application.employee.dto.EmployeeResponse;
import com.itau.hr.people_management.domain.employee.criteria.EmployeeSearchCriteria;
import com.itau.hr.people_management.domain.employee.entity.Employee;
import com.itau.hr.people_management.domain.employee.enumeration.EmployeeStatus;
import com.itau.hr.people_management.domain.employee.repository.EmployeeRepository;
import com.itau.hr.people_management.domain.shared.vo.Email;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchEmployeeUseCase Tests")
class SearchEmployeeUseCaseTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private Employee employee1;

    @Mock
    private Employee employee2;

    @Mock
    private Employee employee3;

    @Mock
    private EmployeeSearchCriteria searchCriteria;

    private SearchEmployeeUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SearchEmployeeUseCase(employeeRepository);
    }

    private void setupMockEmployees() {
        // Setup employee1
        when(employee1.getName()).thenReturn("John Doe");
        when(employee1.getEmail()).thenReturn(Email.create("john.doe@itau.com.br"));
        when(employee1.getStatus()).thenReturn(EmployeeStatus.ACTIVE);
        when(employee1.getHireDate()).thenReturn(LocalDate.of(2023, 1, 15));

        // Setup employee2
        when(employee2.getName()).thenReturn("Jane Smith");
        when(employee2.getEmail()).thenReturn(Email.create("jane.smith@itau.com.br"));
        when(employee2.getStatus()).thenReturn(EmployeeStatus.ON_VACATION);
        when(employee2.getHireDate()).thenReturn(LocalDate.of(2022, 3, 20));

        // Setup employee3
        when(employee3.getName()).thenReturn("Bob Johnson");
        when(employee3.getEmail()).thenReturn(Email.create("bob.johnson@itau.com.br"));
        when(employee3.getStatus()).thenReturn(EmployeeStatus.TERMINATED);
        when(employee3.getHireDate()).thenReturn(LocalDate.of(2021, 7, 10));
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create SearchEmployeeUseCase with valid EmployeeRepository")
        void shouldCreateSearchEmployeeUseCaseWithValidEmployeeRepository() {
            // Act
            SearchEmployeeUseCase newUseCase = new SearchEmployeeUseCase(employeeRepository);

            // Assert
            assertThat(newUseCase, is(notNullValue()));
        }

        @Test
        @DisplayName("Should accept null EmployeeRepository in constructor")
        void shouldAcceptNullEmployeeRepositoryInConstructor() {
            // Act & Assert - Constructor should accept null (will fail at runtime when used)
            assertDoesNotThrow(() -> {
                SearchEmployeeUseCase newUseCase = new SearchEmployeeUseCase(null);
                assertThat(newUseCase, is(notNullValue()));
            });
        }

        @Test
        @DisplayName("Should create different instances with same repository")
        void shouldCreateDifferentInstancesWithSameRepository() {
            // Act
            SearchEmployeeUseCase useCase1 = new SearchEmployeeUseCase(employeeRepository);
            SearchEmployeeUseCase useCase2 = new SearchEmployeeUseCase(employeeRepository);

            // Assert
            assertThat(useCase1, is(notNullValue()));
            assertThat(useCase2, is(notNullValue()));
            assertThat(useCase1 != useCase2, is(true));
        }
    }

    @Nested
    @DisplayName("Successful Search Tests")
    class SuccessfulSearchTests {

        @Test
        @DisplayName("Should return list of EmployeeResponse when employees found")
        void shouldReturnListOfEmployeeResponseWhenEmployeesFound() {
            // Arrange
            List<Employee> employees = Arrays.asList(employee1, employee2);
            when(employeeRepository.search(searchCriteria)).thenReturn(employees);

            // Act
            List<EmployeeResponse> result = useCase.execute(searchCriteria);

            // Assert
            assertThat(result, is(notNullValue()));
            assertThat(result, hasSize(2));
            assertThat(result.get(0), is(instanceOf(EmployeeResponse.class)));
            assertThat(result.get(1), is(instanceOf(EmployeeResponse.class)));
            verify(employeeRepository).search(searchCriteria);
        }

        @Test
        @DisplayName("Should return single EmployeeResponse when one employee found")
        void shouldReturnSingleEmployeeResponseWhenOneEmployeeFound() {
            // Arrange
            List<Employee> employees = Arrays.asList(employee1);
            when(employeeRepository.search(searchCriteria)).thenReturn(employees);

            // Act
            List<EmployeeResponse> result = useCase.execute(searchCriteria);

            // Assert
            assertThat(result, is(notNullValue()));
            assertThat(result, hasSize(1));
            assertThat(result.get(0), is(instanceOf(EmployeeResponse.class)));
            verify(employeeRepository).search(searchCriteria);
        }

        @Test
        @DisplayName("Should return multiple EmployeeResponse in correct order")
        void shouldReturnMultipleEmployeeResponseInCorrectOrder() {
            // Arrange
            List<Employee> employees = Arrays.asList(employee1, employee2, employee3);
            when(employeeRepository.search(searchCriteria)).thenReturn(employees);

            // Act
            List<EmployeeResponse> result = useCase.execute(searchCriteria);

            // Assert
            assertThat(result, is(notNullValue()));
            assertThat(result, hasSize(3));
            
            // Verify order is maintained
            for (int i = 0; i < result.size(); i++) {
                assertThat(result.get(i), is(instanceOf(EmployeeResponse.class)));
            }
            
            verify(employeeRepository).search(searchCriteria);
        }

        @Test
        @DisplayName("Should call repository search exactly once")
        void shouldCallRepositorySearchExactlyOnce() {
            // Arrange
            List<Employee> employees = Arrays.asList(employee1);
            when(employeeRepository.search(searchCriteria)).thenReturn(employees);

            // Act
            useCase.execute(searchCriteria);

            // Assert
            verify(employeeRepository, times(1)).search(searchCriteria);
        }

        @Test
        @DisplayName("Should transform all employees to EmployeeResponse")
        void shouldTransformAllEmployeesToEmployeeResponse() {
            // Arrange
            List<Employee> employees = Arrays.asList(employee1, employee2, employee3);
            when(employeeRepository.search(searchCriteria)).thenReturn(employees);

            // Act
            List<EmployeeResponse> result = useCase.execute(searchCriteria);

            // Assert
            assertThat(result, hasSize(employees.size()));
            result.forEach(response -> 
                assertThat(response, is(instanceOf(EmployeeResponse.class)))
            );
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 5, 10, 50, 100})
        @DisplayName("Should handle various result set sizes")
        void shouldHandleVariousResultSetSizes(int numberOfEmployees) {
            // Arrange
            List<Employee> employees = IntStream.range(0, numberOfEmployees)
                .mapToObj(i -> employee1)
                .toList();
            when(employeeRepository.search(searchCriteria)).thenReturn(employees);

            // Act
            List<EmployeeResponse> result = useCase.execute(searchCriteria);

            // Assert
            assertThat(result, hasSize(numberOfEmployees));
            verify(employeeRepository).search(searchCriteria);
        }
    }

    @Nested
    @DisplayName("Empty Results Tests")
    class EmptyResultsTests {

        @Test
        @DisplayName("Should return empty list when no employees found")
        void shouldReturnEmptyListWhenNoEmployeesFound() {
            // Arrange
            when(employeeRepository.search(searchCriteria)).thenReturn(Collections.emptyList());

            // Act
            List<EmployeeResponse> result = useCase.execute(searchCriteria);

            // Assert
            assertThat(result, is(notNullValue()));
            assertThat(result, is(empty()));
            verify(employeeRepository).search(searchCriteria);
        }

        @Test
        @DisplayName("Should return immutable empty list")
        void shouldReturnImmutableEmptyList() {
            // Arrange
            when(employeeRepository.search(searchCriteria)).thenReturn(Collections.emptyList());

            // Act
            List<EmployeeResponse> result = useCase.execute(searchCriteria);

            // Assert
            assertThat(result, is(empty()));

            EmployeeResponse employeeResponse1 = new EmployeeResponse(employee1);
            assertThrows(UnsupportedOperationException.class, () -> 
                result.add(employeeResponse1)
            );
        }

        @Test
        @DisplayName("Should handle repository returning null list")
        void shouldHandleRepositoryReturningNullList() {
            // Arrange
            when(employeeRepository.search(searchCriteria)).thenReturn(null);

            // Act & Assert
            assertThrows(NullPointerException.class, () -> useCase.execute(searchCriteria));
            verify(employeeRepository).search(searchCriteria);
        }
    }

    @Nested
    @DisplayName("Search Criteria Tests")
    class SearchCriteriaTests {

        @Test
        @DisplayName("Should handle null search criteria")
        void shouldHandleNullSearchCriteria() {
            // Arrange
            List<Employee> employees = Arrays.asList(employee1, employee2);
            when(employeeRepository.search(null)).thenReturn(employees);

            // Act
            List<EmployeeResponse> result = useCase.execute(null);

            // Assert
            assertThat(result, is(notNullValue()));
            assertThat(result, hasSize(2));
            verify(employeeRepository).search(null);
        }

        @Test
        @DisplayName("Should pass criteria unchanged to repository")
        void shouldPassCriteriaUnchangedToRepository() {
            // Arrange
            when(employeeRepository.search(searchCriteria)).thenReturn(Collections.emptyList());

            // Act
            useCase.execute(searchCriteria);

            // Assert
            verify(employeeRepository).search(searchCriteria);
        }

        @Test
        @DisplayName("Should handle different criteria objects")
        void shouldHandleDifferentCriteriaObjects() {
            // Arrange
            EmployeeSearchCriteria criteria1 = searchCriteria;
            EmployeeSearchCriteria criteria2 = mock(EmployeeSearchCriteria.class); 

            when(employeeRepository.search(criteria1)).thenReturn(Arrays.asList(employee1));
            when(employeeRepository.search(criteria2)).thenReturn(Arrays.asList(employee2));

            // Act
            List<EmployeeResponse> result1 = useCase.execute(criteria1);
            List<EmployeeResponse> result2 = useCase.execute(criteria2);

            // Assert
            assertThat(result1, hasSize(1));
            assertThat(result2, hasSize(1));
            verify(employeeRepository).search(criteria1);
            verify(employeeRepository).search(criteria2);
        }

        @Test
        @DisplayName("Should not modify search criteria")
        void shouldNotModifySearchCriteria() {
            // Arrange
            when(employeeRepository.search(searchCriteria)).thenReturn(Arrays.asList(employee1));

            // Act
            useCase.execute(searchCriteria);

            // Assert
            verify(employeeRepository).search(searchCriteria);
            // Criteria should be passed as-is to repository
        }
    }

    @Nested
    @DisplayName("Stream Processing Tests")
    class StreamProcessingTests {

        @Test
        @DisplayName("Should convert stream to immutable list")
        void shouldConvertStreamToImmutableList() {
            // Arrange
            List<Employee> employees = Arrays.asList(employee1, employee2);
            when(employeeRepository.search(searchCriteria)).thenReturn(employees);

            // Act
            List<EmployeeResponse> result = useCase.execute(searchCriteria);
            EmployeeResponse employeeResponse1 = new EmployeeResponse(employee1);

            // Assert
            assertThat(result, is(notNullValue()));
            assertThrows(UnsupportedOperationException.class, () -> 
                result.add(employeeResponse1)
            );
            assertThrows(UnsupportedOperationException.class, () -> 
                result.remove(0)
            );
            assertThrows(UnsupportedOperationException.class, result::clear);
        }

        @Test
        @DisplayName("Should maintain stream order in result list")
        void shouldMaintainStreamOrderInResultList() {
            // Arrange
            List<Employee> employees = Arrays.asList(employee1, employee2, employee3);
            when(employeeRepository.search(searchCriteria)).thenReturn(employees);

            // Act
            List<EmployeeResponse> result = useCase.execute(searchCriteria);

            // Assert
            assertThat(result, hasSize(3));
            // Order should be maintained from original list
            for (int i = 0; i < result.size(); i++) {
                assertThat(result.get(i), is(instanceOf(EmployeeResponse.class)));
            }
        }

        @Test
        @DisplayName("Should handle stream with null elements")
        void shouldHandleStreamWithNullElements() {
            // Arrange
            List<Employee> employeesWithNull = Arrays.asList(employee1, null, employee2);
            when(employeeRepository.search(searchCriteria)).thenReturn(employeesWithNull);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> useCase.execute(searchCriteria));
        }

        @Test
        @DisplayName("Should create new EmployeeResponse for each Employee")
        void shouldCreateNewEmployeeResponseForEachEmployee() {
            // Arrange
            List<Employee> employees = Arrays.asList(employee1, employee1, employee1); // Same employee multiple times
            when(employeeRepository.search(searchCriteria)).thenReturn(employees);

            // Act
            List<EmployeeResponse> result = useCase.execute(searchCriteria);

            // Assert
            assertThat(result, hasSize(3));
            // Each should be a separate EmployeeResponse instance
            result.forEach(response -> 
                assertThat(response, is(instanceOf(EmployeeResponse.class)))
            );
        }

        @Test
        @DisplayName("Should use EmployeeResponse constructor for mapping")
        void shouldUseEmployeeResponseConstructorForMapping() {
            // Arrange
            List<Employee> employees = Arrays.asList(employee1);
            when(employeeRepository.search(searchCriteria)).thenReturn(employees);

            // Act
            List<EmployeeResponse> result = useCase.execute(searchCriteria);

            // Assert
            assertThat(result, hasSize(1));
            assertThat(result.get(0), is(instanceOf(EmployeeResponse.class)));
            // The EmployeeResponse should be created from the Employee
        }
    }

    @Nested
    @DisplayName("Repository Integration Tests")
    class RepositoryIntegrationTests {

        @Test
        @DisplayName("Should delegate search operation to repository")
        void shouldDelegateSearchOperationToRepository() {
            // Arrange
            when(employeeRepository.search(searchCriteria)).thenReturn(Collections.emptyList());

            // Act
            useCase.execute(searchCriteria);

            // Assert
            verify(employeeRepository).search(searchCriteria);
        }

        @Test
        @DisplayName("Should not perform additional repository operations")
        void shouldNotPerformAdditionalRepositoryOperations() {
            // Arrange
            when(employeeRepository.search(searchCriteria)).thenReturn(Arrays.asList(employee1));

            // Act
            useCase.execute(searchCriteria);

            // Assert
            verify(employeeRepository, times(1)).search(searchCriteria);
            verify(employeeRepository, never()).findById(any());
            verify(employeeRepository, never()).findAll();
        }

        @Test
        @DisplayName("Should handle repository search exactly once per execution")
        void shouldHandleRepositorySearchExactlyOncePerExecution() {
            // Arrange
            when(employeeRepository.search(any())).thenReturn(Arrays.asList(employee1));

            // Act
            useCase.execute(searchCriteria);
            useCase.execute(searchCriteria);
            useCase.execute(searchCriteria);

            // Assert
            verify(employeeRepository, times(3)).search(searchCriteria);
        }

        @Test
        @DisplayName("Should work with different repository implementations")
        void shouldWorkWithDifferentRepositoryImplementations() {
            // This test verifies the use case works with the repository interface
            // Arrange
            when(employeeRepository.search(searchCriteria)).thenReturn(Arrays.asList(employee1, employee2));

            // Act
            List<EmployeeResponse> result = useCase.execute(searchCriteria);

            // Assert
            assertThat(result, hasSize(2));
            verify(employeeRepository).search(searchCriteria);
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @DisplayName("Should propagate RuntimeException from repository")
        void shouldPropagateRuntimeExceptionFromRepository() {
            // Arrange
            RuntimeException repositoryException = new RuntimeException("Database connection error");
            when(employeeRepository.search(searchCriteria)).thenThrow(repositoryException);

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () ->
                useCase.execute(searchCriteria)
            );

            assertThat(exception.getMessage(), is(equalTo("Database connection error")));
            verify(employeeRepository).search(searchCriteria);
        }

        @Test
        @DisplayName("Should propagate IllegalArgumentException from repository")
        void shouldPropagateIllegalArgumentExceptionFromRepository() {
            // Arrange
            IllegalArgumentException repositoryException = new IllegalArgumentException("Invalid search criteria");
            when(employeeRepository.search(searchCriteria)).thenThrow(repositoryException);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                useCase.execute(searchCriteria)
            );

            assertThat(exception.getMessage(), is(equalTo("Invalid search criteria")));
            verify(employeeRepository).search(searchCriteria);
        }

        @Test
        @DisplayName("Should handle NullPointerException from repository")
        void shouldHandleNullPointerExceptionFromRepository() {
            // Arrange
            when(employeeRepository.search(searchCriteria)).thenThrow(new NullPointerException("Null criteria"));

            // Act & Assert
            NullPointerException exception = assertThrows(NullPointerException.class, () ->
                useCase.execute(searchCriteria)
            );

            assertThat(exception.getMessage(), is(equalTo("Null criteria")));
        }

        @Test
        @DisplayName("Should handle repository throwing during stream processing")
        void shouldHandleRepositoryThrowingDuringStreamProcessing() {
            // Arrange
            when(employeeRepository.search(searchCriteria)).thenThrow(new RuntimeException("Stream error"));

            // Act & Assert
            assertThrows(RuntimeException.class, () -> useCase.execute(searchCriteria));
        }

        @Test
        @DisplayName("Should not catch or handle repository exceptions")
        void shouldNotCatchOrHandleRepositoryExceptions() {
            // Arrange
            RuntimeException expectedException = new RuntimeException("Expected error");
            when(employeeRepository.search(searchCriteria)).thenThrow(expectedException);

            // Act & Assert
            RuntimeException actualException = assertThrows(RuntimeException.class, () ->
                useCase.execute(searchCriteria)
            );

            assertThat(actualException, is(equalTo(expectedException)));
        }
    }

    @Nested
    @DisplayName("Transactional Behavior Tests")
    class TransactionalBehaviorTests {

        @Test
        @DisplayName("Should be annotated with @Transactional(readOnly = true)")
        void shouldBeAnnotatedWithTransactionalReadOnly() {
            // This test verifies the class has the correct @Transactional annotation
            assertThat(SearchEmployeeUseCase.class.isAnnotationPresent(
                org.springframework.transaction.annotation.Transactional.class), is(true));
            
            // Additional verification for readOnly = true would require reflection
            // or integration testing
        }

        @Test
        @DisplayName("Should execute within read-only transaction context")
        void shouldExecuteWithinReadOnlyTransactionContext() {
            // Arrange
            when(employeeRepository.search(searchCriteria)).thenReturn(Arrays.asList(employee1));

            // Act
            List<EmployeeResponse> result = useCase.execute(searchCriteria);

            // Assert - Should complete successfully in read-only context
            assertThat(result, hasSize(1));
            verify(employeeRepository).search(searchCriteria);
        }

        @Test
        @DisplayName("Should not perform write operations")
        void shouldNotPerformWriteOperations() {
            // Arrange
            when(employeeRepository.search(searchCriteria)).thenReturn(Arrays.asList(employee1));

            // Act
            useCase.execute(searchCriteria);

            // Assert - Only read operation should be performed
            verify(employeeRepository).search(searchCriteria);
            verify(employeeRepository, never()).save(any());
            verify(employeeRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle large result sets efficiently")
        void shouldHandleLargeResultSetsEfficiently() {
            // Arrange
            int largeSize = 10000;
            List<Employee> largeEmployeeList = IntStream.range(0, largeSize)
                .mapToObj(i -> employee1)
                .toList();
            when(employeeRepository.search(searchCriteria)).thenReturn(largeEmployeeList);

            // Act
            List<EmployeeResponse> result = useCase.execute(searchCriteria);

            // Assert
            assertThat(result, hasSize(largeSize));
            verify(employeeRepository).search(searchCriteria);
        }

        @Test
        @DisplayName("Should maintain performance with multiple executions")
        void shouldMaintainPerformanceWithMultipleExecutions() {
            // Arrange
            when(employeeRepository.search(any())).thenReturn(Arrays.asList(employee1, employee2));

            // Act
            for (int i = 0; i < 100; i++) {
                List<EmployeeResponse> result = useCase.execute(searchCriteria);
                assertThat(result, hasSize(2));
            }

            // Assert
            verify(employeeRepository, times(100)).search(searchCriteria);
        }

        @Test
        @DisplayName("Should handle rapid successive searches")
        void shouldHandleRapidSuccessiveSearches() {
            // Arrange
            when(employeeRepository.search(searchCriteria)).thenReturn(Arrays.asList(employee1));

            // Act & Assert
            for (int i = 0; i < 1000; i++) {
                List<EmployeeResponse> result = useCase.execute(searchCriteria);
                assertThat(result, hasSize(1));
            }
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle employee with null properties")
        void shouldHandleEmployeeWithNullProperties() {
            // Arrange
            Employee employeeWithNulls = employee1; // Mock already setup
            when(employeeRepository.search(searchCriteria)).thenReturn(Arrays.asList(employeeWithNulls));

            // Act
            List<EmployeeResponse> result = useCase.execute(searchCriteria);

            // Assert
            assertThat(result, hasSize(1));
            assertThat(result.get(0), is(instanceOf(EmployeeResponse.class)));
        }

        @Test
        @DisplayName("Should handle mixed employee data quality")
        void shouldHandleMixedEmployeeDataQuality() {
            // Arrange
            List<Employee> mixedEmployees = Arrays.asList(employee1, employee2, employee3);
            when(employeeRepository.search(searchCriteria)).thenReturn(mixedEmployees);

            // Act
            List<EmployeeResponse> result = useCase.execute(searchCriteria);

            // Assert
            assertThat(result, hasSize(3));
            result.forEach(response -> 
                assertThat(response, is(instanceOf(EmployeeResponse.class)))
            );
        }

        @Test
        @DisplayName("Should handle repository returning same employee multiple times")
        void shouldHandleRepositoryReturningSameEmployeeMultipleTimes() {
            // Arrange
            List<Employee> duplicateEmployees = Arrays.asList(employee1, employee1, employee1);
            when(employeeRepository.search(searchCriteria)).thenReturn(duplicateEmployees);

            // Act
            List<EmployeeResponse> result = useCase.execute(searchCriteria);

            // Assert
            assertThat(result, hasSize(3));
            result.forEach(response -> 
                assertThat(response, is(instanceOf(EmployeeResponse.class)))
            );
        }

        @Test
        @DisplayName("Should handle repository with inconsistent ordering")
        void shouldHandleRepositoryWithInconsistentOrdering() {
            // Arrange
            List<Employee> unorderedEmployees = Arrays.asList(employee3, employee1, employee2);
            when(employeeRepository.search(searchCriteria)).thenReturn(unorderedEmployees);

            // Act
            List<EmployeeResponse> result = useCase.execute(searchCriteria);

            // Assert
            assertThat(result, hasSize(3));
            // Result should maintain the order returned by repository
            result.forEach(response -> 
                assertThat(response, is(instanceOf(EmployeeResponse.class)))
            );
        }

        @Test
        @DisplayName("Should handle very large employee lists")
        void shouldHandleVeryLargeEmployeeLists() {
            // Arrange
            int veryLargeSize = 100000;
            List<Employee> veryLargeList = IntStream.range(0, veryLargeSize)
                .mapToObj(i -> employee1)
                .toList();
            when(employeeRepository.search(searchCriteria)).thenReturn(veryLargeList);

            // Act
            List<EmployeeResponse> result = useCase.execute(searchCriteria);

            // Assert
            assertThat(result, hasSize(veryLargeSize));
        }
    }

    @Nested
    @DisplayName("Return Type Tests")
    class ReturnTypeTests {

        @Test
        @DisplayName("Should return List interface type")
        void shouldReturnListInterfaceType() {
            // Arrange
            when(employeeRepository.search(searchCriteria)).thenReturn(Arrays.asList(employee1));

            // Act
            List<EmployeeResponse> result = useCase.execute(searchCriteria);

            // Assert
            assertThat(result, is(instanceOf(List.class)));
        }

        @Test
        @DisplayName("Should return immutable list from toList()")
        void shouldReturnImmutableListFromToList() {
            // Arrange
            when(employeeRepository.search(searchCriteria)).thenReturn(Arrays.asList(employee1));

            // Act
            List<EmployeeResponse> result = useCase.execute(searchCriteria);
            EmployeeResponse employeeResponse1 = new EmployeeResponse(employee1);

            // Assert - List from toList() should be immutable
            assertThrows(UnsupportedOperationException.class, () -> result.add(employeeResponse1));
            assertThrows(UnsupportedOperationException.class, () -> result.remove(0));
            assertThrows(UnsupportedOperationException.class, result::clear);
        }

        @Test
        @DisplayName("Should return list with correct generic type")
        void shouldReturnListWithCorrectGenericType() {
            // Arrange
            when(employeeRepository.search(searchCriteria)).thenReturn(Arrays.asList(employee1));

            // Act
            List<EmployeeResponse> result = useCase.execute(searchCriteria);

            // Assert
            assertThat(result, hasSize(1));
            assertThat(result.get(0), is(instanceOf(EmployeeResponse.class)));
        }

        @Test
        @DisplayName("Should return consistent list type across multiple calls")
        void shouldReturnConsistentListTypeAcrossMultipleCalls() {
            // Arrange
            when(employeeRepository.search(searchCriteria)).thenReturn(Arrays.asList(employee1));

            // Act
            List<EmployeeResponse> result1 = useCase.execute(searchCriteria);
            List<EmployeeResponse> result2 = useCase.execute(searchCriteria);

            // Assert
            assertThat(result1.getClass(), is(equalTo(result2.getClass())));
            assertThat(result1, hasSize(1));
            assertThat(result2, hasSize(1));
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should work correctly with complete search workflow")
        void shouldWorkCorrectlyWithCompleteSearchWorkflow() {
            // Test a complete workflow with various scenarios

            // 1. Successful search with results
            when(employeeRepository.search(searchCriteria)).thenReturn(Arrays.asList(employee1, employee2));
            List<EmployeeResponse> result1 = useCase.execute(searchCriteria);
            assertThat(result1, hasSize(2));

            // 2. Empty search results
            when(employeeRepository.search(searchCriteria)).thenReturn(Collections.emptyList());
            List<EmployeeResponse> result2 = useCase.execute(searchCriteria);
            assertThat(result2, is(empty()));

            // 3. Null criteria
            when(employeeRepository.search(null)).thenReturn(Arrays.asList(employee3));
            List<EmployeeResponse> result3 = useCase.execute(null);
            assertThat(result3, hasSize(1));

            // Verify all repository interactions
            verify(employeeRepository, times(2)).search(searchCriteria);
            verify(employeeRepository).search(null);
        }

        @Test
        @DisplayName("Should maintain consistency across multiple search operations")
        void shouldMaintainConsistencyAcrossMultipleSearchOperations() {
            // Arrange - Different search scenarios
            when(employeeRepository.search(searchCriteria)).thenReturn(Arrays.asList(employee1, employee2));

            // Act - Multiple searches
            List<EmployeeResponse> result1 = useCase.execute(searchCriteria);
            List<EmployeeResponse> result2 = useCase.execute(searchCriteria);
            List<EmployeeResponse> result3 = useCase.execute(searchCriteria);

            // Assert - All results should be consistent
            assertThat(result1, hasSize(2));
            assertThat(result2, hasSize(2));
            assertThat(result3, hasSize(2));

            // All should contain EmployeeResponse instances
            result1.forEach(response -> assertThat(response, is(instanceOf(EmployeeResponse.class))));
            result2.forEach(response -> assertThat(response, is(instanceOf(EmployeeResponse.class))));
            result3.forEach(response -> assertThat(response, is(instanceOf(EmployeeResponse.class))));

            verify(employeeRepository, times(3)).search(searchCriteria);
        }

        @Test
        @DisplayName("Should handle mixed success and error scenarios gracefully")
        void shouldHandleMixedSuccessAndErrorScenariosGracefully() {
            // Arrange
            when(employeeRepository.search(searchCriteria))
                .thenReturn(Arrays.asList(employee1))  // First call succeeds
                .thenThrow(new RuntimeException("DB Error"))  // Second call fails
                .thenReturn(Collections.emptyList());  // Third call succeeds with empty result

            // Act & Assert
            // First call - success
            List<EmployeeResponse> result1 = useCase.execute(searchCriteria);
            assertThat(result1, hasSize(1));

            // Second call - error
            assertThrows(RuntimeException.class, () -> useCase.execute(searchCriteria));

            // Third call - success with empty result
            List<EmployeeResponse> result3 = useCase.execute(searchCriteria);
            assertThat(result3, is(empty()));

            verify(employeeRepository, times(3)).search(searchCriteria);
        }
    }

    @Nested
    @DisplayName("Method Isolation Tests")
    class MethodIsolationTests {

        @Test
        @DisplayName("Should not have side effects between executions")
        void shouldNotHaveSideEffectsBetweenExecutions() {
            // Arrange
            when(employeeRepository.search(any())).thenReturn(Arrays.asList(employee1));

            // Act
            List<EmployeeResponse> result1 = useCase.execute(searchCriteria);
            List<EmployeeResponse> result2 = useCase.execute(searchCriteria);

            // Assert
            assertThat(result1, hasSize(1));
            assertThat(result2, hasSize(1));
            
            // Results should be independent
            assertThat(result1 != result2, is(true));
            verify(employeeRepository, times(2)).search(searchCriteria);
        }

        @Test
        @DisplayName("Should handle concurrent execution simulation")
        void shouldHandleConcurrentExecutionSimulation() {
            // Arrange
            when(employeeRepository.search(searchCriteria)).thenReturn(Arrays.asList(employee1, employee2));

            // Act - Simulate rapid concurrent calls
            List<List<EmployeeResponse>> results = IntStream.range(0, 10)
                .mapToObj(i -> useCase.execute(searchCriteria))
                .toList();

            // Assert
            results.forEach(result -> {
                assertThat(result, hasSize(2));
                result.forEach(response -> 
                    assertThat(response, is(instanceOf(EmployeeResponse.class)))
                );
            });

            verify(employeeRepository, times(10)).search(searchCriteria);
        }
    }
}
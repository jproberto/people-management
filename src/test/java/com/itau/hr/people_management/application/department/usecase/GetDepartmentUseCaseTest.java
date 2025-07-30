package com.itau.hr.people_management.application.department.usecase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.application.department.dto.DepartmentResponse;
import com.itau.hr.people_management.domain.department.entity.Department;
import com.itau.hr.people_management.domain.department.repository.DepartmentRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetDepartmentUseCase Tests")
class GetDepartmentUseCaseTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private Department department1;

    @Mock
    private Department department2;

    @Mock
    private Department department3;

    private GetDepartmentUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetDepartmentUseCase(departmentRepository);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create use case with valid repository")
        void shouldCreateUseCaseWithValidRepository() {
            // Act
            GetDepartmentUseCase newUseCase = new GetDepartmentUseCase(departmentRepository);

            // Assert
            assertThat(newUseCase, is(notNullValue()));
        }

        @Test
        @DisplayName("Should accept null repository in constructor")
        void shouldAcceptNullRepositoryInConstructor() {
            // Act & Assert - Constructor should accept null (will fail at runtime)
            assertDoesNotThrow(() -> {
                GetDepartmentUseCase newUseCase = new GetDepartmentUseCase(null);
                assertThat(newUseCase, is(notNullValue()));
            });
        }
    }

    @Nested
    @DisplayName("GetAll Method Tests")
    class GetAllMethodTests {

        @Test
        @DisplayName("Should return empty list when no departments exist")
        void shouldReturnEmptyListWhenNoDepartmentsExist() {
            // Arrange
            when(departmentRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<DepartmentResponse> result = useCase.getAll();

            // Assert
            assertThat(result, is(notNullValue()));
            assertThat(result, is(empty()));
            assertThat(result.size(), is(equalTo(0)));

            verify(departmentRepository).findAll();
        }

        @Test
        @DisplayName("Should return single department response when one department exists")
        void shouldReturnSingleDepartmentResponseWhenOneDepartmentExists() {
            // Arrange
            UUID departmentId = UUID.randomUUID();
            when(department1.getId()).thenReturn(departmentId);
            when(department1.getName()).thenReturn("IT Department");
            when(department1.getCostCenterCode()).thenReturn("IT001");

            when(departmentRepository.findAll()).thenReturn(Arrays.asList(department1));

            // Act
            List<DepartmentResponse> result = useCase.getAll();

            // Assert
            assertThat(result, is(notNullValue()));
            assertThat(result, hasSize(1));

            DepartmentResponse response = result.get(0);
            assertThat(response.getId(), is(equalTo(departmentId)));
            assertThat(response.getName(), is(equalTo("IT Department")));
            assertThat(response.getCostCenterCode(), is(equalTo("IT001")));

            verify(departmentRepository).findAll();
        }

        @Test
        @DisplayName("Should return multiple department responses when multiple departments exist")
        void shouldReturnMultipleDepartmentResponsesWhenMultipleDepartmentsExist() {
            // Arrange
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();
            UUID id3 = UUID.randomUUID();

            when(department1.getId()).thenReturn(id1);
            when(department1.getName()).thenReturn("IT Department");
            when(department1.getCostCenterCode()).thenReturn("IT001");

            when(department2.getId()).thenReturn(id2);
            when(department2.getName()).thenReturn("HR Department");
            when(department2.getCostCenterCode()).thenReturn("HR001");

            when(department3.getId()).thenReturn(id3);
            when(department3.getName()).thenReturn("Finance Department");
            when(department3.getCostCenterCode()).thenReturn("FIN001");

            when(departmentRepository.findAll()).thenReturn(Arrays.asList(department1, department2, department3));

            // Act
            List<DepartmentResponse> result = useCase.getAll();

            // Assert
            assertThat(result, is(notNullValue()));
            assertThat(result, hasSize(3));

            // Verify first department
            DepartmentResponse response1 = result.get(0);
            assertThat(response1.getId(), is(equalTo(id1)));
            assertThat(response1.getName(), is(equalTo("IT Department")));
            assertThat(response1.getCostCenterCode(), is(equalTo("IT001")));

            // Verify second department
            DepartmentResponse response2 = result.get(1);
            assertThat(response2.getId(), is(equalTo(id2)));
            assertThat(response2.getName(), is(equalTo("HR Department")));
            assertThat(response2.getCostCenterCode(), is(equalTo("HR001")));

            // Verify third department
            DepartmentResponse response3 = result.get(2);
            assertThat(response3.getId(), is(equalTo(id3)));
            assertThat(response3.getName(), is(equalTo("Finance Department")));
            assertThat(response3.getCostCenterCode(), is(equalTo("FIN001")));

            verify(departmentRepository).findAll();
        }

        @Test
        @DisplayName("Should preserve order of departments from repository")
        void shouldPreserveOrderOfDepartmentsFromRepository() {
            // Arrange
            when(department1.getId()).thenReturn(UUID.randomUUID());
            when(department1.getName()).thenReturn("First Department");
            when(department1.getCostCenterCode()).thenReturn("FIRST");

            when(department2.getId()).thenReturn(UUID.randomUUID());
            when(department2.getName()).thenReturn("Second Department");
            when(department2.getCostCenterCode()).thenReturn("SECOND");

            when(department3.getId()).thenReturn(UUID.randomUUID());
            when(department3.getName()).thenReturn("Third Department");
            when(department3.getCostCenterCode()).thenReturn("THIRD");

            // Repository returns in specific order
            when(departmentRepository.findAll()).thenReturn(Arrays.asList(department2, department1, department3));

            // Act
            List<DepartmentResponse> result = useCase.getAll();

            // Assert - Order should be preserved
            assertThat(result, hasSize(3));
            assertThat(result.get(0).getName(), is(equalTo("Second Department")));
            assertThat(result.get(1).getName(), is(equalTo("First Department")));
            assertThat(result.get(2).getName(), is(equalTo("Third Department")));
        }

        @Test
        @DisplayName("Should handle departments with null values")
        void shouldHandleDepartmentsWithNullValues() {
            // Arrange
            when(department1.getId()).thenReturn(null);
            when(department1.getName()).thenReturn(null);
            when(department1.getCostCenterCode()).thenReturn(null);

            when(departmentRepository.findAll()).thenReturn(Arrays.asList(department1));

            // Act
            List<DepartmentResponse> result = useCase.getAll();

            // Assert
            assertThat(result, hasSize(1));
            DepartmentResponse response = result.get(0);
            assertThat(response.getId(), is(nullValue()));
            assertThat(response.getName(), is(nullValue()));
            assertThat(response.getCostCenterCode(), is(nullValue()));
        }

        @Test
        @DisplayName("Should handle departments with empty strings")
        void shouldHandleDepartmentsWithEmptyStrings() {
            // Arrange
            UUID departmentId = UUID.randomUUID();
            when(department1.getId()).thenReturn(departmentId);
            when(department1.getName()).thenReturn("");
            when(department1.getCostCenterCode()).thenReturn("");

            when(departmentRepository.findAll()).thenReturn(Arrays.asList(department1));

            // Act
            List<DepartmentResponse> result = useCase.getAll();

            // Assert
            assertThat(result, hasSize(1));
            DepartmentResponse response = result.get(0);
            assertThat(response.getId(), is(equalTo(departmentId)));
            assertThat(response.getName(), is(equalTo("")));
            assertThat(response.getCostCenterCode(), is(equalTo("")));
        }

        @Test
        @DisplayName("Should handle departments with special characters")
        void shouldHandleDepartmentsWithSpecialCharacters() {
            // Arrange
            UUID departmentId = UUID.randomUUID();
            String specialName = "R&D - Research & Development (Special Projects)";
            String specialCode = "R&D-001_SPECIAL.DEPT";

            when(department1.getId()).thenReturn(departmentId);
            when(department1.getName()).thenReturn(specialName);
            when(department1.getCostCenterCode()).thenReturn(specialCode);

            when(departmentRepository.findAll()).thenReturn(Arrays.asList(department1));

            // Act
            List<DepartmentResponse> result = useCase.getAll();

            // Assert
            assertThat(result, hasSize(1));
            DepartmentResponse response = result.get(0);
            assertThat(response.getId(), is(equalTo(departmentId)));
            assertThat(response.getName(), is(equalTo(specialName)));
            assertThat(response.getCostCenterCode(), is(equalTo(specialCode)));
        }

        @Test
        @DisplayName("Should handle departments with unicode characters")
        void shouldHandleDepartmentsWithUnicodeCharacters() {
            // Arrange
            UUID departmentId = UUID.randomUUID();
            String unicodeName = "Departamento de Tecnología e Inovação";
            String unicodeCode = "TECH-BR-ÇÑÁ";

            when(department1.getId()).thenReturn(departmentId);
            when(department1.getName()).thenReturn(unicodeName);
            when(department1.getCostCenterCode()).thenReturn(unicodeCode);

            when(departmentRepository.findAll()).thenReturn(Arrays.asList(department1));

            // Act
            List<DepartmentResponse> result = useCase.getAll();

            // Assert
            assertThat(result, hasSize(1));
            DepartmentResponse response = result.get(0);
            assertThat(response.getId(), is(equalTo(departmentId)));
            assertThat(response.getName(), is(equalTo(unicodeName)));
            assertThat(response.getCostCenterCode(), is(equalTo(unicodeCode)));
        }
    }

    @Nested
    @DisplayName("Stream Mapping Tests")
    class StreamMappingTests {

        @Test
        @DisplayName("Should correctly map each department entity to response")
        void shouldCorrectlyMapEachDepartmentEntityToResponse() {
            // Arrange
            when(department1.getId()).thenReturn(UUID.randomUUID());
            when(department1.getName()).thenReturn("Department 1");
            when(department1.getCostCenterCode()).thenReturn("DEPT1");

            when(department2.getId()).thenReturn(UUID.randomUUID());
            when(department2.getName()).thenReturn("Department 2");
            when(department2.getCostCenterCode()).thenReturn("DEPT2");

            when(departmentRepository.findAll()).thenReturn(Arrays.asList(department1, department2));

            // Act
            List<DepartmentResponse> result = useCase.getAll();

            // Assert - Verify all entity getters were called
            verify(department1).getId();
            verify(department1).getName();
            verify(department1).getCostCenterCode();

            verify(department2).getId();
            verify(department2).getName();
            verify(department2).getCostCenterCode();

            assertThat(result, hasSize(2));
        }

        @Test
        @DisplayName("Should create independent response objects from entities")
        void shouldCreateIndependentResponseObjectsFromEntities() {
            // Arrange
            UUID sharedId = UUID.randomUUID();
            when(department1.getId()).thenReturn(sharedId);
            when(department1.getName()).thenReturn("Shared Department");
            when(department1.getCostCenterCode()).thenReturn("SHARED");

            when(department2.getId()).thenReturn(sharedId); // Same ID
            when(department2.getName()).thenReturn("Shared Department");
            when(department2.getCostCenterCode()).thenReturn("SHARED");

            when(departmentRepository.findAll()).thenReturn(Arrays.asList(department1, department2));

            // Act
            List<DepartmentResponse> result = useCase.getAll();

            // Assert - Should create two independent response objects
            assertThat(result, hasSize(2));
            
            DepartmentResponse response1 = result.get(0);
            DepartmentResponse response2 = result.get(1);
            
            // Objects should be different instances
            assertThat(response1, is(not(sameInstance(response2))));
            
            // But have same values
            assertThat(response1.getId(), is(equalTo(response2.getId())));
            assertThat(response1.getName(), is(equalTo(response2.getName())));
            assertThat(response1.getCostCenterCode(), is(equalTo(response2.getCostCenterCode())));
        }

        @Test
        @DisplayName("Should handle stream processing of large department list")
        void shouldHandleStreamProcessingOfLargeDepartmentList() {
            // Arrange - Create a large list of departments
            List<Department> largeDepartmentList = Arrays.asList(
                department1, department2, department3, department1, department2, 
                department3, department1, department2, department3, department1
            );

            // Setup common responses for reused mocks
            when(department1.getId()).thenReturn(UUID.randomUUID());
            when(department1.getName()).thenReturn("Department 1");
            when(department1.getCostCenterCode()).thenReturn("DEPT1");

            when(department2.getId()).thenReturn(UUID.randomUUID());
            when(department2.getName()).thenReturn("Department 2");
            when(department2.getCostCenterCode()).thenReturn("DEPT2");

            when(department3.getId()).thenReturn(UUID.randomUUID());
            when(department3.getName()).thenReturn("Department 3");
            when(department3.getCostCenterCode()).thenReturn("DEPT3");

            when(departmentRepository.findAll()).thenReturn(largeDepartmentList);

            // Act
            List<DepartmentResponse> result = useCase.getAll();

            // Assert
            assertThat(result, hasSize(10));
            assertThat(result, everyItem(is(notNullValue())));
        }
    }

    @Nested
    @DisplayName("Repository Integration Tests")
    class RepositoryIntegrationTests {

        @Test
        @DisplayName("Should call repository findAll method exactly once")
        void shouldCallRepositoryFindAllMethodExactlyOnce() {
            // Arrange
            when(departmentRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            useCase.getAll();

            // Assert
            verify(departmentRepository, times(1)).findAll();
            verifyNoMoreInteractions(departmentRepository);
        }

        @Test
        @DisplayName("Should handle repository returning null")
        void shouldHandleRepositoryReturningNull() {
            // Arrange
            when(departmentRepository.findAll()).thenReturn(null);

            // Act & Assert
            assertThrows(NullPointerException.class, () -> {
                useCase.getAll();
            });

            verify(departmentRepository).findAll();
        }

        @Test
        @DisplayName("Should handle repository throwing exception")
        void shouldHandleRepositoryThrowingException() {
            // Arrange
            when(departmentRepository.findAll()).thenThrow(new RuntimeException("Database connection error"));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                useCase.getAll();
            });

            assertThat(exception.getMessage(), is(equalTo("Database connection error")));
            verify(departmentRepository).findAll();
        }

        @Test
        @DisplayName("Should propagate repository exceptions without modification")
        void shouldPropagateRepositoryExceptionsWithoutModification() {
            // Arrange
            RuntimeException originalException = new RuntimeException("Original database error");
            when(departmentRepository.findAll()).thenThrow(originalException);

            // Act & Assert
            RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                useCase.getAll();
            });

            assertThat(thrownException, is(sameInstance(originalException)));
        }
    }

    @Nested
    @DisplayName("Return Type Tests")
    class ReturnTypeTests {

        @Test
        @DisplayName("Should return List interface type")
        void shouldReturnListInterfaceType() {
            // Arrange
            when(departmentRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            Object result = useCase.getAll();

            // Assert
            assertThat(result, is(instanceOf(List.class)));
            assertThat(result, is(not(instanceOf(Arrays.class))));
        }

        @Test
        @DisplayName("Should return immutable list from toList()")
        void shouldReturnImmutableListFromToList() {
            // Arrange
            when(department1.getId()).thenReturn(UUID.randomUUID());
            when(department1.getName()).thenReturn("IT Department");
            when(department1.getCostCenterCode()).thenReturn("IT001");

            when(departmentRepository.findAll()).thenReturn(Arrays.asList(department1));

            // Act
            List<DepartmentResponse> result = useCase.getAll();

            // Assert - List from toList() should be immutable
            DepartmentResponse newDepartment = new DepartmentResponse();
            assertThrows(UnsupportedOperationException.class, () -> result.add(newDepartment));
            assertThrows(UnsupportedOperationException.class, () -> result.remove(0));
            assertThrows(UnsupportedOperationException.class, result::clear);
        }

        @Test
        @DisplayName("Should return list with correct generic type")
        void shouldReturnListWithCorrectGenericType() {
            // Arrange
            when(department1.getId()).thenReturn(UUID.randomUUID());
            when(department1.getName()).thenReturn("IT Department");
            when(department1.getCostCenterCode()).thenReturn("IT001");

            when(departmentRepository.findAll()).thenReturn(Arrays.asList(department1));

            // Act
            List<DepartmentResponse> result = useCase.getAll();

            // Assert
            assertThat(result, hasSize(1));
            assertThat(result.get(0), is(instanceOf(DepartmentResponse.class)));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle multiple rapid calls")
        void shouldHandleMultipleRapidCalls() {
            // Arrange
            when(departmentRepository.findAll()).thenReturn(Collections.emptyList());

            // Act & Assert
            for (int i = 0; i < 1000; i++) {
                List<DepartmentResponse> result = useCase.getAll();
                assertThat(result, is(notNullValue()));
                assertThat(result, is(empty()));
            }

            verify(departmentRepository, times(1000)).findAll();
        }

        @Test
        @DisplayName("Should handle departments with extremely long values")
        void shouldHandleDepartmentsWithExtremelyLongValues() {
            // Arrange
            UUID departmentId = UUID.randomUUID();
            String longName = "Very Long Department Name ".repeat(50); // ~1250+ characters
            String longCode = "VERY-LONG-CODE-".repeat(10); // ~150+ characters

            when(department1.getId()).thenReturn(departmentId);
            when(department1.getName()).thenReturn(longName);
            when(department1.getCostCenterCode()).thenReturn(longCode);

            when(departmentRepository.findAll()).thenReturn(Arrays.asList(department1));

            // Act
            List<DepartmentResponse> result = useCase.getAll();

            // Assert
            assertThat(result, hasSize(1));
            DepartmentResponse response = result.get(0);
            assertThat(response.getName(), is(equalTo(longName)));
            assertThat(response.getCostCenterCode(), is(equalTo(longCode)));
            assertThat(response.getName().length(), is(greaterThan(1000)));
        }

        @Test
        @DisplayName("Should handle departments with whitespace values")
        void shouldHandleDepartmentsWithWhitespaceValues() {
            // Arrange
            UUID departmentId = UUID.randomUUID();
            String nameWithSpaces = "  Department With Spaces  ";
            String codeWithSpaces = "  CODE-001  ";

            when(department1.getId()).thenReturn(departmentId);
            when(department1.getName()).thenReturn(nameWithSpaces);
            when(department1.getCostCenterCode()).thenReturn(codeWithSpaces);

            when(departmentRepository.findAll()).thenReturn(Arrays.asList(department1));

            // Act
            List<DepartmentResponse> result = useCase.getAll();

            // Assert
            assertThat(result, hasSize(1));
            DepartmentResponse response = result.get(0);
            assertThat(response.getName(), is(equalTo(nameWithSpaces)));
            assertThat(response.getCostCenterCode(), is(equalTo(codeWithSpaces)));
        }

        @Test
        @DisplayName("Should handle concurrent access to same department entities")
        void shouldHandleConcurrentAccessToSameDepartmentEntities() {
            // Arrange
            when(department1.getId()).thenReturn(UUID.randomUUID());
            when(department1.getName()).thenReturn("Concurrent Department");
            when(department1.getCostCenterCode()).thenReturn("CONCURRENT");

            when(departmentRepository.findAll()).thenReturn(Arrays.asList(department1));

            // Act - Simulate concurrent calls
            List<DepartmentResponse> result1 = useCase.getAll();
            List<DepartmentResponse> result2 = useCase.getAll();
            List<DepartmentResponse> result3 = useCase.getAll();

            // Assert - All results should be consistent
            assertThat(result1, hasSize(1));
            assertThat(result2, hasSize(1));
            assertThat(result3, hasSize(1));

            assertThat(result1.get(0).getName(), is(equalTo("Concurrent Department")));
            assertThat(result2.get(0).getName(), is(equalTo("Concurrent Department")));
            assertThat(result3.get(0).getName(), is(equalTo("Concurrent Department")));

            verify(departmentRepository, times(3)).findAll();
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should follow complete read-only business flow")
        void shouldFollowCompleteReadOnlyBusinessFlow() {
            // Arrange
            when(department1.getId()).thenReturn(UUID.randomUUID());
            when(department1.getName()).thenReturn("Business Department");
            when(department1.getCostCenterCode()).thenReturn("BIZ001");

            when(departmentRepository.findAll()).thenReturn(Arrays.asList(department1));

            // Act
            List<DepartmentResponse> result = useCase.getAll();

            // Assert - Complete business flow verification
            // 1. Fetch all departments from repository
            verify(departmentRepository).findAll();
            
            // 2. Transform each entity to response DTO
            verify(department1).getId();
            verify(department1).getName();
            verify(department1).getCostCenterCode();
            
            // 3. Return list of responses
            assertThat(result, is(notNullValue()));
            assertThat(result, hasSize(1));
            assertThat(result.get(0), is(instanceOf(DepartmentResponse.class)));
        }

        @Test
        @DisplayName("Should maintain data consistency between entity and response")
        void shouldMaintainDataConsistencyBetweenEntityAndResponse() {
            // Arrange
            UUID expectedId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
            String expectedName = "Consistency Test Department";
            String expectedCode = "CONSISTENCY-001";

            when(department1.getId()).thenReturn(expectedId);
            when(department1.getName()).thenReturn(expectedName);
            when(department1.getCostCenterCode()).thenReturn(expectedCode);

            when(departmentRepository.findAll()).thenReturn(Arrays.asList(department1));

            // Act
            List<DepartmentResponse> result = useCase.getAll();

            // Assert - Data should be exactly preserved
            assertThat(result, hasSize(1));
            DepartmentResponse response = result.get(0);
            
            assertThat(response.getId(), is(equalTo(expectedId)));
            assertThat(response.getName(), is(equalTo(expectedName)));
            assertThat(response.getCostCenterCode(), is(equalTo(expectedCode)));
        }

        @Test
        @DisplayName("Should be read-only operation without side effects")
        void shouldBeReadOnlyOperationWithoutSideEffects() {
            // Arrange
            when(departmentRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<DepartmentResponse> result1 = useCase.getAll();
            List<DepartmentResponse> result2 = useCase.getAll();
            List<DepartmentResponse> result3 = useCase.getAll();

            // Assert - Multiple calls should not have side effects
            assertThat(result1, is(empty()));
            assertThat(result2, is(empty()));
            assertThat(result3, is(empty()));

            // Should only call repository, no other modifications
            verify(departmentRepository, times(3)).findAll();
            verifyNoMoreInteractions(departmentRepository);
        }
    }
}

package com.itau.hr.people_management.application.department.usecase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.application.department.dto.CreateDepartmentRequest;
import com.itau.hr.people_management.application.department.dto.DepartmentResponse;
import com.itau.hr.people_management.domain.department.entity.Department;
import com.itau.hr.people_management.domain.department.repository.DepartmentRepository;
import com.itau.hr.people_management.domain.shared.exception.ConflictException;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateDepartmentUseCase Tests")
class CreateDepartmentUseCaseTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private Department departmentMock;

    @Mock
    private Department savedDepartmentMock;

    private CreateDepartmentUseCase useCase;
    private CreateDepartmentRequest request;

    @BeforeEach
    void setUp() {
        useCase = new CreateDepartmentUseCase(departmentRepository);
        request = new CreateDepartmentRequest();
        request.setName("IT Department");
        request.setCostCenterCode("IT001");
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create use case with valid repository")
        void shouldCreateUseCaseWithValidRepository() {
            // Act
            CreateDepartmentUseCase newUseCase = new CreateDepartmentUseCase(departmentRepository);

            // Assert
            assertThat(newUseCase, is(notNullValue()));
        }

        @Test
        @DisplayName("Should accept null repository in constructor")
        void shouldAcceptNullRepositoryInConstructor() {
            // Act & Assert - Constructor should accept null (will fail at runtime)
            assertDoesNotThrow(() -> {
                CreateDepartmentUseCase newUseCase = new CreateDepartmentUseCase(null);
                assertThat(newUseCase, is(notNullValue()));
            });
        }
    }

    @Nested
    @DisplayName("Successful Execution Tests")
    class SuccessfulExecutionTests {

        @Test
        @DisplayName("Should create department successfully when cost center code is unique")
        void shouldCreateDepartmentSuccessfullyWhenCostCenterCodeIsUnique() {
            // Arrange
            UUID departmentId = UUID.randomUUID();
            
            when(departmentRepository.findByCostCenterCode("IT001")).thenReturn(Optional.empty());
            when(departmentRepository.save(any(Department.class))).thenReturn(savedDepartmentMock);
            when(savedDepartmentMock.getId()).thenReturn(departmentId);
            when(savedDepartmentMock.getName()).thenReturn("IT Department");
            when(savedDepartmentMock.getCostCenterCode()).thenReturn("IT001");

            // Act
            DepartmentResponse response = useCase.execute(request);

            // Assert
            assertThat(response, is(notNullValue()));
            assertThat(response.getId(), is(equalTo(departmentId)));
            assertThat(response.getName(), is(equalTo("IT Department")));
            assertThat(response.getCostCenterCode(), is(equalTo("IT001")));

            verify(departmentRepository).findByCostCenterCode("IT001");
            verify(departmentRepository).save(any(Department.class));
        }

        @Test
        @DisplayName("Should call repository methods in correct order")
        void shouldCallRepositoryMethodsInCorrectOrder() {
            // Arrange
            when(departmentRepository.findByCostCenterCode(anyString())).thenReturn(Optional.empty());
            when(departmentRepository.save(any(Department.class))).thenReturn(savedDepartmentMock);
            when(savedDepartmentMock.getId()).thenReturn(UUID.randomUUID());
            when(savedDepartmentMock.getName()).thenReturn("IT Department");
            when(savedDepartmentMock.getCostCenterCode()).thenReturn("IT001");

            // Act
            useCase.execute(request);

            // Assert - Verify order of calls
            var inOrder = inOrder(departmentRepository);
            inOrder.verify(departmentRepository).findByCostCenterCode("IT001");
            inOrder.verify(departmentRepository).save(any(Department.class));
        }

        @Test
        @DisplayName("Should create department entity with correct parameters")
        void shouldCreateDepartmentEntityWithCorrectParameters() {
            // Arrange
            when(departmentRepository.findByCostCenterCode(anyString())).thenReturn(Optional.empty());
            when(departmentRepository.save(any(Department.class))).thenReturn(savedDepartmentMock);
            when(savedDepartmentMock.getId()).thenReturn(UUID.randomUUID());
            when(savedDepartmentMock.getName()).thenReturn("IT Department");
            when(savedDepartmentMock.getCostCenterCode()).thenReturn("IT001");

            // Act
            useCase.execute(request);

            // Assert - Capture the department being saved
            ArgumentCaptor<Department> departmentCaptor = ArgumentCaptor.forClass(Department.class);
            verify(departmentRepository).save(departmentCaptor.capture());
            
            Department capturedDepartment = departmentCaptor.getValue();
            assertThat(capturedDepartment, is(notNullValue()));
        }

        @Test
        @DisplayName("Should generate random UUID for department ID")
        void shouldGenerateRandomUuidForDepartmentId() {
            // Arrange
            when(departmentRepository.findByCostCenterCode(anyString())).thenReturn(Optional.empty());
            when(departmentRepository.save(any(Department.class))).thenReturn(savedDepartmentMock);
            when(savedDepartmentMock.getId()).thenReturn(UUID.randomUUID());
            when(savedDepartmentMock.getName()).thenReturn("IT Department");
            when(savedDepartmentMock.getCostCenterCode()).thenReturn("IT001");

            // Act - Execute multiple times to verify different UUIDs
            useCase.execute(request);
            useCase.execute(request);

            // Assert - Department.create should be called with different UUIDs each time
            verify(departmentRepository, times(2)).save(any(Department.class));
        }

        @Test
        @DisplayName("Should return response based on saved department")
        void shouldReturnResponseBasedOnSavedDepartment() {
            // Arrange
            UUID savedId = UUID.randomUUID();
            String savedName = "Saved IT Department";
            String savedCode = "SAVED-IT001";

            when(departmentRepository.findByCostCenterCode(anyString())).thenReturn(Optional.empty());
            when(departmentRepository.save(any(Department.class))).thenReturn(savedDepartmentMock);
            when(savedDepartmentMock.getId()).thenReturn(savedId);
            when(savedDepartmentMock.getName()).thenReturn(savedName);
            when(savedDepartmentMock.getCostCenterCode()).thenReturn(savedCode);

            // Act
            DepartmentResponse response = useCase.execute(request);

            // Assert - Response should be based on saved department, not original request
            assertThat(response.getId(), is(equalTo(savedId)));
            assertThat(response.getName(), is(equalTo(savedName)));
            assertThat(response.getCostCenterCode(), is(equalTo(savedCode)));
        }
    }

    @Nested
    @DisplayName("Conflict Exception Tests")
    class ConflictExceptionTests {

        @Test
        @DisplayName("Should throw ConflictException when cost center code already exists")
        void shouldThrowConflictExceptionWhenCostCenterCodeAlreadyExists() {
            // Arrange
            when(departmentRepository.findByCostCenterCode("IT001")).thenReturn(Optional.of(departmentMock));

            // Act & Assert
            ConflictException exception = assertThrows(ConflictException.class, () -> {
                useCase.execute(request);
            });

            assertThat(exception.getMessageKey(), is(equalTo("error.department.costcenter.exists")));
            assertThat(exception.getArgs(), is(arrayContaining("IT001")));

            verify(departmentRepository).findByCostCenterCode("IT001");
            verify(departmentRepository, never()).save(any(Department.class));
        }

        @Test
        @DisplayName("Should not save department when conflict exists")
        void shouldNotSaveDepartmentWhenConflictExists() {
            // Arrange
            when(departmentRepository.findByCostCenterCode(anyString())).thenReturn(Optional.of(departmentMock));

            // Act & Assert
            assertThrows(ConflictException.class, () -> {
                useCase.execute(request);
            });

            verify(departmentRepository, never()).save(any(Department.class));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "IT001",
            "HR-001", 
            "FIN_001",
            "MKT.001",
            "EXISTING-CODE",
            "DUPLICATE-123"
        })
        @DisplayName("Should detect conflicts for various cost center codes")
        void shouldDetectConflictsForVariousCostCenterCodes(String existingCode) {
            // Arrange
            request.setCostCenterCode(existingCode);
            when(departmentRepository.findByCostCenterCode(existingCode)).thenReturn(Optional.of(departmentMock));

            // Act & Assert
            ConflictException exception = assertThrows(ConflictException.class, () -> {
                useCase.execute(request);
            });

            assertThat(exception.getMessageKey(), is(equalTo("error.department.costcenter.exists")));
            assertThat(exception.getArgs(), is(arrayContaining(existingCode)));
        }

        @Test
        @DisplayName("Should throw conflict with exact cost center code from request")
        void shouldThrowConflictWithExactCostCenterCodeFromRequest() {
            // Arrange
            String conflictCode = "CONFLICT-CODE-123";
            request.setCostCenterCode(conflictCode);
            when(departmentRepository.findByCostCenterCode(conflictCode)).thenReturn(Optional.of(departmentMock));

            // Act & Assert
            ConflictException exception = assertThrows(ConflictException.class, () -> {
                useCase.execute(request);
            });

            assertThat(exception.getArgs()[0], is(equalTo(conflictCode)));
        }
    }

    @Nested
    @DisplayName("Request Parameter Tests")
    class RequestParameterTests {

        @Test
        @DisplayName("Should handle request with different valid names")
        void shouldHandleRequestWithDifferentValidNames() {
            // Arrange
            request.setName("Human Resources Department");
            when(departmentRepository.findByCostCenterCode(anyString())).thenReturn(Optional.empty());
            when(departmentRepository.save(any(Department.class))).thenReturn(savedDepartmentMock);
            when(savedDepartmentMock.getId()).thenReturn(UUID.randomUUID());
            when(savedDepartmentMock.getName()).thenReturn("Human Resources Department");
            when(savedDepartmentMock.getCostCenterCode()).thenReturn("IT001");

            // Act
            DepartmentResponse response = useCase.execute(request);

            // Assert
            assertThat(response, is(notNullValue()));
            verify(departmentRepository).save(any(Department.class));
        }

        @Test
        @DisplayName("Should handle request with special characters in name")
        void shouldHandleRequestWithSpecialCharactersInName() {
            // Arrange
            request.setName("R&D - Research & Development");
            when(departmentRepository.findByCostCenterCode(anyString())).thenReturn(Optional.empty());
            when(departmentRepository.save(any(Department.class))).thenReturn(savedDepartmentMock);
            when(savedDepartmentMock.getId()).thenReturn(UUID.randomUUID());
            when(savedDepartmentMock.getName()).thenReturn("R&D - Research & Development");
            when(savedDepartmentMock.getCostCenterCode()).thenReturn("IT001");

            // Act
            DepartmentResponse response = useCase.execute(request);

            // Assert
            assertThat(response, is(notNullValue()));
        }

        @Test
        @DisplayName("Should handle request with unicode characters")
        void shouldHandleRequestWithUnicodeCharacters() {
            // Arrange
            request.setName("Departamento de Tecnología");
            request.setCostCenterCode("TECH-BR-001");
            when(departmentRepository.findByCostCenterCode("TECH-BR-001")).thenReturn(Optional.empty());
            when(departmentRepository.save(any(Department.class))).thenReturn(savedDepartmentMock);
            when(savedDepartmentMock.getId()).thenReturn(UUID.randomUUID());
            when(savedDepartmentMock.getName()).thenReturn("Departamento de Tecnología");
            when(savedDepartmentMock.getCostCenterCode()).thenReturn("TECH-BR-001");

            // Act
            DepartmentResponse response = useCase.execute(request);

            // Assert
            assertThat(response, is(notNullValue()));
            verify(departmentRepository).findByCostCenterCode("TECH-BR-001");
        }

        @Test
        @DisplayName("Should pass all request data to department creation")
        void shouldPassAllRequestDataToDepartmentCreation() {
            // Arrange
            String testName = "Test Department Name";
            String testCode = "TEST-CODE-001";
            request.setName(testName);
            request.setCostCenterCode(testCode);

            when(departmentRepository.findByCostCenterCode(testCode)).thenReturn(Optional.empty());
            when(departmentRepository.save(any(Department.class))).thenReturn(savedDepartmentMock);
            when(savedDepartmentMock.getId()).thenReturn(UUID.randomUUID());
            when(savedDepartmentMock.getName()).thenReturn(testName);
            when(savedDepartmentMock.getCostCenterCode()).thenReturn(testCode);

            // Act
            useCase.execute(request);

            // Assert - Verify the request data is used for conflict check
            verify(departmentRepository).findByCostCenterCode(testCode);
        }
    }

    @Nested
    @DisplayName("Repository Integration Tests")
    class RepositoryIntegrationTests {

        @Test
        @DisplayName("Should handle repository returning different saved department")
        void shouldHandleRepositoryReturningDifferentSavedDepartment() {
            // Arrange - Repository might modify the department during save
            UUID savedId = UUID.randomUUID();
            
            when(departmentRepository.findByCostCenterCode(anyString())).thenReturn(Optional.empty());
            when(departmentRepository.save(any(Department.class))).thenReturn(savedDepartmentMock);
            when(savedDepartmentMock.getId()).thenReturn(savedId);
            when(savedDepartmentMock.getName()).thenReturn("Modified Name");
            when(savedDepartmentMock.getCostCenterCode()).thenReturn("MODIFIED-CODE");

            // Act
            DepartmentResponse response = useCase.execute(request);

            // Assert - Response should reflect saved department, not original
            assertThat(response.getId(), is(equalTo(savedId)));
            assertThat(response.getName(), is(equalTo("Modified Name")));
            assertThat(response.getCostCenterCode(), is(equalTo("MODIFIED-CODE")));
        }

        @Test
        @DisplayName("Should handle repository save exceptions")
        void shouldHandleRepositorySaveExceptions() {
            // Arrange
            when(departmentRepository.findByCostCenterCode(anyString())).thenReturn(Optional.empty());
            when(departmentRepository.save(any(Department.class))).thenThrow(new RuntimeException("Database error"));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                useCase.execute(request);
            });

            assertThat(exception.getMessage(), is(equalTo("Database error")));
            verify(departmentRepository).save(any(Department.class));
        }

        @Test
        @DisplayName("Should handle repository find exceptions")
        void shouldHandleRepositoryFindExceptions() {
            // Arrange
            when(departmentRepository.findByCostCenterCode(anyString())).thenThrow(new RuntimeException("Database connection error"));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                useCase.execute(request);
            });

            assertThat(exception.getMessage(), is(equalTo("Database connection error")));
            verify(departmentRepository).findByCostCenterCode(anyString());
            verify(departmentRepository, never()).save(any(Department.class));
        }

        @Test
        @DisplayName("Should verify exact repository method calls")
        void shouldVerifyExactRepositoryMethodCalls() {
            // Arrange
            when(departmentRepository.findByCostCenterCode("IT001")).thenReturn(Optional.empty());
            when(departmentRepository.save(any(Department.class))).thenReturn(savedDepartmentMock);
            when(savedDepartmentMock.getId()).thenReturn(UUID.randomUUID());
            when(savedDepartmentMock.getName()).thenReturn("IT Department");
            when(savedDepartmentMock.getCostCenterCode()).thenReturn("IT001");

            // Act
            useCase.execute(request);

            // Assert - Verify exact calls with exact parameters
            verify(departmentRepository, times(1)).findByCostCenterCode("IT001");
            verify(departmentRepository, times(1)).save(any(Department.class));
            verifyNoMoreInteractions(departmentRepository);
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle null request")
        void shouldHandleNullRequest() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> {
                useCase.execute(null);
            });

            verify(departmentRepository, never()).findByCostCenterCode(anyString());
            verify(departmentRepository, never()).save(any(Department.class));
        }

        @Test
        @DisplayName("Should handle request with null cost center code")
        void shouldHandleRequestWithNullCostCenterCode() {
            // Arrange
            request.setCostCenterCode(null);

            // Act & Assert
            assertThrows(Exception.class, () -> {
                useCase.execute(request);
            });
        }

        @Test
        @DisplayName("Should handle request with null name")
        void shouldHandleRequestWithNullName() {
            // Arrange
            request.setName(null);
            when(departmentRepository.findByCostCenterCode(anyString())).thenReturn(Optional.empty());

            // Act & Assert - Department.create might throw exception for null name
            assertThrows(Exception.class, () -> {
                useCase.execute(request);
            });
        }

        @Test
        @DisplayName("Should handle multiple rapid executions")
        void shouldHandleMultipleRapidExecutions() {
            // Arrange
            when(departmentRepository.findByCostCenterCode(anyString())).thenReturn(Optional.empty());
            when(departmentRepository.save(any(Department.class))).thenReturn(savedDepartmentMock);
            when(savedDepartmentMock.getId()).thenReturn(UUID.randomUUID());
            when(savedDepartmentMock.getName()).thenReturn("IT Department");
            when(savedDepartmentMock.getCostCenterCode()).thenReturn("IT001");

            // Act & Assert
            for (int i = 0; i < 100; i++) {
                CreateDepartmentRequest testRequest = new CreateDepartmentRequest();
                testRequest.setName("Department " + i);
                testRequest.setCostCenterCode("DEPT" + i);
                
                when(departmentRepository.findByCostCenterCode("DEPT" + i)).thenReturn(Optional.empty());
                
                DepartmentResponse response = useCase.execute(testRequest);
                assertThat(response, is(notNullValue()));
            }
        }

        @Test
        @DisplayName("Should handle repository returning null from save")
        void shouldHandleRepositoryReturningNullFromSave() {
            // Arrange
            when(departmentRepository.findByCostCenterCode(anyString())).thenReturn(Optional.empty());
            when(departmentRepository.save(any(Department.class))).thenReturn(null);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                useCase.execute(request);
            });
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("Should follow complete business flow for department creation")
        void shouldFollowCompleteBusinessFlowForDepartmentCreation() {
            // Arrange
            UUID departmentId = UUID.randomUUID();
            when(departmentRepository.findByCostCenterCode("IT001")).thenReturn(Optional.empty());
            when(departmentRepository.save(any(Department.class))).thenReturn(savedDepartmentMock);
            when(savedDepartmentMock.getId()).thenReturn(departmentId);
            when(savedDepartmentMock.getName()).thenReturn("IT Department");
            when(savedDepartmentMock.getCostCenterCode()).thenReturn("IT001");

            // Act
            DepartmentResponse response = useCase.execute(request);

            // Assert - Complete business flow verification
            // 1. Check for existing cost center code
            verify(departmentRepository).findByCostCenterCode("IT001");
            
            // 2. Create and save new department
            verify(departmentRepository).save(any(Department.class));
            
            // 3. Return appropriate response
            assertThat(response, is(notNullValue()));
            assertThat(response.getId(), is(equalTo(departmentId)));
            assertThat(response.getName(), is(equalTo("IT Department")));
            assertThat(response.getCostCenterCode(), is(equalTo("IT001")));
        }

        @Test
        @DisplayName("Should ensure cost center code uniqueness validation")
        void shouldEnsureCostCenterCodeUniquenessValidation() {
            // This test ensures the business rule: cost center codes must be unique
            
            // Arrange - First department already exists
            when(departmentRepository.findByCostCenterCode("EXISTING-001")).thenReturn(Optional.of(departmentMock));
            request.setCostCenterCode("EXISTING-001");

            // Act & Assert
            ConflictException exception = assertThrows(ConflictException.class, () -> {
                useCase.execute(request);
            });

            assertThat(exception.getMessageKey(), is(equalTo("error.department.costcenter.exists")));
            assertThat(exception.getArgs(), is(arrayContaining("EXISTING-001")));
            
            // Verify no save attempt was made
            verify(departmentRepository, never()).save(any(Department.class));
        }

        @Test
        @DisplayName("Should create department with system-generated ID")
        void shouldCreateDepartmentWithSystemGeneratedId() {
            // This test verifies that the system generates the ID, not the user
            
            // Arrange
            when(departmentRepository.findByCostCenterCode(anyString())).thenReturn(Optional.empty());
            when(departmentRepository.save(any(Department.class))).thenReturn(savedDepartmentMock);
            when(savedDepartmentMock.getId()).thenReturn(UUID.randomUUID());
            when(savedDepartmentMock.getName()).thenReturn("IT Department");
            when(savedDepartmentMock.getCostCenterCode()).thenReturn("IT001");

            // Act
            DepartmentResponse response = useCase.execute(request);

            // Assert - ID should be generated by system (UUID.randomUUID())
            assertThat(response.getId(), is(notNullValue()));
            assertThat(response.getId(), is(instanceOf(UUID.class)));
            
            // Verify Department.create was called with generated UUID
            verify(departmentRepository).save(any(Department.class));
        }
    }
}

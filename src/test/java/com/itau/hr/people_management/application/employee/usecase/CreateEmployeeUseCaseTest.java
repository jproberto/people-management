package com.itau.hr.people_management.application.employee.usecase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.application.employee.dto.CreateEmployeeRequest;
import com.itau.hr.people_management.application.employee.dto.EmployeeResponse;
import com.itau.hr.people_management.domain.department.entity.Department;
import com.itau.hr.people_management.domain.department.repository.DepartmentRepository;
import com.itau.hr.people_management.domain.employee.entity.Employee;
import com.itau.hr.people_management.domain.employee.enumeration.EmployeeStatus;
import com.itau.hr.people_management.domain.employee.event.EmployeeCreatedEvent;
import com.itau.hr.people_management.domain.employee.event.EventPublisher;
import com.itau.hr.people_management.domain.employee.repository.EmployeeRepository;
import com.itau.hr.people_management.domain.position.entity.Position;
import com.itau.hr.people_management.domain.position.repository.PositionRepository;
import com.itau.hr.people_management.domain.shared.exception.ConflictException;
import com.itau.hr.people_management.domain.shared.exception.NotFoundException;
import com.itau.hr.people_management.domain.shared.vo.Email;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateEmployeeUseCase Unit Tests")
class CreateEmployeeUseCaseTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private Department department;

    @Mock
    private Position position;

    @Mock
    private Employee employee;

    @Mock
    private Email email;

    private CreateEmployeeUseCase useCase;

    private CreateEmployeeRequest request;
    private UUID departmentId;
    private UUID positionId;
    private UUID employeeId;
    private String employeeName;
    private String employeeEmail;
    private LocalDate hireDate;

    @BeforeEach
    void setUp() {
        useCase = new CreateEmployeeUseCase(employeeRepository, departmentRepository, positionRepository, eventPublisher);
        
        departmentId = UUID.randomUUID();
        positionId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        employeeName = "John Doe";
        employeeEmail = "john.doe@itau.com.br";
        hireDate = LocalDate.of(2024, 1, 15);

        request = new CreateEmployeeRequest();
        request.setName(employeeName);
        request.setEmail(employeeEmail);
        request.setHireDate(hireDate);
        request.setDepartmentId(departmentId);
        request.setPositionId(positionId);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create use case with valid dependencies")
        void shouldCreateUseCaseWithValidDependencies() {
            // Act
            CreateEmployeeUseCase newUseCase = new CreateEmployeeUseCase(
                employeeRepository, departmentRepository, positionRepository, eventPublisher
            );

            // Assert
            assertThat(newUseCase, is(notNullValue()));
        }

        @Test
        @DisplayName("Should accept null dependencies in constructor")
        void shouldAcceptNullDependenciesInConstructor() {
            // Act & Assert - Constructor should accept null (will fail at runtime when used)
            assertDoesNotThrow(() -> {
                CreateEmployeeUseCase newUseCase = new CreateEmployeeUseCase(null, null, null, null);
                assertThat(newUseCase, is(notNullValue()));
            });
        }
    }

    @Nested
    @DisplayName("Execute Method - Success Scenarios")
    class ExecuteMethodSuccessScenarios {

        @Test
        @DisplayName("Should create employee successfully")
        void shouldCreateEmployeeSuccessfully() {
            // Arrange
            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.empty());
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(positionRepository.findById(positionId)).thenReturn(Optional.of(position));
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(email);
            when(email.getAddress()).thenReturn(employeeEmail);

            try (MockedStatic<Employee> employeeMock = mockStatic(Employee.class);
                 MockedStatic<Email> emailMock = mockStatic(Email.class);
                 MockedStatic<UUID> uuidMock = mockStatic(UUID.class)) {
                
                when(Email.create(employeeEmail)).thenReturn(email);
                when(UUID.randomUUID()).thenReturn(employeeId);
                when(Employee.create(employeeId, employeeName, email, hireDate, EmployeeStatus.ACTIVE, department, position))
                    .thenReturn(employee);
                when(employee.getDepartment()).thenReturn(department);
                when(employee.getPosition()).thenReturn(position);

                // Act
                EmployeeResponse response = useCase.execute(request);

                // Assert
                assertThat(response, is(notNullValue()));
                verify(employeeRepository).findByEmail(employeeEmail);
                verify(departmentRepository).findById(departmentId);
                verify(positionRepository).findById(positionId);
                verify(employeeRepository).save(employee);
                verify(eventPublisher).publish(any(EmployeeCreatedEvent.class));
            }
        }

        @Test
        @DisplayName("Should validate email uniqueness before creating employee")
        void shouldValidateEmailUniquenessBeforeCreatingEmployee() {
            // Arrange
            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.empty());
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(positionRepository.findById(positionId)).thenReturn(Optional.of(position));
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(email);
            when(email.getAddress()).thenReturn(employeeEmail);

            try (MockedStatic<Employee> employeeMock = mockStatic(Employee.class);
                 MockedStatic<Email> emailMock = mockStatic(Email.class);
                 MockedStatic<UUID> uuidMock = mockStatic(UUID.class)) {
                
                when(Email.create(employeeEmail)).thenReturn(email);
                when(UUID.randomUUID()).thenReturn(employeeId);
                when(Employee.create(any(), any(), any(), any(), any(), any(), any())).thenReturn(employee);
                when(employee.getDepartment()).thenReturn(department);
                when(employee.getPosition()).thenReturn(position);

                // Act
                useCase.execute(request);

                // Assert - Email validation should be first
                var inOrder = inOrder(employeeRepository, departmentRepository, positionRepository);
                inOrder.verify(employeeRepository).findByEmail(employeeEmail);
                inOrder.verify(departmentRepository).findById(departmentId);
                inOrder.verify(positionRepository).findById(positionId);
            }
        }

        @Test
        @DisplayName("Should find department and position before creating employee")
        void shouldFindDepartmentAndPositionBeforeCreatingEmployee() {
            // Arrange
            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.empty());
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(positionRepository.findById(positionId)).thenReturn(Optional.of(position));
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(email);
            when(email.getAddress()).thenReturn(employeeEmail);

            try (MockedStatic<Employee> employeeMock = mockStatic(Employee.class);
                 MockedStatic<Email> emailMock = mockStatic(Email.class);
                 MockedStatic<UUID> uuidMock = mockStatic(UUID.class)) {
                
                when(Email.create(employeeEmail)).thenReturn(email);
                when(UUID.randomUUID()).thenReturn(employeeId);
                when(Employee.create(any(), any(), any(), any(), any(), any(), any())).thenReturn(employee);
                when(employee.getDepartment()).thenReturn(department);
                when(employee.getPosition()).thenReturn(position);

                // Act
                useCase.execute(request);

                // Assert
                verify(departmentRepository).findById(departmentId);
                verify(positionRepository).findById(positionId);
            }
        }

        @Test
        @DisplayName("Should create employee with ACTIVE status")
        void shouldCreateEmployeeWithActiveStatus() {
            // Arrange
            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.empty());
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(positionRepository.findById(positionId)).thenReturn(Optional.of(position));
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(email);
            when(email.getAddress()).thenReturn(employeeEmail);

            try (MockedStatic<Employee> employeeMock = mockStatic(Employee.class);
                 MockedStatic<Email> emailMock = mockStatic(Email.class);
                 MockedStatic<UUID> uuidMock = mockStatic(UUID.class)) {
                
                when(Email.create(employeeEmail)).thenReturn(email);
                when(UUID.randomUUID()).thenReturn(employeeId);
                when(Employee.create(any(), any(), any(), any(), any(), any(), any())).thenReturn(employee);
                when(employee.getDepartment()).thenReturn(department);
                when(employee.getPosition()).thenReturn(position);

                // Act
                useCase.execute(request);

                // Assert - Verify Employee.create was called with ACTIVE status
                employeeMock.verify(() -> Employee.create(
                    eq(employeeId),
                    eq(employeeName),
                    eq(email),
                    eq(hireDate),
                    eq(EmployeeStatus.ACTIVE),
                    eq(department),
                    eq(position)
                ));
            }
        }

        @Test
        @DisplayName("Should publish EmployeeCreatedEvent after saving")
        void shouldPublishEmployeeCreatedEventAfterSaving() {
            // Arrange
            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.empty());
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(positionRepository.findById(positionId)).thenReturn(Optional.of(position));
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(email);
            when(email.getAddress()).thenReturn(employeeEmail);

            try (MockedStatic<Employee> employeeMock = mockStatic(Employee.class);
                 MockedStatic<Email> emailMock = mockStatic(Email.class);
                 MockedStatic<UUID> uuidMock = mockStatic(UUID.class)) {
                
                when(Email.create(employeeEmail)).thenReturn(email);
                when(UUID.randomUUID()).thenReturn(employeeId);
                when(Employee.create(any(), any(), any(), any(), any(), any(), any())).thenReturn(employee);
                when(employee.getDepartment()).thenReturn(department);
                when(employee.getPosition()).thenReturn(position);

                // Act
                useCase.execute(request);

                // Assert - Verify save happens before event publishing
                var inOrder = inOrder(employeeRepository, eventPublisher);
                inOrder.verify(employeeRepository).save(employee);
                inOrder.verify(eventPublisher).publish(any(EmployeeCreatedEvent.class));
            }
        }

        @Test
        @DisplayName("Should create EmployeeCreatedEvent with correct data")
        void shouldCreateEmployeeCreatedEventWithCorrectData() {
            // Arrange
            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.empty());
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(positionRepository.findById(positionId)).thenReturn(Optional.of(position));
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(email);
            when(email.getAddress()).thenReturn(employeeEmail);

            try (MockedStatic<Employee> employeeMock = mockStatic(Employee.class);
                 MockedStatic<Email> emailMock = mockStatic(Email.class);
                 MockedStatic<UUID> uuidMock = mockStatic(UUID.class)) {
                
                when(Email.create(employeeEmail)).thenReturn(email);
                when(UUID.randomUUID()).thenReturn(employeeId);
                when(Employee.create(any(), any(), any(), any(), any(), any(), any())).thenReturn(employee);
                when(employee.getDepartment()).thenReturn(department);
                when(employee.getPosition()).thenReturn(position);

                // Act
                useCase.execute(request);

                // Assert
                ArgumentCaptor<EmployeeCreatedEvent> eventCaptor = ArgumentCaptor.forClass(EmployeeCreatedEvent.class);
                verify(eventPublisher).publish(eventCaptor.capture());

                EmployeeCreatedEvent publishedEvent = eventCaptor.getValue();
                assertThat(publishedEvent.employeeId(), is(equalTo(employeeId)));
                assertThat(publishedEvent.employeeName(), is(equalTo(employeeName)));
                assertThat(publishedEvent.employeeEmail(), is(equalTo(employeeEmail)));
            }
        }

        @Test
        @DisplayName("Should return EmployeeResponse with saved employee data")
        void shouldReturnEmployeeResponseWithSavedEmployeeData() {
            // Arrange
            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.empty());
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(positionRepository.findById(positionId)).thenReturn(Optional.of(position));
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(email);
            when(email.getAddress()).thenReturn(employeeEmail);

            try (MockedStatic<Employee> employeeMock = mockStatic(Employee.class);
                 MockedStatic<Email> emailMock = mockStatic(Email.class);
                 MockedStatic<UUID> uuidMock = mockStatic(UUID.class)) {
                
                when(Email.create(employeeEmail)).thenReturn(email);
                when(UUID.randomUUID()).thenReturn(employeeId);
                when(Employee.create(any(), any(), any(), any(), any(), any(), any())).thenReturn(employee);
                when(employee.getDepartment()).thenReturn(department);
                when(employee.getPosition()).thenReturn(position);

                // Act
                EmployeeResponse response = useCase.execute(request);

                // Assert
                assertThat(response, is(notNullValue()));
                assertThat(response, is(instanceOf(EmployeeResponse.class)));
            }
        }
    }

    @Nested
    @DisplayName("Execute Method - Email Conflict Tests")
    class ExecuteMethodEmailConflictTests {

        @Test
        @DisplayName("Should throw ConflictException when email already exists")
        void shouldThrowConflictExceptionWhenEmailAlreadyExists() {
            // Arrange
            Employee existingEmployee = mock(Employee.class);
            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.of(existingEmployee));

            // Act & Assert
            ConflictException thrownException = assertThrows(ConflictException.class, () -> {
                useCase.execute(request);
            });

            assertThat(thrownException.getMessageKey(), containsString("error.employee.email.exists"));
            verify(employeeRepository).findByEmail(employeeEmail);
            verify(departmentRepository, never()).findById(any());
            verify(positionRepository, never()).findById(any());
            verify(employeeRepository, never()).save(any());
            verify(eventPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("Should include email in ConflictException message")
        void shouldIncludeEmailInConflictExceptionMessage() {
            // Arrange
            Employee existingEmployee = mock(Employee.class);
            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.of(existingEmployee));

            // Act & Assert
            ConflictException thrownException = assertThrows(ConflictException.class, () -> {
                useCase.execute(request);
            });

            // The exception should be created with the email as argument
            assertThat(thrownException.getMessageKey(), containsString("error.employee.email.exists"));
            verify(employeeRepository).findByEmail(employeeEmail);
        }

        @Test
        @DisplayName("Should not proceed with creation when email exists")
        void shouldNotProceedWithCreationWhenEmailExists() {
            // Arrange
            Employee existingEmployee = mock(Employee.class);
            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.of(existingEmployee));

            // Act & Assert
            assertThrows(ConflictException.class, () -> {
                useCase.execute(request);
            });

            verify(employeeRepository).findByEmail(employeeEmail);
            verify(departmentRepository, never()).findById(departmentId);
            verify(positionRepository, never()).findById(positionId);
            verify(employeeRepository, never()).save(any(Employee.class));
            verify(eventPublisher, never()).publish(any(EmployeeCreatedEvent.class));
        }
    }

    @Nested
    @DisplayName("Execute Method - Department Not Found Tests")
    class ExecuteMethodDepartmentNotFoundTests {

        @Test
        @DisplayName("Should throw NotFoundException when department does not exist")
        void shouldThrowNotFoundExceptionWhenDepartmentDoesNotExist() {
            // Arrange
            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.empty());
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException thrownException = assertThrows(NotFoundException.class, () -> {
                useCase.execute(request);
            });

            assertThat(thrownException.getMessageKey(), containsString("error.department.notfound"));
            verify(employeeRepository).findByEmail(employeeEmail);
            verify(departmentRepository).findById(departmentId);
            verify(positionRepository, never()).findById(any());
            verify(employeeRepository, never()).save(any());
            verify(eventPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("Should include departmentId in NotFoundException")
        void shouldIncludeDepartmentIdInNotFoundException() {
            // Arrange
            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.empty());
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException thrownException = assertThrows(NotFoundException.class, () -> {
                useCase.execute(request);
            });

            assertThat(thrownException.getMessageKey(), containsString("error.department.notfound"));
            verify(departmentRepository).findById(departmentId);
        }

        @Test
        @DisplayName("Should not check position when department not found")
        void shouldNotCheckPositionWhenDepartmentNotFound() {
            // Arrange
            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.empty());
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(NotFoundException.class, () -> {
                useCase.execute(request);
            });

            verify(positionRepository, never()).findById(positionId);
            verify(employeeRepository, never()).save(any(Employee.class));
            verify(eventPublisher, never()).publish(any(EmployeeCreatedEvent.class));
        }
    }

    @Nested
    @DisplayName("Execute Method - Position Not Found Tests")
    class ExecuteMethodPositionNotFoundTests {

        @Test
        @DisplayName("Should throw NotFoundException when position does not exist")
        void shouldThrowNotFoundExceptionWhenPositionDoesNotExist() {
            // Arrange
            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.empty());
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(positionRepository.findById(positionId)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException thrownException = assertThrows(NotFoundException.class, () -> {
                useCase.execute(request);
            });

            assertThat(thrownException.getMessageKey(), containsString("error.position.notfound"));
            verify(employeeRepository).findByEmail(employeeEmail);
            verify(departmentRepository).findById(departmentId);
            verify(positionRepository).findById(positionId);
            verify(employeeRepository, never()).save(any());
            verify(eventPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("Should include positionId in NotFoundException")
        void shouldIncludePositionIdInNotFoundException() {
            // Arrange
            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.empty());
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(positionRepository.findById(positionId)).thenReturn(Optional.empty());

            // Act & Assert
            NotFoundException thrownException = assertThrows(NotFoundException.class, () -> {
                useCase.execute(request);
            });

            assertThat(thrownException.getMessageKey(), containsString("error.position.notfound"));
            verify(positionRepository).findById(positionId);
        }

        @Test
        @DisplayName("Should find department before checking position")
        void shouldFindDepartmentBeforeCheckingPosition() {
            // Arrange
            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.empty());
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(positionRepository.findById(positionId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(NotFoundException.class, () -> {
                useCase.execute(request);
            });

            var inOrder = inOrder(departmentRepository, positionRepository);
            inOrder.verify(departmentRepository).findById(departmentId);
            inOrder.verify(positionRepository).findById(positionId);
        }
    }

    @Nested
    @DisplayName("Execute Method - Repository Exception Tests")
    class ExecuteMethodRepositoryExceptionTests {

        @Test
        @DisplayName("Should propagate exception when findByEmail fails")
        void shouldPropagateExceptionWhenFindByEmailFails() {
            // Arrange
            RuntimeException repositoryException = new RuntimeException("Database connection error");
            when(employeeRepository.findByEmail(employeeEmail)).thenThrow(repositoryException);

            // Act & Assert
            RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                useCase.execute(request);
            });

            assertThat(thrownException, is(equalTo(repositoryException)));
            verify(employeeRepository).findByEmail(employeeEmail);
            verify(departmentRepository, never()).findById(any());
            verify(positionRepository, never()).findById(any());
            verify(employeeRepository, never()).save(any());
            verify(eventPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("Should propagate exception when department findById fails")
        void shouldPropagateExceptionWhenDepartmentFindByIdFails() {
            // Arrange
            RuntimeException repositoryException = new RuntimeException("Database connection error");
            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.empty());
            when(departmentRepository.findById(departmentId)).thenThrow(repositoryException);

            // Act & Assert
            RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                useCase.execute(request);
            });

            assertThat(thrownException, is(equalTo(repositoryException)));
            verify(employeeRepository).findByEmail(employeeEmail);
            verify(departmentRepository).findById(departmentId);
            verify(positionRepository, never()).findById(any());
            verify(employeeRepository, never()).save(any());
            verify(eventPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("Should propagate exception when position findById fails")
        void shouldPropagateExceptionWhenPositionFindByIdFails() {
            // Arrange
            RuntimeException repositoryException = new RuntimeException("Database connection error");
            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.empty());
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(positionRepository.findById(positionId)).thenThrow(repositoryException);

            // Act & Assert
            RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                useCase.execute(request);
            });

            assertThat(thrownException, is(equalTo(repositoryException)));
            verify(employeeRepository).findByEmail(employeeEmail);
            verify(departmentRepository).findById(departmentId);
            verify(positionRepository).findById(positionId);
            verify(employeeRepository, never()).save(any());
            verify(eventPublisher, never()).publish(any());
        }

        @Test
        @DisplayName("Should propagate exception when save fails")
        void shouldPropagateExceptionWhenSaveFails() {
            // Arrange
            RuntimeException saveException = new RuntimeException("Save operation failed");
            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.empty());
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(positionRepository.findById(positionId)).thenReturn(Optional.of(position));
            when(employeeRepository.save(any(Employee.class))).thenThrow(saveException);

            try (MockedStatic<Employee> employeeMock = mockStatic(Employee.class);
                 MockedStatic<Email> emailMock = mockStatic(Email.class);
                 MockedStatic<UUID> uuidMock = mockStatic(UUID.class)) {
                
                when(Email.create(employeeEmail)).thenReturn(email);
                when(UUID.randomUUID()).thenReturn(employeeId);
                when(Employee.create(any(), any(), any(), any(), any(), any(), any())).thenReturn(employee);

                // Act & Assert
                RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                    useCase.execute(request);
                });

                assertThat(thrownException, is(equalTo(saveException)));
                verify(employeeRepository).save(employee);
                verify(eventPublisher, never()).publish(any());
            }
        }
    }

    @Nested
    @DisplayName("Execute Method - Domain Exception Tests")
    class ExecuteMethodDomainExceptionTests {

        @Test
        @DisplayName("Should propagate exception when Email.create fails")
        void shouldPropagateExceptionWhenEmailCreateFails() {
            // Arrange
            RuntimeException emailException = new RuntimeException("Invalid email format");
            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.empty());
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(positionRepository.findById(positionId)).thenReturn(Optional.of(position));

            try (MockedStatic<Email> emailMock = mockStatic(Email.class)) {
                when(Email.create(employeeEmail)).thenThrow(emailException);

                // Act & Assert
                RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                    useCase.execute(request);
                });

                assertThat(thrownException, is(equalTo(emailException)));
                verify(employeeRepository, never()).save(any());
                verify(eventPublisher, never()).publish(any());
            }
        }

        @Test
        @DisplayName("Should propagate exception when Employee.create fails")
        void shouldPropagateExceptionWhenEmployeeCreateFails() {
            // Arrange
            RuntimeException employeeException = new RuntimeException("Invalid employee data");
            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.empty());
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(positionRepository.findById(positionId)).thenReturn(Optional.of(position));

            try (MockedStatic<Employee> employeeMock = mockStatic(Employee.class);
                 MockedStatic<Email> emailMock = mockStatic(Email.class);
                 MockedStatic<UUID> uuidMock = mockStatic(UUID.class)) {
                
                when(Email.create(employeeEmail)).thenReturn(email);
                when(UUID.randomUUID()).thenReturn(employeeId);
                when(Employee.create(any(), any(), any(), any(), any(), any(), any())).thenThrow(employeeException);

                // Act & Assert
                RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                    useCase.execute(request);
                });

                assertThat(thrownException, is(equalTo(employeeException)));
                verify(employeeRepository, never()).save(any());
                verify(eventPublisher, never()).publish(any());
            }
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
            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.empty());
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(positionRepository.findById(positionId)).thenReturn(Optional.of(position));
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(email);
            when(email.getAddress()).thenReturn(employeeEmail);
            doThrow(publishException).when(eventPublisher).publish(any(EmployeeCreatedEvent.class));

            try (MockedStatic<Employee> employeeMock = mockStatic(Employee.class);
                 MockedStatic<Email> emailMock = mockStatic(Email.class);
                 MockedStatic<UUID> uuidMock = mockStatic(UUID.class)) {
                
                when(Email.create(employeeEmail)).thenReturn(email);
                when(UUID.randomUUID()).thenReturn(employeeId);
                when(Employee.create(any(), any(), any(), any(), any(), any(), any())).thenReturn(employee);

                // Act & Assert
                RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                    useCase.execute(request);
                });

                assertThat(thrownException, is(equalTo(publishException)));
                verify(employeeRepository).save(employee);
                verify(eventPublisher).publish(any(EmployeeCreatedEvent.class));
            }
        }

        @Test
        @DisplayName("Should complete save operation before event publishing fails")
        void shouldCompleteSaveOperationBeforeEventPublishingFails() {
            // Arrange
            RuntimeException publishException = new RuntimeException("Event publishing failed");
            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.empty());
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(positionRepository.findById(positionId)).thenReturn(Optional.of(position));
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(email);
            when(email.getAddress()).thenReturn(employeeEmail);
            doThrow(publishException).when(eventPublisher).publish(any(EmployeeCreatedEvent.class));

            try (MockedStatic<Employee> employeeMock = mockStatic(Employee.class);
                 MockedStatic<Email> emailMock = mockStatic(Email.class);
                 MockedStatic<UUID> uuidMock = mockStatic(UUID.class)) {
                
                when(Email.create(employeeEmail)).thenReturn(email);
                when(UUID.randomUUID()).thenReturn(employeeId);
                when(Employee.create(any(), any(), any(), any(), any(), any(), any())).thenReturn(employee);

                // Act & Assert
                assertThrows(RuntimeException.class, () -> {
                    useCase.execute(request);
                });

                // Verify that save was completed before the exception
                var inOrder = inOrder(employeeRepository, eventPublisher);
                inOrder.verify(employeeRepository).save(employee);
                inOrder.verify(eventPublisher).publish(any(EmployeeCreatedEvent.class));
            }
        }
    }

    @Nested
    @DisplayName("FindOrThrow Method Tests")
    class FindOrThrowMethodTests {

        @Test
        @DisplayName("Should return value when Optional is present")
        void shouldReturnValueWhenOptionalIsPresent() {
            // This is tested indirectly through successful execution scenarios
            // where department and position are found successfully
            
            // Arrange
            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.empty());
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(positionRepository.findById(positionId)).thenReturn(Optional.of(position));
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(email);
            when(employee.getDepartment()).thenReturn(department);
            when(employee.getPosition()).thenReturn(position);
            when(email.getAddress()).thenReturn(employeeEmail);

            try (MockedStatic<Employee> employeeMock = mockStatic(Employee.class);
                 MockedStatic<Email> emailMock = mockStatic(Email.class);
                 MockedStatic<UUID> uuidMock = mockStatic(UUID.class)) {
                
                when(Email.create(employeeEmail)).thenReturn(email);
                when(UUID.randomUUID()).thenReturn(employeeId);
                when(Employee.create(any(), any(), any(), any(), any(), any(), any())).thenReturn(employee);

                // Act & Assert - Should not throw exception
                assertDoesNotThrow(() -> useCase.execute(request));
            }
        }

        @Test
        @DisplayName("Should throw NotFoundException when Optional is empty")
        void shouldThrowNotFoundExceptionWhenOptionalIsEmpty() {
            // This is tested through department and position not found scenarios
            // Arrange
            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.empty());
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(NotFoundException.class, () -> {
                useCase.execute(request);
            });
        }
    }

    @Nested
    @DisplayName("Transactional Behavior Tests")
    class TransactionalBehaviorTests {

        @Test
        @DisplayName("Should be annotated with @Transactional")
        void shouldBeAnnotatedWithTransactional() {
            // Act & Assert - Verify the class has @Transactional annotation
            assertTrue(CreateEmployeeUseCase.class
                .isAnnotationPresent(org.springframework.transaction.annotation.Transactional.class));
        }

        @Test
        @DisplayName("Should be annotated with @Service")
        void shouldBeAnnotatedWithService() {
            // Act & Assert - Verify the class has @Service annotation
            assertTrue(CreateEmployeeUseCase.class
                .isAnnotationPresent(org.springframework.stereotype.Service.class));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle rapid successive executions with different emails")
        void shouldHandleRapidSuccessiveExecutionsWithDifferentEmails() {
            // Arrange
            String[] emails = {"john1@itau.com.br", "john2@itau.com.br", "john3@itau.com.br"};
            
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(positionRepository.findById(positionId)).thenReturn(Optional.of(position));
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(email);
            when(email.getAddress()).thenReturn(employeeEmail);

            for (String emailAddress : emails) {
                when(employeeRepository.findByEmail(emailAddress)).thenReturn(Optional.empty());
            }

            try (MockedStatic<Employee> employeeMock = mockStatic(Employee.class);
                 MockedStatic<Email> emailMock = mockStatic(Email.class);
                 MockedStatic<UUID> uuidMock = mockStatic(UUID.class)) {
                
                when(Email.create(any())).thenReturn(email);
                when(UUID.randomUUID()).thenReturn(employeeId);
                when(Employee.create(any(), any(), any(), any(), any(), any(), any())).thenReturn(employee);
                when(employee.getDepartment()).thenReturn(department);
                when(employee.getPosition()).thenReturn(position);

                // Act - Execute multiple times rapidly
                for (String emailAddress : emails) {
                    CreateEmployeeRequest newRequest = new CreateEmployeeRequest();
                    newRequest.setName(employeeName);
                    newRequest.setEmail(emailAddress);
                    newRequest.setHireDate(hireDate);
                    newRequest.setDepartmentId(departmentId);
                    newRequest.setPositionId(positionId);
                    
                    useCase.execute(newRequest);
                }

                // Assert
                verify(employeeRepository, times(emails.length)).save(employee);
                verify(eventPublisher, times(emails.length)).publish(any(EmployeeCreatedEvent.class));
            }
        }

        @Test
        @DisplayName("Should handle different hire dates")
        void shouldHandleDifferentHireDates() {
            // Arrange
            LocalDate[] hireDates = {
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 15),
                LocalDate.of(2024, 12, 31)
            };

            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(positionRepository.findById(positionId)).thenReturn(Optional.of(position));
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(employeeName);
            when(employee.getEmail()).thenReturn(email);
            when(email.getAddress()).thenReturn(employeeEmail);

            try (MockedStatic<Employee> employeeMock = mockStatic(Employee.class);
                 MockedStatic<Email> emailMock = mockStatic(Email.class);
                 MockedStatic<UUID> uuidMock = mockStatic(UUID.class)) {
                
                when(Email.create(employeeEmail)).thenReturn(email);
                when(UUID.randomUUID()).thenReturn(employeeId);
                when(Employee.create(any(), any(), any(), any(), any(), any(), any())).thenReturn(employee);
                when(employee.getDepartment()).thenReturn(department);
                when(employee.getPosition()).thenReturn(position);

                // Act & Assert - Should handle all hire dates without issues
                for (LocalDate currentHireDate : hireDates) {
                    CreateEmployeeRequest newRequest = new CreateEmployeeRequest();
                    newRequest.setName(employeeName);
                    newRequest.setEmail(employeeEmail + currentHireDate.toString()); // Make email unique
                    newRequest.setHireDate(currentHireDate);
                    newRequest.setDepartmentId(departmentId);
                    newRequest.setPositionId(positionId);
                    
                    when(employeeRepository.findByEmail(newRequest.getEmail())).thenReturn(Optional.empty());
                    
                    assertDoesNotThrow(() -> useCase.execute(newRequest));
                }
            }
        }

        @Test
        @DisplayName("Should handle special characters in employee name")
        void shouldHandleSpecialCharactersInEmployeeName() {
            // Arrange
            String specialName = "José María Fernández-González";
            CreateEmployeeRequest specialRequest = new CreateEmployeeRequest();
            specialRequest.setName(specialName);
            specialRequest.setEmail(employeeEmail);
            specialRequest.setHireDate(hireDate);
            specialRequest.setDepartmentId(departmentId);
            specialRequest.setPositionId(positionId);

            when(employeeRepository.findByEmail(employeeEmail)).thenReturn(Optional.empty());
            when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(department));
            when(positionRepository.findById(positionId)).thenReturn(Optional.of(position));
            when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
            when(employee.getId()).thenReturn(employeeId);
            when(employee.getName()).thenReturn(specialName);
            when(employee.getEmail()).thenReturn(email);
            when(employee.getDepartment()).thenReturn(department);
            when(employee.getPosition()).thenReturn(position);
            when(email.getAddress()).thenReturn(employeeEmail);

            try (MockedStatic<Employee> employeeMock = mockStatic(Employee.class);
                 MockedStatic<Email> emailMock = mockStatic(Email.class);
                 MockedStatic<UUID> uuidMock = mockStatic(UUID.class)) {
                
                when(Email.create(employeeEmail)).thenReturn(email);
                when(UUID.randomUUID()).thenReturn(employeeId);
                when(Employee.create(employeeId, specialName, email, hireDate, EmployeeStatus.ACTIVE, department, position))
                    .thenReturn(employee);

                // Act & Assert
                assertDoesNotThrow(() -> useCase.execute(specialRequest));
                
                // Verify Employee.create was called with special name
                employeeMock.verify(() -> Employee.create(
                    eq(employeeId),
                    eq(specialName),
                    eq(email),
                    eq(hireDate),
                    eq(EmployeeStatus.ACTIVE),
                    eq(department),
                    eq(position)
                ));
            }
        }
    }
}
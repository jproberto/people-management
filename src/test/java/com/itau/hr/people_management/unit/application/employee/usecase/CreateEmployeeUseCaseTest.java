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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.application.employee.dto.CreateEmployeeRequest;
import com.itau.hr.people_management.application.employee.dto.EmployeeResponse;
import com.itau.hr.people_management.application.employee.usecase.CreateEmployeeUseCase;
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

    @BeforeEach
    void setUp() {
        useCase = new CreateEmployeeUseCase(employeeRepository, departmentRepository, positionRepository, eventPublisher);
        
        request = new CreateEmployeeRequest();
        request.setName("John Doe");
        request.setEmail("john.doe@itau.com.br");
        request.setDepartmentId(UUID.randomUUID());
        request.setPositionId(UUID.randomUUID());
    }

    @Test
    @DisplayName("Should create employee successfully")
    void shouldCreateEmployeeSuccessfully() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        when(employeeRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(departmentRepository.findById(request.getDepartmentId())).thenReturn(Optional.of(department));
        when(positionRepository.findById(request.getPositionId())).thenReturn(Optional.of(position));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(employee.getId()).thenReturn(employeeId);
        when(employee.getName()).thenReturn(request.getName());
        when(employee.getEmail()).thenReturn(email);
        when(email.getAddress()).thenReturn(request.getEmail());
        when(employee.getDepartment()).thenReturn(department);
        when(employee.getPosition()).thenReturn(position);

        try (MockedStatic<Employee> employeeMock = mockStatic(Employee.class);
             MockedStatic<Email> emailMock = mockStatic(Email.class);
             MockedStatic<UUID> uuidMock = mockStatic(UUID.class)) {
            
            when(Email.create(request.getEmail())).thenReturn(email);
            when(UUID.randomUUID()).thenReturn(employeeId);
            when(Employee.create(employeeId, request.getName(), email, 
                    EmployeeStatus.ACTIVE, department, position)).thenReturn(employee);

            // Act
            EmployeeResponse response = useCase.execute(request);

            // Assert
            assertThat(response.getId(), is(employeeId));
            verify(employeeRepository).findByEmail(request.getEmail());
            verify(departmentRepository).findById(request.getDepartmentId());
            verify(positionRepository).findById(request.getPositionId());
            verify(employeeRepository).save(employee);
            verify(eventPublisher).publish(any(EmployeeCreatedEvent.class));
        }
    }

    @Test
    @DisplayName("Should throw ConflictException when email already exists")
    void shouldThrowConflictExceptionWhenEmailAlreadyExists() {
        // Arrange
        Employee existingEmployee = mock(Employee.class);
        when(employeeRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(existingEmployee));

        // Act & Assert
        ConflictException exception = assertThrows(ConflictException.class, () -> {
            useCase.execute(request);
        });

        assertThat(exception.getMessageKey(), containsString("error.employee.email.exists"));
        verify(departmentRepository, never()).findById(any());
        verify(positionRepository, never()).findById(any());
        verify(employeeRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Should throw NotFoundException when department does not exist")
    void shouldThrowNotFoundExceptionWhenDepartmentDoesNotExist() {
        // Arrange
        when(employeeRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(departmentRepository.findById(request.getDepartmentId())).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            useCase.execute(request);
        });

        assertThat(exception.getMessageKey(), containsString("error.department.notfound"));
        verify(positionRepository, never()).findById(any());
        verify(employeeRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Should throw NotFoundException when position does not exist")
    void shouldThrowNotFoundExceptionWhenPositionDoesNotExist() {
        // Arrange
        when(employeeRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(departmentRepository.findById(request.getDepartmentId())).thenReturn(Optional.of(department));
        when(positionRepository.findById(request.getPositionId())).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            useCase.execute(request);
        });

        assertThat(exception.getMessageKey(), containsString("error.position.notfound"));
        verify(employeeRepository, never()).save(any());
        verify(eventPublisher, never()).publish(any());
    }

    @Test
    @DisplayName("Should create employee with ACTIVE status")
    void shouldCreateEmployeeWithActiveStatus() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        when(employeeRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(departmentRepository.findById(request.getDepartmentId())).thenReturn(Optional.of(department));
        when(positionRepository.findById(request.getPositionId())).thenReturn(Optional.of(position));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(employee.getEmail()).thenReturn(email);

        try (MockedStatic<Employee> employeeMock = mockStatic(Employee.class);
             MockedStatic<Email> emailMock = mockStatic(Email.class);
             MockedStatic<UUID> uuidMock = mockStatic(UUID.class)) {
            
            when(Email.create(request.getEmail())).thenReturn(email);
            when(UUID.randomUUID()).thenReturn(employeeId);
            when(Employee.create(any(), any(), any(), any(), any(), any())).thenReturn(employee);

            // Act
            useCase.execute(request);

            // Assert
            employeeMock.verify(() -> Employee.create(
                eq(employeeId),
                eq(request.getName()),
                eq(email),
                eq(EmployeeStatus.ACTIVE),
                eq(department),
                eq(position)
            ));
        }
    }

    @Test
    @DisplayName("Should publish EmployeeCreatedEvent with correct data")
    void shouldPublishEmployeeCreatedEventWithCorrectData() {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        when(employeeRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(departmentRepository.findById(request.getDepartmentId())).thenReturn(Optional.of(department));
        when(positionRepository.findById(request.getPositionId())).thenReturn(Optional.of(position));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee);
        when(employee.getId()).thenReturn(employeeId);
        when(employee.getName()).thenReturn(request.getName());
        when(employee.getEmail()).thenReturn(email);
        when(email.getAddress()).thenReturn(request.getEmail());

        try (MockedStatic<Employee> employeeMock = mockStatic(Employee.class);
             MockedStatic<Email> emailMock = mockStatic(Email.class);
             MockedStatic<UUID> uuidMock = mockStatic(UUID.class)) {
            
            when(Email.create(request.getEmail())).thenReturn(email);
            when(UUID.randomUUID()).thenReturn(employeeId);
            when(Employee.create(any(), any(), any(), any(), any(), any())).thenReturn(employee);

            // Act
            useCase.execute(request);

            // Assert
            ArgumentCaptor<EmployeeCreatedEvent> eventCaptor = ArgumentCaptor.forClass(EmployeeCreatedEvent.class);
            verify(eventPublisher).publish(eventCaptor.capture());

            EmployeeCreatedEvent publishedEvent = eventCaptor.getValue();
            assertThat(publishedEvent.employeeId(), is(employeeId));
            assertThat(publishedEvent.employeeName(), is(request.getName()));
            assertThat(publishedEvent.employeeEmail(), is(request.getEmail()));
        }
    }

    @Test
    @DisplayName("Should not publish event when save fails")
    void shouldNotPublishEventWhenSaveFails() {
        // Arrange
        when(employeeRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(departmentRepository.findById(request.getDepartmentId())).thenReturn(Optional.of(department));
        when(positionRepository.findById(request.getPositionId())).thenReturn(Optional.of(position));
        when(employeeRepository.save(any(Employee.class))).thenThrow(new RuntimeException("Save failed"));

        try (MockedStatic<Employee> employeeMock = mockStatic(Employee.class);
             MockedStatic<Email> emailMock = mockStatic(Email.class);
             MockedStatic<UUID> uuidMock = mockStatic(UUID.class)) {
            
            when(Email.create(request.getEmail())).thenReturn(email);
            when(Employee.create(any(), any(), any(), any(), any(), any())).thenReturn(employee);

            // Act & Assert
            assertThrows(RuntimeException.class, () -> useCase.execute(request));
            verify(eventPublisher, never()).publish(any());
        }
    }
}
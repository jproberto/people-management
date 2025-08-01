package com.itau.hr.people_management.unit.infrastructure.shared.mapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.domain.department.entity.Department;
import com.itau.hr.people_management.domain.employee.entity.Employee;
import com.itau.hr.people_management.domain.employee.enumeration.EmployeeStatus;
import com.itau.hr.people_management.domain.position.entity.Position;
import com.itau.hr.people_management.domain.shared.vo.Email;
import com.itau.hr.people_management.infrastructure.department.entity.DepartmentJpaEntity;
import com.itau.hr.people_management.infrastructure.employee.entity.EmployeeJpaEntity;
import com.itau.hr.people_management.infrastructure.position.entity.PositionJpaEntity;
import com.itau.hr.people_management.infrastructure.shared.mapper.DepartmentMapper;
import com.itau.hr.people_management.infrastructure.shared.mapper.EmployeeMapper;
import com.itau.hr.people_management.infrastructure.shared.mapper.PositionMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeMapper Unit Tests")
class EmployeeMapperTest {

    @Mock
    private Employee domainEmployee;

    @Mock
    private EmployeeJpaEntity jpaEmployee;

    @Mock
    private Email email;

    @Mock
    private Department department;

    @Mock
    private Position position;

    @Mock
    private DepartmentJpaEntity departmentJpaEntity;

    @Mock
    private PositionJpaEntity positionJpaEntity;

    private UUID employeeId;
    private String name;
    private String emailAddress;
    private LocalDate hireDate;
    private EmployeeStatus status;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        name = "John Doe";
        emailAddress = "john.doe@example.com";
        hireDate = LocalDate.of(2023, 1, 15);
        status = EmployeeStatus.ACTIVE;
    }

    @Test
    @DisplayName("Should map domain to JPA entity with nested mappers")
    void shouldMapDomainToJpaEntityWithNestedMappers() {
        try (MockedStatic<DepartmentMapper> deptMapperMock = mockStatic(DepartmentMapper.class);
             MockedStatic<PositionMapper> posMapperMock = mockStatic(PositionMapper.class)) {
            
            // Arrange
            when(domainEmployee.getId()).thenReturn(employeeId);
            when(domainEmployee.getName()).thenReturn(name);
            when(domainEmployee.getEmail()).thenReturn(email);
            when(email.getAddress()).thenReturn(emailAddress);
            when(domainEmployee.getHireDate()).thenReturn(hireDate);
            when(domainEmployee.getStatus()).thenReturn(status);
            when(domainEmployee.getDepartment()).thenReturn(department);
            when(domainEmployee.getPosition()).thenReturn(position);
            
            deptMapperMock.when(() -> DepartmentMapper.toJpaEntity(department)).thenReturn(departmentJpaEntity);
            posMapperMock.when(() -> PositionMapper.toJpaEntity(position)).thenReturn(positionJpaEntity);

            // Act
            EmployeeJpaEntity result = EmployeeMapper.toJpaEntity(domainEmployee);

            // Assert
            assertThat(result.getId(), is(employeeId));
            assertThat(result.getName(), is(name));
            assertThat(result.getEmail(), is(emailAddress));
            assertThat(result.getHireDate(), is(hireDate));
            assertThat(result.getStatus(), is(status));
            assertThat(result.getDepartment(), is(sameInstance(departmentJpaEntity)));
            assertThat(result.getPosition(), is(sameInstance(positionJpaEntity)));
        }
    }

    @Test
    @DisplayName("Should map JPA entity to domain with nested mappers")
    void shouldMapJpaEntityToDomainWithNestedMappers() {
        try (MockedStatic<DepartmentMapper> deptMapperMock = mockStatic(DepartmentMapper.class);
             MockedStatic<PositionMapper> posMapperMock = mockStatic(PositionMapper.class);
             MockedStatic<Email> emailMock = mockStatic(Email.class)) {
            
            // Arrange
            when(jpaEmployee.getId()).thenReturn(employeeId);
            when(jpaEmployee.getName()).thenReturn(name);
            when(jpaEmployee.getEmail()).thenReturn(emailAddress);
            when(jpaEmployee.getHireDate()).thenReturn(hireDate);
            when(jpaEmployee.getStatus()).thenReturn(status);
            when(jpaEmployee.getDepartment()).thenReturn(departmentJpaEntity);
            when(jpaEmployee.getPosition()).thenReturn(positionJpaEntity);
            
            emailMock.when(() -> Email.create(emailAddress)).thenReturn(email);
            deptMapperMock.when(() -> DepartmentMapper.toDomainEntity(departmentJpaEntity)).thenReturn(department);
            posMapperMock.when(() -> PositionMapper.toDomainEntity(positionJpaEntity)).thenReturn(position);

            // Act
            Employee result = EmployeeMapper.toDomainEntity(jpaEmployee);

            // Assert
            assertThat(result.getId(), is(employeeId));
            assertThat(result.getName(), is(name));
            assertThat(result.getEmail(), is(sameInstance(email)));
            assertThat(result.getHireDate(), is(hireDate));
            assertThat(result.getStatus(), is(status));
            assertThat(result.getDepartment(), is(sameInstance(department)));
            assertThat(result.getPosition(), is(sameInstance(position)));
        }
    }

    @Test
    @DisplayName("Should handle null inputs")
    void shouldHandleNullInputs() {
        // Act & Assert
        assertThat(EmployeeMapper.toJpaEntity(null), is(nullValue()));
        assertThat(EmployeeMapper.toDomainEntity(null), is(nullValue()));
    }
}
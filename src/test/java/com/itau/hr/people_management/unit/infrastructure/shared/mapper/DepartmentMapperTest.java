package com.itau.hr.people_management.unit.infrastructure.shared.mapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.domain.department.entity.Department;
import com.itau.hr.people_management.infrastructure.persistence.entity.DepartmentJpaEntity;
import com.itau.hr.people_management.infrastructure.shared.mapper.DepartmentMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentMapper Unit Tests")
class DepartmentMapperTest {

    @Mock
    private Department domainDepartment;

    @Mock
    private DepartmentJpaEntity jpaEntity;

    private UUID departmentId;
    private String name;
    private String costCenterCode;

    @BeforeEach
    void setUp() {
        departmentId = UUID.randomUUID();
        name = "Engineering";
        costCenterCode = "ENG001";
    }

    @Test
    @DisplayName("Should map domain to JPA entity")
    void shouldMapDomainToJpaEntity() {
        // Arrange
        when(domainDepartment.getId()).thenReturn(departmentId);
        when(domainDepartment.getName()).thenReturn(name);
        when(domainDepartment.getCostCenterCode()).thenReturn(costCenterCode);

        // Act
        DepartmentJpaEntity result = DepartmentMapper.toJpaEntity(domainDepartment);

        // Assert
        assertThat(result.getId(), is(departmentId));
        assertThat(result.getName(), is(name));
        assertThat(result.getCostCenterCode(), is(costCenterCode));
    }

    @Test
    @DisplayName("Should map JPA entity to domain")
    void shouldMapJpaEntityToDomain() {
        // Arrange
        when(jpaEntity.getId()).thenReturn(departmentId);
        when(jpaEntity.getName()).thenReturn(name);
        when(jpaEntity.getCostCenterCode()).thenReturn(costCenterCode);

        // Act
        Department result = DepartmentMapper.toDomainEntity(jpaEntity);

        // Assert
        assertThat(result.getId(), is(departmentId));
        assertThat(result.getName(), is(name));
        assertThat(result.getCostCenterCode(), is(costCenterCode));
    }

    @Test
    @DisplayName("Should handle null inputs")
    void shouldHandleNullInputs() {
        // Act & Assert
        assertThat(DepartmentMapper.toJpaEntity(null), is(nullValue()));
        assertThat(DepartmentMapper.toDomainEntity(null), is(nullValue()));
    }
}
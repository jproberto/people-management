package com.itau.hr.people_management.unit.infrastructure.persistence.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

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

import com.itau.hr.people_management.domain.department.entity.Department;
import com.itau.hr.people_management.infrastructure.persistence.entity.DepartmentJpaEntity;
import com.itau.hr.people_management.infrastructure.persistence.repository.DepartmentRepositoryImpl;
import com.itau.hr.people_management.infrastructure.persistence.repository.JpaDepartmentRepository;
import com.itau.hr.people_management.infrastructure.shared.mapper.DepartmentMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentRepositoryImpl Unit Tests")
class DepartmentRepositoryImplTest {

    @Mock
    private JpaDepartmentRepository jpaDepartmentRepository;

    @Mock
    private Department department;

    @Mock
    private DepartmentJpaEntity jpaEntity;

    private DepartmentRepositoryImpl repository;
    private UUID departmentId;
    private String costCenterCode;

    @BeforeEach
    void setUp() {
        repository = new DepartmentRepositoryImpl(jpaDepartmentRepository);
        departmentId = UUID.randomUUID();
        costCenterCode = "CC001";
    }

    @Nested
    @DisplayName("FindById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should return department when found")
        void shouldReturnDepartmentWhenFound() {
            try (MockedStatic<DepartmentMapper> mapperMock = mockStatic(DepartmentMapper.class)) {
                // Arrange
                when(jpaDepartmentRepository.findById(departmentId)).thenReturn(Optional.of(jpaEntity));
                mapperMock.when(() -> DepartmentMapper.toDomainEntity(jpaEntity)).thenReturn(department);

                // Act
                Optional<Department> result = repository.findById(departmentId);

                // Assert
                assertThat(result.isPresent(), is(true));
                assertThat(result.get(), is(sameInstance(department)));
            }
        }

        @Test
        @DisplayName("Should return empty when not found")
        void shouldReturnEmptyWhenNotFound() {
            // Arrange
            when(jpaDepartmentRepository.findById(departmentId)).thenReturn(Optional.empty());

            // Act
            Optional<Department> result = repository.findById(departmentId);

            // Assert
            assertThat(result.isEmpty(), is(true));
        }
    }

    @Nested
    @DisplayName("Save Tests")
    class SaveTests {

        @Test
        @DisplayName("Should save and return mapped department")
        void shouldSaveAndReturnMappedDepartment() {
            try (MockedStatic<DepartmentMapper> mapperMock = mockStatic(DepartmentMapper.class)) {
                // Arrange
                mapperMock.when(() -> DepartmentMapper.toJpaEntity(department)).thenReturn(jpaEntity);
                when(jpaDepartmentRepository.save(jpaEntity)).thenReturn(jpaEntity);
                mapperMock.when(() -> DepartmentMapper.toDomainEntity(jpaEntity)).thenReturn(department);

                // Act
                Department result = repository.save(department);

                // Assert
                assertThat(result, is(sameInstance(department)));
            }
        }
    }

    @Nested
    @DisplayName("Delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete mapped JPA entity")
        void shouldDeleteMappedJpaEntity() {
            try (MockedStatic<DepartmentMapper> mapperMock = mockStatic(DepartmentMapper.class)) {
                // Arrange
                mapperMock.when(() -> DepartmentMapper.toJpaEntity(department)).thenReturn(jpaEntity);

                // Act
                repository.delete(department);

                // Assert
                verify(jpaDepartmentRepository).delete(jpaEntity);
            }
        }
    }

    @Nested
    @DisplayName("FindAll Tests")
    class FindAllTests {

        @Test
        @DisplayName("Should return mapped departments list")
        void shouldReturnMappedDepartmentsList() {
            try (MockedStatic<DepartmentMapper> mapperMock = mockStatic(DepartmentMapper.class)) {
                // Arrange
                List<DepartmentJpaEntity> jpaEntities = List.of(jpaEntity);
                when(jpaDepartmentRepository.findAll()).thenReturn(jpaEntities);
                mapperMock.when(() -> DepartmentMapper.toDomainEntity(jpaEntity)).thenReturn(department);

                // Act
                List<Department> result = repository.findAll();

                // Assert
                assertThat(result, hasSize(1));
            }
        }

        @Test
        @DisplayName("Should return empty list when no departments found")
        void shouldReturnEmptyListWhenNoDepartmentsFound() {
            // Arrange
            when(jpaDepartmentRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<Department> result = repository.findAll();

            // Assert
            assertThat(result, is(empty()));
        }
    }

    @Nested
    @DisplayName("FindByCostCenterCode Tests")
    class FindByCostCenterCodeTests {

        @Test
        @DisplayName("Should return department when found by cost center code")
        void shouldReturnDepartmentWhenFoundByCostCenterCode() {
            try (MockedStatic<DepartmentMapper> mapperMock = mockStatic(DepartmentMapper.class)) {
                // Arrange
                when(jpaDepartmentRepository.findByCostCenterCode(costCenterCode)).thenReturn(Optional.of(jpaEntity));
                mapperMock.when(() -> DepartmentMapper.toDomainEntity(jpaEntity)).thenReturn(department);

                // Act
                Optional<Department> result = repository.findByCostCenterCode(costCenterCode);

                // Assert
                assertThat(result.isPresent(), is(true));
                assertThat(result.get(), is(sameInstance(department)));
            }
        }

        @Test
        @DisplayName("Should return empty when not found by cost center code")
        void shouldReturnEmptyWhenNotFoundByCostCenterCode() {
            // Arrange
            when(jpaDepartmentRepository.findByCostCenterCode(costCenterCode)).thenReturn(Optional.empty());

            // Act
            Optional<Department> result = repository.findByCostCenterCode(costCenterCode);

            // Assert
            assertThat(result.isEmpty(), is(true));
        }
    }
}
package com.itau.hr.people_management.unit.infrastructure.employee.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.data.jpa.domain.Specification;

import com.itau.hr.people_management.domain.employee.criteria.EmployeeSearchCriteria;
import com.itau.hr.people_management.domain.employee.entity.Employee;
import com.itau.hr.people_management.infrastructure.employee.entity.EmployeeJpaEntity;
import com.itau.hr.people_management.infrastructure.employee.repository.EmployeeRepositoryImpl;
import com.itau.hr.people_management.infrastructure.employee.repository.JpaEmployeeRepository;
import com.itau.hr.people_management.infrastructure.employee.specification.EmployeeSpecification;
import com.itau.hr.people_management.infrastructure.shared.mapper.EmployeeMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeRepositoryImpl Unit Tests")
class EmployeeRepositoryImplTest {

    @Mock
    private JpaEmployeeRepository jpaEmployeeRepository;

    @Mock
    private Employee employee;

    @Mock
    private EmployeeJpaEntity jpaEntity;

    @Mock
    private EmployeeSearchCriteria searchCriteria;

    @Mock
    private Specification<EmployeeJpaEntity> specification;

    private EmployeeRepositoryImpl repository;
    private UUID employeeId;
    private String email;

    @BeforeEach
    void setUp() {
        repository = new EmployeeRepositoryImpl(jpaEmployeeRepository);
        employeeId = UUID.randomUUID();
        email = "john.doe@example.com";
    }

    @Nested
    @DisplayName("FindById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should return employee when found")
        void shouldReturnEmployeeWhenFound() {
            try (MockedStatic<EmployeeMapper> mapperMock = mockStatic(EmployeeMapper.class)) {
                // Arrange
                when(jpaEmployeeRepository.findById(employeeId)).thenReturn(Optional.of(jpaEntity));
                mapperMock.when(() -> EmployeeMapper.toDomainEntity(jpaEntity)).thenReturn(employee);

                // Act
                Optional<Employee> result = repository.findById(employeeId);

                // Assert
                assertThat(result.isPresent(), is(true));
                assertThat(result.get(), is(sameInstance(employee)));
            }
        }

        @Test
        @DisplayName("Should return empty when not found")
        void shouldReturnEmptyWhenNotFound() {
            // Arrange
            when(jpaEmployeeRepository.findById(employeeId)).thenReturn(Optional.empty());

            // Act
            Optional<Employee> result = repository.findById(employeeId);

            // Assert
            assertThat(result.isEmpty(), is(true));
        }
    }

    @Nested
    @DisplayName("FindAll Tests")
    class FindAllTests {

        @Test
        @DisplayName("Should return mapped employees list")
        void shouldReturnMappedEmployeesList() {
            try (MockedStatic<EmployeeMapper> mapperMock = mockStatic(EmployeeMapper.class)) {
                // Arrange
                List<EmployeeJpaEntity> jpaEntities = List.of(jpaEntity);
                when(jpaEmployeeRepository.findAll()).thenReturn(jpaEntities);
                mapperMock.when(() -> EmployeeMapper.toDomainEntity(jpaEntity)).thenReturn(employee);

                // Act
                List<Employee> result = repository.findAll();

                // Assert
                assertThat(result, hasSize(1));
            }
        }

        @Test
        @DisplayName("Should return empty list when no employees found")
        void shouldReturnEmptyListWhenNoEmployeesFound() {
            // Arrange
            when(jpaEmployeeRepository.findAll()).thenReturn(Collections.emptyList());

            // Act
            List<Employee> result = repository.findAll();

            // Assert
            assertThat(result, is(empty()));
        }
    }

    @Nested
    @DisplayName("Save Tests")
    class SaveTests {

        @Test
        @DisplayName("Should save and return mapped employee")
        void shouldSaveAndReturnMappedEmployee() {
            try (MockedStatic<EmployeeMapper> mapperMock = mockStatic(EmployeeMapper.class)) {
                // Arrange
                mapperMock.when(() -> EmployeeMapper.toJpaEntity(employee)).thenReturn(jpaEntity);
                when(jpaEmployeeRepository.save(jpaEntity)).thenReturn(jpaEntity);
                mapperMock.when(() -> EmployeeMapper.toDomainEntity(jpaEntity)).thenReturn(employee);

                // Act
                Employee result = repository.save(employee);

                // Assert
                assertThat(result, is(sameInstance(employee)));
            }
        }
    }

    @Nested
    @DisplayName("Delete Tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete mapped JPA entity")
        void shouldDeleteMappedJpaEntity() {
            try (MockedStatic<EmployeeMapper> mapperMock = mockStatic(EmployeeMapper.class)) {
                // Arrange
                mapperMock.when(() -> EmployeeMapper.toJpaEntity(employee)).thenReturn(jpaEntity);

                // Act
                repository.delete(employee);

                // Assert
                verify(jpaEmployeeRepository).delete(jpaEntity);
            }
        }
    }

    @Nested
    @DisplayName("Search Tests")
    class SearchTests {

        @Test
        @DisplayName("Should search and return mapped employees")
        void shouldSearchAndReturnMappedEmployees() {
            try (MockedStatic<EmployeeMapper> mapperMock = mockStatic(EmployeeMapper.class);
                 MockedStatic<EmployeeSpecification> specMock = mockStatic(EmployeeSpecification.class)) {
                
                // Arrange
                List<EmployeeJpaEntity> jpaEntities = List.of(jpaEntity);
                specMock.when(() -> EmployeeSpecification.search(searchCriteria)).thenReturn(specification);
                when(jpaEmployeeRepository.findAll(specification)).thenReturn(jpaEntities);
                mapperMock.when(() -> EmployeeMapper.toDomainEntity(jpaEntity)).thenReturn(employee);

                // Act
                List<Employee> result = repository.search(searchCriteria);

                // Assert
                assertThat(result, hasSize(1));
            }
        }

        @Test
        @DisplayName("Should return empty list when search returns no results")
        void shouldReturnEmptyListWhenSearchReturnsNoResults() {
            try (MockedStatic<EmployeeSpecification> specMock = mockStatic(EmployeeSpecification.class)) {
                // Arrange
                specMock.when(() -> EmployeeSpecification.search(searchCriteria)).thenReturn(specification);
                when(jpaEmployeeRepository.findAll(specification)).thenReturn(Collections.emptyList());

                // Act
                List<Employee> result = repository.search(searchCriteria);

                // Assert
                assertThat(result, is(empty()));
            }
        }
    }

    @Nested
    @DisplayName("FindByEmail Tests")
    class FindByEmailTests {

        @Test
        @DisplayName("Should return employee when found by email")
        void shouldReturnEmployeeWhenFoundByEmail() {
            try (MockedStatic<EmployeeMapper> mapperMock = mockStatic(EmployeeMapper.class)) {
                // Arrange
                when(jpaEmployeeRepository.findByEmail(email)).thenReturn(Optional.of(jpaEntity));
                mapperMock.when(() -> EmployeeMapper.toDomainEntity(jpaEntity)).thenReturn(employee);

                // Act
                Optional<Employee> result = repository.findByEmail(email);

                // Assert
                assertThat(result.isPresent(), is(true));
                assertThat(result.get(), is(sameInstance(employee)));
            }
        }

        @Test
        @DisplayName("Should return empty when not found by email")
        void shouldReturnEmptyWhenNotFoundByEmail() {
            // Arrange
            when(jpaEmployeeRepository.findByEmail(email)).thenReturn(Optional.empty());

            // Act
            Optional<Employee> result = repository.findByEmail(email);

            // Assert
            assertThat(result.isEmpty(), is(true));
        }
    }
}
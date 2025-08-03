package com.itau.hr.people_management.integration.infrastructure.persistence.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.itau.hr.people_management.domain.department.entity.Department;
import com.itau.hr.people_management.domain.employee.criteria.EmployeeSearchCriteria;
import com.itau.hr.people_management.domain.employee.entity.Employee;
import com.itau.hr.people_management.domain.employee.enumeration.EmployeeStatus;
import com.itau.hr.people_management.domain.position.entity.Position;
import com.itau.hr.people_management.domain.position.enumeration.PositionLevel;
import com.itau.hr.people_management.domain.shared.vo.Email;
import com.itau.hr.people_management.infrastructure.persistence.entity.DepartmentJpaEntity;
import com.itau.hr.people_management.infrastructure.persistence.entity.EmployeeJpaEntity;
import com.itau.hr.people_management.infrastructure.persistence.entity.PositionJpaEntity;
import com.itau.hr.people_management.infrastructure.persistence.repository.EmployeeRepositoryImpl;
import com.itau.hr.people_management.infrastructure.persistence.repository.JpaEmployeeRepository;
import com.itau.hr.people_management.infrastructure.shared.mapper.DepartmentMapper;
import com.itau.hr.people_management.infrastructure.shared.mapper.PositionMapper;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackages = {"com.itau.hr.people_management.infrastructure.persistence.entity"})
@DisplayName("EmployeeRepositoryImpl Integration Tests with TestContainers")
class EmployeeRepositoryImplIntegrationTest {

    @SuppressWarnings("resource")
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("people_management_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("db/migration/V1__create_initial_tables.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JpaEmployeeRepository jpaEmployeeRepository;

    private EmployeeRepositoryImpl employeeRepository;
    private Employee testEmployee;
    private UUID employeeId;
    private UUID departmentId;
    private UUID positionId;
    private DepartmentJpaEntity departmentEntity;
    private PositionJpaEntity positionEntity;

    @BeforeEach
    void setUp() {
        employeeRepository = new EmployeeRepositoryImpl(jpaEmployeeRepository);
        
        // Setup dependencies
        departmentId = UUID.randomUUID();
        positionId = UUID.randomUUID();
        
        departmentEntity = new DepartmentJpaEntity(departmentId, "IT Department", "IT001");
        positionEntity = PositionJpaEntity.builder()
            .id(positionId)
            .title("Software Engineer")
            .positionLevel(PositionLevel.SENIOR)
            .build();
        
        entityManager.persist(departmentEntity);
        entityManager.persist(positionEntity);
        entityManager.flush();
        
        // Setup test employee
        Department department = DepartmentMapper.toDomainEntity(departmentEntity);
        Position position = PositionMapper.toDomainEntity(positionEntity);

        employeeId = UUID.randomUUID();
        testEmployee = Employee.create(
            employeeId,
            "John Doe",
            Email.create("john.doe@example.com"),
            EmployeeStatus.ACTIVE,
            department,
            position
        );
    }

    @Test
    @DisplayName("Should save employee and map correctly between domain and JPA entity")
    void shouldSaveEmployeeAndMapCorrectlyBetweenDomainAndJpaEntity() {
        // Act
        Employee savedEmployee = employeeRepository.save(testEmployee);

        // Assert
        assertThat(savedEmployee.getId(), is(employeeId));
        assertThat(savedEmployee.getName(), is("John Doe"));
        assertThat(savedEmployee.getEmail().getAddress(), is("john.doe@example.com"));
        assertThat(savedEmployee.getStatus(), is(EmployeeStatus.ACTIVE));

        // Verify JPA entity was persisted correctly
        EmployeeJpaEntity jpaEntity = entityManager.find(EmployeeJpaEntity.class, employeeId);
        assertThat(jpaEntity, is(notNullValue()));
        assertThat(jpaEntity.getName(), is("John Doe"));
        assertThat(jpaEntity.getEmail(), is("john.doe@example.com"));
        assertThat(jpaEntity.getDepartment(), is(departmentEntity));
        assertThat(jpaEntity.getPosition(), is(positionEntity));
    }

    @Test
    @DisplayName("Should find employee by ID and map from JPA to domain entity")
    void shouldFindEmployeeByIdAndMapFromJpaToDomainEntity() {
        // Arrange
        EmployeeJpaEntity jpaEntity = EmployeeJpaEntity.builder()
            .id(employeeId)
            .name("John Doe")
            .email("john.doe@example.com")
            .status(EmployeeStatus.ACTIVE)
            .department(departmentEntity)
            .position(positionEntity)
            .build();
        entityManager.persistAndFlush(jpaEntity);

        // Act
        Optional<Employee> result = employeeRepository.findById(employeeId);

        // Assert
        assertThat(result.isPresent(), is(true));
        Employee foundEmployee = result.get();
        assertThat(foundEmployee.getId(), is(employeeId));
        assertThat(foundEmployee.getName(), is("John Doe"));
        assertThat(foundEmployee.getEmail().getAddress(), is("john.doe@example.com"));
        assertThat(foundEmployee.getStatus(), is(EmployeeStatus.ACTIVE));
    }

    @Test
    @DisplayName("Should return empty optional when employee not found by ID")
    void shouldReturnEmptyOptionalWhenEmployeeNotFoundById() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();

        // Act
        Optional<Employee> result = employeeRepository.findById(nonExistentId);

        // Assert
        assertThat(result.isPresent(), is(false));
    }

    @Test
    @DisplayName("Should find all employees and map collection correctly")
    void shouldFindAllEmployeesAndMapCollectionCorrectly() {
        // Arrange
        EmployeeJpaEntity jpaEntity1 = EmployeeJpaEntity.builder()
            .id(UUID.randomUUID())
            .name("John Doe")
            .email("john.doe@example.com")
            .status(EmployeeStatus.ACTIVE)
            .department(departmentEntity)
            .position(positionEntity)
            .build();
        EmployeeJpaEntity jpaEntity2 = EmployeeJpaEntity.builder()
            .id(UUID.randomUUID())
            .name("Jane Smith")
            .email("jane.smith@example.com")
            .status(EmployeeStatus.ACTIVE)
            .department(departmentEntity)
            .position(positionEntity)
            .build();
        entityManager.persist(jpaEntity1);
        entityManager.persist(jpaEntity2);
        entityManager.flush();

        // Act
        List<Employee> result = employeeRepository.findAll();

        // Assert
        assertThat(result, hasSize(2));
        assertThat(result.stream().map(Employee::getName).toList(), 
                   containsInAnyOrder("John Doe", "Jane Smith"));
        assertThat(result.stream().map(e -> e.getEmail().getAddress()).toList(), 
                   containsInAnyOrder("john.doe@example.com", "jane.smith@example.com"));
    }

    @Test
    @DisplayName("Should find employee by email")
    void shouldFindEmployeeByEmail() {
        // Arrange
        EmployeeJpaEntity jpaEntity = EmployeeJpaEntity.builder()
            .id(employeeId)
            .name("John Doe")
            .email("john.doe@example.com")
            .status(EmployeeStatus.ACTIVE)
            .department(departmentEntity)
            .position(positionEntity)
            .build();
        entityManager.persistAndFlush(jpaEntity);

        // Act
        Optional<Employee> result = employeeRepository.findByEmail("john.doe@example.com");

        // Assert
        assertThat(result.isPresent(), is(true));
        Employee foundEmployee = result.get();
        assertThat(foundEmployee.getId(), is(employeeId));
        assertThat(foundEmployee.getEmail().getAddress(), is("john.doe@example.com"));
    }

    @Test
    @DisplayName("Should return empty when email not found")
    void shouldReturnEmptyWhenEmailNotFound() {
        // Act
        Optional<Employee> result = employeeRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertThat(result.isPresent(), is(false));
    }

    @Test
    @DisplayName("Should search employees using criteria and specifications")
    void shouldSearchEmployeesUsingCriteriaAndSpecifications() {
        // Arrange
        EmployeeJpaEntity activeEmployee = EmployeeJpaEntity.builder()
            .id(UUID.randomUUID())
            .name("John Doe")
            .email("john.doe@example.com")
            .status(EmployeeStatus.ACTIVE)
            .department(departmentEntity)
            .position(positionEntity)
            .build();
        EmployeeJpaEntity inactiveEmployee = EmployeeJpaEntity.builder()
            .id(UUID.randomUUID())
            .name("Jane Smith")
            .email("jane.smith@example.com")
            .status(EmployeeStatus.TERMINATED)
            .department(departmentEntity)
            .position(positionEntity)
            .build();
        entityManager.persist(activeEmployee);
        entityManager.persist(inactiveEmployee);
        entityManager.flush();

        // Act - Search by status
        EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder()
            .employeeStatus(EmployeeStatus.ACTIVE)
            .build();
        List<Employee> result = employeeRepository.search(criteria);

        // Assert
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getName(), is("John Doe"));
        assertThat(result.get(0).getStatus(), is(EmployeeStatus.ACTIVE));
    }

    @Test
    @DisplayName("Should search employees by name using specifications")
    void shouldSearchEmployeesByNameUsingSpecifications() {
        // Arrange
        EmployeeJpaEntity employee1 = EmployeeJpaEntity.builder()
            .id(UUID.randomUUID())
            .name("John Doe")
            .email("john.doe@example.com")
            .status(EmployeeStatus.ACTIVE)
            .department(departmentEntity)
            .position(positionEntity)
            .build();
        EmployeeJpaEntity employee2 = EmployeeJpaEntity.builder()
            .id(UUID.randomUUID())
            .name("John Smith")
            .email("john.smith@example.com")
            .status(EmployeeStatus.ACTIVE)
            .department(departmentEntity)
            .position(positionEntity)
            .build();
        entityManager.persist(employee1);
        entityManager.persist(employee2);
        entityManager.flush();

        // Act - Search by name pattern
        EmployeeSearchCriteria criteria = EmployeeSearchCriteria.builder()
            .name("John")
            .build();
        List<Employee> result = employeeRepository.search(criteria);

        // Assert
        assertThat(result, hasSize(2));
        assertThat(result.stream().map(Employee::getName).toList(), 
                   containsInAnyOrder("John Doe", "John Smith"));
    }

    @Test
    @DisplayName("Should delete employee and remove from database")
    void shouldDeleteEmployeeAndRemoveFromDatabase() {
        // Arrange
        EmployeeJpaEntity jpaEntity = EmployeeJpaEntity.builder()
            .id(employeeId)
            .name("John Doe")
            .email("john.doe@example.com")
            .status(EmployeeStatus.ACTIVE)
            .department(departmentEntity)
            .position(positionEntity)
            .build();
        entityManager.persistAndFlush(jpaEntity);

        // Act
        employeeRepository.delete(testEmployee);
        entityManager.flush();

        // Assert
        EmployeeJpaEntity deletedEntity = entityManager.find(EmployeeJpaEntity.class, employeeId);
        assertThat(deletedEntity, is(nullValue()));
    }

    @Test
    @DisplayName("Should handle save and retrieve cycle maintaining data integrity")
    void shouldHandleSaveAndRetrieveCycleMaintainingDataIntegrity() {
        // Act - Save → Retrieve → Verify full cycle
        Employee savedEmployee = employeeRepository.save(testEmployee);
        Optional<Employee> retrievedEmployee = employeeRepository.findById(savedEmployee.getId());

        // Assert - Full cycle integrity
        assertThat(retrievedEmployee.isPresent(), is(true));
        Employee finalEmployee = retrievedEmployee.get();
        assertThat(finalEmployee.getId(), is(testEmployee.getId()));
        assertThat(finalEmployee.getName(), is(testEmployee.getName()));
        assertThat(finalEmployee.getEmail(), is(testEmployee.getEmail()));
        assertThat(finalEmployee.getStatus(), is(testEmployee.getStatus()));
        assertThat(finalEmployee.getDepartment(), is(testEmployee.getDepartment()));
        assertThat(finalEmployee.getPosition(), is(testEmployee.getPosition()));
    }
}
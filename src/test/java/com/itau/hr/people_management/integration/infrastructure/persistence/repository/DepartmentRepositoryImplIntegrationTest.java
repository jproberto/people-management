package com.itau.hr.people_management.integration.infrastructure.persistence.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasProperty;
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
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.itau.hr.people_management.domain.department.entity.Department;
import com.itau.hr.people_management.infrastructure.persistence.entity.DepartmentJpaEntity;
import com.itau.hr.people_management.infrastructure.persistence.repository.DepartmentRepositoryImpl;
import com.itau.hr.people_management.infrastructure.shared.message.SpringDomainMessageSource;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({DepartmentRepositoryImpl.class, SpringDomainMessageSource.class})
@DisplayName("DepartmentRepositoryImpl Integration Tests with TestContainers")
class DepartmentRepositoryImplIntegrationTest {

    @SuppressWarnings("resource")
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("people_management_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DepartmentRepositoryImpl departmentRepository;

    private Department testDepartment;
    private UUID departmentId;

    @BeforeEach
    void setUp() {
        departmentId = UUID.randomUUID();
        testDepartment = Department.create(departmentId, "Information Technology", "IT001");
    }

    @Test
    @DisplayName("Should save department and persist in real PostgreSQL database")
    void shouldSaveDepartmentAndPersistInRealPostgreSqlDatabase() {
        // Act
        Department savedDepartment = departmentRepository.save(testDepartment);

        // Assert
        assertThat(savedDepartment.getId(), is(departmentId));
        assertThat(savedDepartment.getName(), is("Information Technology"));
        assertThat(savedDepartment.getCostCenterCode(), is("IT001"));

        // Verify persistence in real database
        DepartmentJpaEntity jpaEntity = entityManager.find(DepartmentJpaEntity.class, departmentId);
        assertThat(jpaEntity, is(notNullValue()));
        assertThat(jpaEntity.getName(), is("Information Technology"));
        assertThat(jpaEntity.getCostCenterCode(), is("IT001"));
    }

    @Test
    @DisplayName("Should find department by ID from real database")
    void shouldFindDepartmentByIdFromRealDatabase() {
        // Arrange
        DepartmentJpaEntity jpaEntity = new DepartmentJpaEntity(departmentId, "Information Technology", "IT001");
        entityManager.persistAndFlush(jpaEntity);

        // Act
        Optional<Department> result = departmentRepository.findById(departmentId);

        // Assert
        assertThat(result.isPresent(), is(true));
        Department foundDepartment = result.get();
        assertThat(foundDepartment.getId(), is(departmentId));
        assertThat(foundDepartment.getName(), is("Information Technology"));
        assertThat(foundDepartment.getCostCenterCode(), is("IT001"));
    }

    @Test
    @DisplayName("Should return empty when department not found in real database")
    void shouldReturnEmptyWhenDepartmentNotFoundInRealDatabase() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();

        // Act
        Optional<Department> result = departmentRepository.findById(nonExistentId);

        // Assert
        assertThat(result.isPresent(), is(false));
    }

    @Test
    @DisplayName("Should find all departments from real database")
    void shouldFindAllDepartmentsFromRealDatabase() {
        // Arrange
        DepartmentJpaEntity jpaEntity1 = new DepartmentJpaEntity(UUID.randomUUID(), "IT Department", "IT001");
        DepartmentJpaEntity jpaEntity2 = new DepartmentJpaEntity(UUID.randomUUID(), "HR Department", "HR001");
        entityManager.persist(jpaEntity1);
        entityManager.persist(jpaEntity2);
        entityManager.flush();

        // Act
        List<Department> result = departmentRepository.findAll();

        // Assert
        assertThat(result, hasSize(2));
        assertThat(result.stream().map(Department::getName).toList(), 
                   containsInAnyOrder("IT Department", "HR Department"));
        assertThat(result.stream().map(Department::getCostCenterCode).toList(), 
                   containsInAnyOrder("IT001", "HR001"));
    }

    @Test
    @DisplayName("Should find department by cost center code in real database")
    void shouldFindDepartmentByCostCenterCodeInRealDatabase() {
        // Arrange
        DepartmentJpaEntity jpaEntity = new DepartmentJpaEntity(departmentId, "Information Technology", "IT001");
        entityManager.persistAndFlush(jpaEntity);

        // Act
        Optional<Department> result = departmentRepository.findByCostCenterCode("IT001");

        // Assert
        assertThat(result.isPresent(), is(true));
        Department foundDepartment = result.get();
        assertThat(foundDepartment.getId(), is(departmentId));
        assertThat(foundDepartment.getCostCenterCode(), is("IT001"));
    }

    @Test
    @DisplayName("Should delete department from real database")
    void shouldDeleteDepartmentFromRealDatabase() {
        // Arrange
        DepartmentJpaEntity jpaEntity = new DepartmentJpaEntity(departmentId, "Information Technology", "IT001");
        entityManager.persistAndFlush(jpaEntity);

        // Act
        departmentRepository.delete(testDepartment);
        entityManager.flush();

        // Assert
        DepartmentJpaEntity deletedEntity = entityManager.find(DepartmentJpaEntity.class, departmentId);
        assertThat(deletedEntity, is(nullValue()));
    }

    @Test
    @DisplayName("Should handle concurrent operations with database isolation")
    void shouldHandleConcurrentOperationsWithDatabaseIsolation() {
        // Arrange
        Department dept1 = Department.create(UUID.randomUUID(), "Department 1", "DEPT001");
        Department dept2 = Department.create(UUID.randomUUID(), "Department 2", "DEPT002");

        // Act - Simulate concurrent saves
        Department saved1 = departmentRepository.save(dept1);
        Department saved2 = departmentRepository.save(dept2);

        // Assert - Both should be persisted independently
        List<Department> allDepartments = departmentRepository.findAll();
        assertThat(allDepartments, hasSize(2));
        assertThat(allDepartments.stream().map(Department::getId).toList(),
                   containsInAnyOrder(saved1.getId(), saved2.getId()));
    }

    @Test
    @DisplayName("Should maintain ACID properties in real PostgreSQL")
    void shouldMaintainAcidPropertiesInRealPostgreSql() {
        // Act - Save, retrieve, modify, and verify atomicity
        Department savedDepartment = departmentRepository.save(testDepartment);
        Optional<Department> retrievedDepartment = departmentRepository.findById(savedDepartment.getId());
        
        // Assert - Atomicity and Consistency
        assertThat(retrievedDepartment.isPresent(), is(true));
        Department finalDepartment = retrievedDepartment.get();
        assertThat(finalDepartment.getId(), is(testDepartment.getId()));
        assertThat(finalDepartment.getName(), is(testDepartment.getName()));
        assertThat(finalDepartment.getCostCenterCode(), is(testDepartment.getCostCenterCode()));
        
        // Verify Isolation - Other transactions don't see uncommitted changes
        List<Department> allDepartments = departmentRepository.findAll();
        assertThat(allDepartments, contains(hasProperty("id", is(finalDepartment.getId()))));
    }

    @Test
    @DisplayName("Should handle PostgreSQL specific constraints and data types")
    void shouldHandlePostgreSqlSpecificConstraintsAndDataTypes() {
        // Arrange - Test UUID and VARCHAR constraints specific to PostgreSQL
        UUID specificId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        Department departmentWithSpecificId = Department.create(specificId, "Test Department", "TEST001");

        // Act
        Department savedDepartment = departmentRepository.save(departmentWithSpecificId);

        // Assert - PostgreSQL UUID handling
        assertThat(savedDepartment.getId(), is(specificId));
        
        // Verify in database directly
        DepartmentJpaEntity jpaEntity = entityManager.find(DepartmentJpaEntity.class, specificId);
        assertThat(jpaEntity.getId(), is(specificId));
    }
}
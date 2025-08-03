package com.itau.hr.people_management.integration.infrastructure.persistence.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.itau.hr.people_management.domain.position.entity.Position;
import com.itau.hr.people_management.domain.position.enumeration.PositionLevel;
import com.itau.hr.people_management.infrastructure.persistence.entity.PositionJpaEntity;
import com.itau.hr.people_management.infrastructure.persistence.repository.JpaPositionRepository;
import com.itau.hr.people_management.infrastructure.persistence.repository.PositionRepositoryImpl;
import com.itau.hr.people_management.infrastructure.shared.message.SpringDomainMessageSource;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({SpringDomainMessageSource.class})
@EntityScan(basePackageClasses = {PositionJpaEntity.class})
@DisplayName("PositionRepositoryImpl Integration Tests with TestContainers")
class PositionRepositoryImplIntegrationTest {

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
    private JpaPositionRepository jpaPositionRepository;

    private PositionRepositoryImpl positionRepository;
    private Position testPosition;
    private UUID positionId;

    @BeforeEach
    void setUp() {
        positionRepository = new PositionRepositoryImpl(jpaPositionRepository);
        
        positionId = UUID.randomUUID();
        testPosition = Position.create(
            positionId,
            "Software Engineer",
            PositionLevel.SENIOR
        );
    }

    @Test
    @DisplayName("Should save position and map correctly between domain and JPA entity")
    void shouldSavePositionAndMapCorrectlyBetweenDomainAndJpaEntity() {
        // Act
        Position savedPosition = positionRepository.save(testPosition);

        // Assert
        assertThat(savedPosition.getId(), is(positionId));
        assertThat(savedPosition.getTitle(), is("Software Engineer"));
        assertThat(savedPosition.getPositionLevel(), is(PositionLevel.SENIOR));

        // Verify JPA entity was persisted correctly
        PositionJpaEntity jpaEntity = entityManager.find(PositionJpaEntity.class, positionId);
        assertThat(jpaEntity, is(notNullValue()));
        assertThat(jpaEntity.getTitle(), is("Software Engineer"));
        assertThat(jpaEntity.getPositionLevel(), is(PositionLevel.SENIOR));
    }

    @Test
    @DisplayName("Should find position by ID and map from JPA to domain entity")
    void shouldFindPositionByIdAndMapFromJpaToDomainEntity() {
        // Arrange
        PositionJpaEntity jpaEntity = new PositionJpaEntity(positionId, "Software Engineer", PositionLevel.SENIOR);
        entityManager.persistAndFlush(jpaEntity);

        // Act
        Optional<Position> result = positionRepository.findById(positionId);

        // Assert
        assertThat(result.isPresent(), is(true));
        Position foundPosition = result.get();
        assertThat(foundPosition.getId(), is(positionId));
        assertThat(foundPosition.getTitle(), is("Software Engineer"));
        assertThat(foundPosition.getPositionLevel(), is(PositionLevel.SENIOR));
    }

    @Test
    @DisplayName("Should return empty optional when position not found by ID")
    void shouldReturnEmptyOptionalWhenPositionNotFoundById() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();

        // Act
        Optional<Position> result = positionRepository.findById(nonExistentId);

        // Assert
        assertThat(result.isPresent(), is(false));
    }

    @Test
    @DisplayName("Should find all positions and map collection correctly")
    void shouldFindAllPositionsAndMapCollectionCorrectly() {
        // Arrange
        PositionJpaEntity jpaEntity1 = new PositionJpaEntity(UUID.randomUUID(), "Software Engineer", PositionLevel.SENIOR);
        PositionJpaEntity jpaEntity2 = new PositionJpaEntity(UUID.randomUUID(), "Product Manager", PositionLevel.PLENO);
        entityManager.persist(jpaEntity1);
        entityManager.persist(jpaEntity2);
        entityManager.flush();

        // Act
        List<Position> result = positionRepository.findAll();

        // Assert
        assertThat(result, hasSize(2));
        assertThat(result.stream().map(Position::getTitle).toList(), 
                   containsInAnyOrder("Software Engineer", "Product Manager"));
        assertThat(result.stream().map(Position::getPositionLevel).toList(), 
                   containsInAnyOrder(PositionLevel.SENIOR, PositionLevel.PLENO));
    }

    @Test
    @DisplayName("Should find position by title and position level")
    void shouldFindPositionByTitleAndPositionLevel() {
        // Arrange
        PositionJpaEntity jpaEntity = new PositionJpaEntity(positionId, "Software Engineer", PositionLevel.SENIOR);
        entityManager.persistAndFlush(jpaEntity);

        // Act
        Optional<Position> result = positionRepository.findByTitleAndPositionLevel("Software Engineer", PositionLevel.SENIOR);

        // Assert
        assertThat(result.isPresent(), is(true));
        Position foundPosition = result.get();
        assertThat(foundPosition.getId(), is(positionId));
        assertThat(foundPosition.getTitle(), is("Software Engineer"));
        assertThat(foundPosition.getPositionLevel(), is(PositionLevel.SENIOR));
    }

    @Test
    @DisplayName("Should return empty when title and position level combination not found")
    void shouldReturnEmptyWhenTitleAndPositionLevelCombinationNotFound() {
        // Act
        Optional<Position> result = positionRepository.findByTitleAndPositionLevel("Nonexistent Position", PositionLevel.JUNIOR);

        // Assert
        assertThat(result.isPresent(), is(false));
    }

    @Test
    @DisplayName("Should handle different position levels correctly")
    void shouldHandleDifferentPositionLevelsCorrectly() {
        // Arrange
        PositionJpaEntity juniorPosition = new PositionJpaEntity(UUID.randomUUID(), "Developer", PositionLevel.JUNIOR);
        PositionJpaEntity seniorPosition = new PositionJpaEntity(UUID.randomUUID(), "Developer", PositionLevel.SENIOR);
        PositionJpaEntity managerPosition = new PositionJpaEntity(UUID.randomUUID(), "Developer", PositionLevel.PLENO);
        
        entityManager.persist(juniorPosition);
        entityManager.persist(seniorPosition);
        entityManager.persist(managerPosition);
        entityManager.flush();

        // Act & Assert - Find by specific combinations
        Optional<Position> juniorResult = positionRepository.findByTitleAndPositionLevel("Developer", PositionLevel.JUNIOR);
        Optional<Position> seniorResult = positionRepository.findByTitleAndPositionLevel("Developer", PositionLevel.SENIOR);
        Optional<Position> managerResult = positionRepository.findByTitleAndPositionLevel("Developer", PositionLevel.PLENO);

        assertThat(juniorResult.isPresent(), is(true));
        assertThat(seniorResult.isPresent(), is(true));
        assertThat(managerResult.isPresent(), is(true));
        
        assertThat(juniorResult.get().getPositionLevel(), is(PositionLevel.JUNIOR));
        assertThat(seniorResult.get().getPositionLevel(), is(PositionLevel.SENIOR));
        assertThat(managerResult.get().getPositionLevel(), is(PositionLevel.PLENO));
    }

    @Test
    @DisplayName("Should delete position and remove from database")
    void shouldDeletePositionAndRemoveFromDatabase() {
        // Arrange
        PositionJpaEntity jpaEntity = new PositionJpaEntity(positionId, "Software Engineer", PositionLevel.SENIOR);
        entityManager.persistAndFlush(jpaEntity);

        // Act
        positionRepository.delete(testPosition);
        entityManager.flush();

        // Assert
        PositionJpaEntity deletedEntity = entityManager.find(PositionJpaEntity.class, positionId);
        assertThat(deletedEntity, is(nullValue()));
    }

    @Test
    @DisplayName("Should handle save and retrieve cycle maintaining data integrity")
    void shouldHandleSaveAndRetrieveCycleMaintainingDataIntegrity() {
        // Act - Save → Retrieve → Verify full cycle
        Position savedPosition = positionRepository.save(testPosition);
        Optional<Position> retrievedPosition = positionRepository.findById(savedPosition.getId());

        // Assert - Full cycle integrity
        assertThat(retrievedPosition.isPresent(), is(true));
        Position finalPosition = retrievedPosition.get();
        assertThat(finalPosition.getId(), is(testPosition.getId()));
        assertThat(finalPosition.getTitle(), is(testPosition.getTitle()));
        assertThat(finalPosition.getPositionLevel(), is(testPosition.getPositionLevel()));
    }

    @Test
    @DisplayName("Should handle position title case sensitivity correctly")
    void shouldHandlePositionTitleCaseSensitivityCorrectly() {
        // Arrange
        PositionJpaEntity jpaEntity = new PositionJpaEntity(positionId, "Software Engineer", PositionLevel.SENIOR);
        entityManager.persistAndFlush(jpaEntity);

        // Act - Search with different cases
        Optional<Position> exactMatch = positionRepository.findByTitleAndPositionLevel("Software Engineer", PositionLevel.SENIOR);
        Optional<Position> lowerCaseMatch = positionRepository.findByTitleAndPositionLevel("software engineer", PositionLevel.SENIOR);
        Optional<Position> upperCaseMatch = positionRepository.findByTitleAndPositionLevel("SOFTWARE ENGINEER", PositionLevel.SENIOR);

        // Assert - Only exact match should work (case sensitive)
        assertThat(exactMatch.isPresent(), is(true));
        assertThat(lowerCaseMatch.isPresent(), is(false));
        assertThat(upperCaseMatch.isPresent(), is(false));
    }
}
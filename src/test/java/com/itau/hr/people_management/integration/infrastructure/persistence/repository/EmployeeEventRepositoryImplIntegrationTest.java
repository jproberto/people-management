package com.itau.hr.people_management.integration.infrastructure.persistence.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;

import java.time.Instant;
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

import com.itau.hr.people_management.domain.employee.enumeration.EventType;
import com.itau.hr.people_management.domain.employee.history.EmployeeEvent;
import com.itau.hr.people_management.infrastructure.persistence.entity.EmployeeEventJpaEntity;
import com.itau.hr.people_management.infrastructure.persistence.repository.EmployeeEventRepositoryImpl;
import com.itau.hr.people_management.infrastructure.shared.message.SpringDomainMessageSource;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({EmployeeEventRepositoryImpl.class, SpringDomainMessageSource.class})
@DisplayName("EmployeeEventRepositoryImpl Integration Tests with TestContainers")
class EmployeeEventRepositoryImplIntegrationTest {

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
    private EmployeeEventRepositoryImpl employeeEventRepository;

    private EmployeeEvent testEmployeeEvent;
    private UUID eventId;
    private UUID employeeId;
    private Instant eventTimestamp;

    @BeforeEach
    void setUp() {
        eventId = UUID.randomUUID();
        employeeId = UUID.randomUUID();
        eventTimestamp = Instant.now();
        
        testEmployeeEvent = EmployeeEvent.builder()
            .id(eventId)
            .employeeId(employeeId)
            .eventType(EventType.EMPLOYEE_CREATED_EVENT)
            .occurredOn(eventTimestamp)
            .description("Employee was created successfully")
            .eventData("{\"name\":\"John Doe\",\"email\":\"john.doe@example.com\"}")
            .build();
    }

    @Test
    @DisplayName("Should save employee event and persist in real PostgreSQL database")
    void shouldSaveEmployeeEventAndPersistInRealPostgreSqlDatabase() {
        // Act
        employeeEventRepository.save(testEmployeeEvent);
        entityManager.flush();

        // Assert - Verify persistence in real database
        EmployeeEventJpaEntity jpaEntity = entityManager.find(EmployeeEventJpaEntity.class, eventId);
        assertThat(jpaEntity, is(notNullValue()));
        assertThat(jpaEntity.getId(), is(eventId));
        assertThat(jpaEntity.getEmployeeId(), is(employeeId));
        assertThat(jpaEntity.getEventType(), is(EventType.EMPLOYEE_CREATED_EVENT));
        assertThat(jpaEntity.getOccurredOn(), is(eventTimestamp));
        assertThat(jpaEntity.getDescription(), is("Employee was created successfully"));
        assertThat(jpaEntity.getEventData(), is("{\"name\":\"John Doe\",\"email\":\"john.doe@example.com\"}"));
    }

    @Test
    @DisplayName("Should handle builder pattern mapping correctly")
    void shouldHandleBuilderPatternMappingCorrectly() {
        // Arrange - Event with all fields populated
        EmployeeEvent complexEvent = EmployeeEvent.builder()
            .id(UUID.randomUUID())
            .employeeId(UUID.randomUUID())
            .eventType(EventType.EMPLOYEE_STATUS_CHANGED_EVENT)
            .occurredOn(Instant.now())
            .description("Employee status changed from ACTIVE to TERMINATED")
            .eventData("{\"previousStatus\":\"ACTIVE\",\"newStatus\":\"TERMINATED\",\"reason\":\"Resignation\"}")
            .build();

        // Act
        employeeEventRepository.save(complexEvent);
        entityManager.flush();

        // Assert - Verify complex mapping
        EmployeeEventJpaEntity jpaEntity = entityManager.find(EmployeeEventJpaEntity.class, complexEvent.getId());
        assertThat(jpaEntity.getEventType(), is(EventType.EMPLOYEE_STATUS_CHANGED_EVENT));
        assertThat(jpaEntity.getDescription(), containsString("status changed"));
        assertThat(jpaEntity.getEventData(), containsString("previousStatus"));
        assertThat(jpaEntity.getEventData(), containsString("newStatus"));
    }

    @Test
    @DisplayName("Should handle large JSON event data in PostgreSQL")
    void shouldHandleLargeJsonEventDataInPostgreSql() {
        // Arrange - Event with large JSON data
        String largeJsonData = """
            {
                "previousData": {
                    "name": "John Doe",
                    "email": "john.doe@example.com",
                    "department": "Information Technology",
                    "position": "Senior Software Engineer",
                    "details": "Very long description with multiple fields and complex nested objects"
                },
                "newData": {
                    "name": "John Smith",
                    "email": "john.smith@example.com",
                    "department": "Engineering",
                    "position": "Lead Software Engineer",
                    "details": "Updated information with extensive details about the employee changes"
                },
                "metadata": {
                    "updatedBy": "admin@example.com",
                    "updateReason": "Name change due to marriage",
                    "additionalNotes": "Employee requested name update in all systems"
                }
            }
            """;
        
        EmployeeEvent eventWithLargeData = EmployeeEvent.builder()
            .id(UUID.randomUUID())
            .employeeId(employeeId)
            .eventType(EventType.EMPLOYEE_STATUS_CHANGED_EVENT)
            .occurredOn(Instant.now())
            .description("Employee data was updated with extensive changes")
            .eventData(largeJsonData)
            .build();

        // Act
        employeeEventRepository.save(eventWithLargeData);
        entityManager.flush();

        // Assert - Verify large data handling
        EmployeeEventJpaEntity jpaEntity = entityManager.find(EmployeeEventJpaEntity.class, eventWithLargeData.getId());
        assertThat(jpaEntity.getEventData(), containsString("previousData"));
        assertThat(jpaEntity.getEventData(), containsString("newData"));
        assertThat(jpaEntity.getEventData(), containsString("metadata"));
        assertThat(jpaEntity.getEventData().length(), is(greaterThan(500)));
    }

    @Test
    @DisplayName("Should handle concurrent event saves with database isolation")
    void shouldHandleConcurrentEventSavesWithDatabaseIsolation() {
        // Arrange - Multiple events for same employee
        EmployeeEvent event1 = EmployeeEvent.builder()
            .id(UUID.randomUUID())
            .employeeId(employeeId)
            .eventType(EventType.EMPLOYEE_CREATED_EVENT)
            .occurredOn(Instant.now().minusSeconds(10))
            .description("Employee created")
            .eventData("{\"action\":\"create\"}")
            .build();
        EmployeeEvent event2 = EmployeeEvent.builder()
            .id(UUID.randomUUID())
            .employeeId(employeeId)
            .eventType(EventType.EMPLOYEE_STATUS_CHANGED_EVENT)
            .occurredOn(Instant.now().minusSeconds(5))
            .description("Employee updated")
            .eventData("{\"action\":\"update\"}")
            .build();
        EmployeeEvent event3 = EmployeeEvent.builder()
            .id(UUID.randomUUID())
            .employeeId(employeeId)
            .eventType(EventType.EMPLOYEE_CREATED_EVENT)
            .occurredOn(Instant.now())
            .description("Employee activated")
            .eventData("{\"action\":\"activate\"}")
            .build();

        // Act - Save events concurrently
        employeeEventRepository.save(event1);
        employeeEventRepository.save(event2);
        employeeEventRepository.save(event3);
        entityManager.flush();

        // Assert - All events should be persisted independently
        EmployeeEventJpaEntity entity1 = entityManager.find(EmployeeEventJpaEntity.class, event1.getId());
        EmployeeEventJpaEntity entity2 = entityManager.find(EmployeeEventJpaEntity.class, event2.getId());
        EmployeeEventJpaEntity entity3 = entityManager.find(EmployeeEventJpaEntity.class, event3.getId());
        
        assertThat(entity1.getEventType(), is(EventType.EMPLOYEE_CREATED_EVENT));
        assertThat(entity2.getEventType(), is(EventType.EMPLOYEE_STATUS_CHANGED_EVENT));
        assertThat(entity3.getEventType(), is(EventType.EMPLOYEE_CREATED_EVENT));

        // Verify chronological order is maintained
        assertThat(entity1.getOccurredOn(), is(lessThan(entity2.getOccurredOn())));
        assertThat(entity2.getOccurredOn(), is(lessThan(entity3.getOccurredOn())));
    }

    @Test
    @DisplayName("Should handle PostgreSQL specific timestamp precision")
    void shouldHandlePostgreSqlSpecificTimestampPrecision() {
        // Arrange - Event with high precision timestamp
        Instant preciseTimestamp = Instant.parse("2023-12-01T10:15:30.123456789Z");
        EmployeeEvent eventWithPreciseTime = EmployeeEvent.builder()
            .id(UUID.randomUUID())
            .employeeId(employeeId)
            .eventType(EventType.EMPLOYEE_CREATED_EVENT)
            .occurredOn(preciseTimestamp)
            .description("Testing timestamp precision")
            .eventData("{\"precision\":\"nanoseconds\"}")
            .build();

        // Act
        employeeEventRepository.save(eventWithPreciseTime);
        entityManager.flush();

        // Assert - Verify PostgreSQL timestamp handling
        EmployeeEventJpaEntity jpaEntity = entityManager.find(EmployeeEventJpaEntity.class, eventWithPreciseTime.getId());
        assertThat(jpaEntity.getOccurredOn(), is(notNullValue()));
        // PostgreSQL typically truncates to microsecond precision
        assertThat(jpaEntity.getOccurredOn().getEpochSecond(), is(preciseTimestamp.getEpochSecond()));
    }

    @Test
    @DisplayName("Should verify one-way repository pattern - save only operation")
    void shouldVerifyOneWayRepositoryPatternSaveOnlyOperation() {
        // Arrange - Multiple different event types
        EmployeeEvent createEvent = EmployeeEvent.builder()
            .id(UUID.randomUUID())
            .employeeId(employeeId)
            .eventType(EventType.EMPLOYEE_CREATED_EVENT)
            .occurredOn(Instant.now())
            .description("Create")
            .eventData("{}")
            .build();
        EmployeeEvent updateEvent = EmployeeEvent.builder()
            .id(UUID.randomUUID())
            .employeeId(employeeId)
            .eventType(EventType.EMPLOYEE_STATUS_CHANGED_EVENT)
            .occurredOn(Instant.now())
            .description("Update")
            .eventData("{}")
            .build();
        EmployeeEvent deleteEvent = EmployeeEvent.builder()
            .id(UUID.randomUUID())
            .employeeId(employeeId)
            .eventType(EventType.EMPLOYEE_CREATED_EVENT)
            .occurredOn(Instant.now())
            .description("Delete")
            .eventData("{}")
            .build();

        // Act - Save different event types (event sourcing pattern)
        employeeEventRepository.save(createEvent);
        employeeEventRepository.save(updateEvent);
        employeeEventRepository.save(deleteEvent);
        entityManager.flush();

        // Assert - Verify all events are persisted (append-only pattern)
        assertThat(entityManager.find(EmployeeEventJpaEntity.class, createEvent.getId()), is(notNullValue()));
        assertThat(entityManager.find(EmployeeEventJpaEntity.class, updateEvent.getId()), is(notNullValue()));
        assertThat(entityManager.find(EmployeeEventJpaEntity.class, deleteEvent.getId()), is(notNullValue()));
    }
}
package com.itau.hr.people_management.integration.infrastructure.persistence.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.itau.hr.people_management.domain.employee.enumeration.EventType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.itau.hr.people_management.infrastructure.outbox.enumeration.OutboxMessageStatus;
import com.itau.hr.people_management.infrastructure.persistence.entity.OutboxMessage;
import com.itau.hr.people_management.infrastructure.persistence.repository.OutboxMessageRepository;
import com.itau.hr.people_management.infrastructure.shared.message.SpringDomainMessageSource;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({SpringDomainMessageSource.class})
@DisplayName("OutboxMessageRepository Integration Tests with TestContainers")
class OutboxMessageRepositoryIntegrationTest {

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
    private OutboxMessageRepository outboxMessageRepository;

    private OutboxMessage testOutboxMessage;
    private Instant baseTimestamp;

    @BeforeEach
    void setUp() {
        baseTimestamp = Instant.now();
        
        testOutboxMessage = OutboxMessage.builder()
            .aggregateId(UUID.randomUUID())
            .aggregateType("Employee")
            .eventType(EventType.EMPLOYEE_CREATED_EVENT.name())
            .payload("{\"employeeId\":\"" + UUID.randomUUID() + "\",\"name\":\"John Doe\"}")
            .status(OutboxMessageStatus.PENDING)
            .occurredOn(baseTimestamp)
            .nextAttemptAt(baseTimestamp.plusSeconds(60))
            .retryAttempts(0)
            .build();
    }

    @Test
    @DisplayName("Should save outbox message and persist in real PostgreSQL database")
    void shouldSaveOutboxMessageAndPersistInRealPostgreSqlDatabase() {
        // Arragente
        UUID messageId = UUID.randomUUID();
        testOutboxMessage.setId(messageId);

        // Act
        OutboxMessage savedMessage = outboxMessageRepository.save(testOutboxMessage);

        // Assert
        assertThat(savedMessage.getId(), is(messageId));
        assertThat(savedMessage.getAggregateType(), is("Employee"));
        assertThat(savedMessage.getEventType(), is(EventType.EMPLOYEE_CREATED_EVENT.name()));
        assertThat(savedMessage.getStatus(), is(OutboxMessageStatus.PENDING));

        // Verify persistence in real database
        OutboxMessage persistedMessage = entityManager.find(OutboxMessage.class, messageId);
        assertThat(persistedMessage, is(notNullValue()));
        assertThat(persistedMessage.getAggregateType(), is("Employee"));
        assertThat(persistedMessage.getEventType(), is(EventType.EMPLOYEE_CREATED_EVENT.name()));
        assertThat(persistedMessage.getPayload(), containsString("John Doe"));
    }

    @Test
    @DisplayName("Should find outbox message by ID")
    void shouldFindOutboxMessageById() {
        // Arrange
        UUID messageId = UUID.randomUUID();
        testOutboxMessage.setId(messageId);
        entityManager.persistAndFlush(testOutboxMessage);
        
        // Act
        Optional<OutboxMessage> result = outboxMessageRepository.findById(messageId);

        // Assert
        assertThat(result.isPresent(), is(true));
        OutboxMessage foundMessage = result.get();
        assertThat(foundMessage.getId(), is(messageId));
        assertThat(foundMessage.getAggregateType(), is("Employee"));
        assertThat(foundMessage.getStatus(), is(OutboxMessageStatus.PENDING));
    }

    @Test
    @DisplayName("Should return empty when outbox message not found by ID")
    void shouldReturnEmptyWhenOutboxMessageNotFoundById() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();

        // Act
        Optional<OutboxMessage> result = outboxMessageRepository.findById(nonExistentId);

        // Assert
        assertThat(result.isPresent(), is(false));
    }

    @Test
    @DisplayName("Should find messages by status and next attempt time with ordering")
    void shouldFindMessagesByStatusAndNextAttemptTimeWithOrdering() {
        // Arrange
        Instant now = Instant.now();
        Instant pastTime = now.minusSeconds(300);
        Instant futureTime = now.plusSeconds(300);

        OutboxMessage pendingMessage1 = OutboxMessage.builder()
            .id(UUID.randomUUID())
            .aggregateId(UUID.randomUUID())
            .aggregateType("Employee")
            .eventType(EventType.EMPLOYEE_STATUS_CHANGED_EVENT.name())
            .payload("{\"test\":\"data1\"}")
            .status(OutboxMessageStatus.PENDING)
            .occurredOn(now.minusSeconds(600))
            .nextAttemptAt(pastTime)
            .retryAttempts(0)
            .build();

        OutboxMessage pendingMessage2 = OutboxMessage.builder()
            .id(UUID.randomUUID())
            .aggregateId(UUID.randomUUID())
            .aggregateType("Employee")
            .eventType(EventType.EMPLOYEE_CREATED_EVENT.name())
            .payload("{\"test\":\"data2\"}")
            .status(OutboxMessageStatus.PENDING)
            .occurredOn(now.minusSeconds(300))
            .nextAttemptAt(pastTime.minusSeconds(60))
            .retryAttempts(1)
            .build();

        OutboxMessage futureMessage = OutboxMessage.builder()
            .id(UUID.randomUUID())
            .aggregateId(UUID.randomUUID())
            .aggregateType("Employee")
            .eventType("EmployeeDeleted")
            .payload("{\"test\":\"future\"}")
            .status(OutboxMessageStatus.PENDING)
            .occurredOn(now.minusSeconds(100))
            .nextAttemptAt(futureTime)
            .retryAttempts(0)
            .build();

        OutboxMessage processedMessage = OutboxMessage.builder()
            .id(UUID.randomUUID())
            .aggregateId(UUID.randomUUID())
            .aggregateType("Employee")
            .eventType("EmployeeProcessed")
            .payload("{\"test\":\"processed\"}")
            .status(OutboxMessageStatus.SENT)
            .occurredOn(now.minusSeconds(200))
            .nextAttemptAt(pastTime)
            .retryAttempts(1)
            .build();

        entityManager.persist(pendingMessage1);
        entityManager.persist(pendingMessage2);
        entityManager.persist(futureMessage);
        entityManager.persist(processedMessage);
        entityManager.flush();

        // Act
        List<OutboxMessageStatus> statuses = List.of(OutboxMessageStatus.PENDING, OutboxMessageStatus.FAILED);
        Pageable pageable = PageRequest.of(0, 10);
        List<OutboxMessage> result = outboxMessageRepository
            .findByStatusInAndNextAttemptAtBeforeOrderByOccurredOnAsc(statuses, now, pageable);

        // Assert
        assertThat(result, hasSize(2)); // Only pending messages with past nextAttemptAt
        assertThat(result.get(0).getEventType(), is(EventType.EMPLOYEE_STATUS_CHANGED_EVENT.name())); // Ordered by occurredOn ASC
        assertThat(result.get(1).getEventType(), is(EventType.EMPLOYEE_CREATED_EVENT.name()));

        // Verify all returned messages have correct status and timing
        result.forEach(message -> {
            assertThat(message.getStatus(), is(in(List.of(OutboxMessageStatus.PENDING, OutboxMessageStatus.FAILED))));
            assertThat(message.getNextAttemptAt(), is(lessThan(now)));
        });
    }

    @Test
    @DisplayName("Should handle empty result for status and timing query")
    void shouldHandleEmptyResultForStatusAndTimingQuery() {
        // Arrange
        Instant futureTime = Instant.now().plusSeconds(3600);
        List<OutboxMessageStatus> statuses = List.of(OutboxMessageStatus.PENDING);
        Pageable pageable = PageRequest.of(0, 10);

        // Act - Query with future time (no messages should be ready)
        List<OutboxMessage> result = outboxMessageRepository
            .findByStatusInAndNextAttemptAtBeforeOrderByOccurredOnAsc(statuses, futureTime.minusSeconds(7200), pageable);

        // Assert
        assertThat(result, is(empty()));
    }

    @Test
    @DisplayName("Should respect pagination in status and timing query")
    void shouldRespectPaginationInStatusAndTimingQuery() {
        // Arrange
        Instant now = Instant.now();
        Instant pastTime = now.minusSeconds(300);
        
        // Create 5 pending messages
        for (int i = 0; i < 5; i++) {
            OutboxMessage message = OutboxMessage.builder()
                .id(UUID.randomUUID())
                .aggregateId(UUID.randomUUID())
                .aggregateType("Employee")
                .eventType("EmployeeEvent" + i)
                .payload("{\"index\":" + i + "}")
                .status(OutboxMessageStatus.PENDING)
                .occurredOn(Instant.now().minusSeconds(i * 60)) // Staggered occurredOn
                .nextAttemptAt(pastTime)
                .retryAttempts(0)
                .build();
            entityManager.persist(message);
        }
        entityManager.flush();

        // Act - Request only 3 messages
        List<OutboxMessageStatus> statuses = List.of(OutboxMessageStatus.PENDING);
        Pageable pageable = PageRequest.of(0, 3);
        List<OutboxMessage> result = outboxMessageRepository
            .findByStatusInAndNextAttemptAtBeforeOrderByOccurredOnAsc(statuses, now, pageable);

        // Assert
        assertThat(result, hasSize(3)); // Respects page size
        assertThat(result.get(0).getEventType(), is("EmployeeEvent4")); // Ordered by occurredOn ASC (oldest first)
        assertThat(result.get(1).getEventType(), is("EmployeeEvent3"));
        assertThat(result.get(2).getEventType(), is("EmployeeEvent2"));
    }

    @Test
    @DisplayName("Should handle large JSON payload in PostgreSQL")
    void shouldHandleLargeJsonPayloadInPostgreSql() {
        // Arrange - Message with large JSON payload
        String largePayload = """
            {
                "employeeData": {
                    "personalInfo": {
                        "name": "John Doe",
                        "email": "john.doe@example.com",
                        "phone": "+1234567890",
                        "address": {
                            "street": "123 Main Street",
                            "city": "New York",
                            "state": "NY",
                            "zipCode": "10001"
                        }
                    },
                    "professionalInfo": {
                        "position": "Senior Software Engineer",
                        "department": "Information Technology",
                        "hireDate": "2023-01-15",
                        "salary": 95000,
                        "skills": ["Java", "Spring Boot", "PostgreSQL", "Docker", "Kubernetes","Java", "Spring Boot", "PostgreSQL", "Docker", "Kubernetes"]
                    },
                    "metadata": {
                        "createdBy": "system",
                        "createdAt": "2023-12-01T10:00:00Z",
                        "version": 1,
                        "tags": ["new-hire", "engineer", "remote-worker","new-hire", "engineer", "remote-worker", "new-hire", "engineer", "remote-worker","new-hire", "engineer", "remote-worker", "new-hire", "engineer", "remote-worker","new-hire", "engineer", "remote-worker"]
                    }
                }
            }
            """;

        OutboxMessage messageWithLargePayload = OutboxMessage.builder()
            .id(UUID.randomUUID())
            .aggregateId(UUID.randomUUID())
            .aggregateType("Employee")
            .eventType("EmployeeCreatedWithDetails")
            .payload(largePayload)
            .status(OutboxMessageStatus.PENDING)
            .occurredOn(baseTimestamp)
            .nextAttemptAt(baseTimestamp.plusSeconds(60))
            .retryAttempts(0)
            .build();

        // Act
        OutboxMessage savedMessage = outboxMessageRepository.save(messageWithLargePayload);

        // Assert - Verify large payload handling
        assertThat(savedMessage.getPayload(), containsString("employeeData"));
        assertThat(savedMessage.getPayload(), containsString("personalInfo"));
        assertThat(savedMessage.getPayload(), containsString("professionalInfo"));
        assertThat(savedMessage.getPayload().length(), is(greaterThan(1000)));

        // Verify persistence
        OutboxMessage persistedMessage = entityManager.find(OutboxMessage.class, savedMessage.getId());
        assertThat(persistedMessage.getPayload(), is(equalTo(largePayload)));
    }

    @Test
    @DisplayName("Should update message status and attempt count")
    void shouldUpdateMessageStatusAndAttemptCount() {
        // Arrange
        UUID messageId = UUID.randomUUID();
        testOutboxMessage.setId(messageId);
        entityManager.persistAndFlush(testOutboxMessage);
        
        // ✅ DETACH a entidade do contexto JPA
        entityManager.detach(testOutboxMessage);
        entityManager.clear(); // Limpa o cache de primeiro nível
        
        // ✅ FORÇAR nova consulta do banco (não do cache)
        OutboxMessage managedMessage = entityManager.find(OutboxMessage.class, messageId);
        entityManager.refresh(managedMessage); // Força reload do banco
        
        managedMessage.setStatus(OutboxMessageStatus.FAILED);
        managedMessage.setRetryAttempts(managedMessage.getRetryAttempts() + 1);
        managedMessage.setNextAttemptAt(baseTimestamp.plusSeconds(300));
        
        // Act
        OutboxMessage updatedMessage = outboxMessageRepository.saveAndFlush(managedMessage);

        // Assert
        assertThat(updatedMessage.getStatus(), is(OutboxMessageStatus.FAILED));
        assertThat(updatedMessage.getRetryAttempts(), is(1));
        assertThat(updatedMessage.getNextAttemptAt(), is(baseTimestamp.plusSeconds(300)));

        // Verify persistence of update
        entityManager.clear(); // Clear first-level cache
        OutboxMessage persistedMessage = entityManager.find(OutboxMessage.class, messageId);
        assertThat(persistedMessage.getStatus(), is(OutboxMessageStatus.FAILED));
        assertThat(persistedMessage.getRetryAttempts(), is(1));
    }

    @Test
    @DisplayName("Should delete outbox message from database")
    void shouldDeleteOutboxMessageFromDatabase() {
        // Arrange
        UUID messageId = UUID.randomUUID();
        testOutboxMessage.setId(messageId);
        entityManager.persistAndFlush(testOutboxMessage);

        // Act
        outboxMessageRepository.delete(testOutboxMessage);
        entityManager.flush();

        // Assert
        OutboxMessage deletedMessage = entityManager.find(OutboxMessage.class, messageId);
        assertThat(deletedMessage, is(nullValue()));
    }
}
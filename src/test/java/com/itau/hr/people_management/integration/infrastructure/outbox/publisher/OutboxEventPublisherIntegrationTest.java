package com.itau.hr.people_management.integration.infrastructure.outbox.publisher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itau.hr.people_management.domain.employee.enumeration.EmployeeStatus;
import com.itau.hr.people_management.domain.employee.enumeration.EventType;
import com.itau.hr.people_management.domain.employee.event.EmployeeCreatedEvent;
import com.itau.hr.people_management.domain.employee.event.EmployeeStatusChangedEvent;
import com.itau.hr.people_management.infrastructure.outbox.enumeration.OutboxMessageStatus;
import com.itau.hr.people_management.infrastructure.outbox.publisher.OutboxEventPublisher;
import com.itau.hr.people_management.infrastructure.persistence.entity.OutboxMessage;
import com.itau.hr.people_management.infrastructure.persistence.repository.OutboxMessageRepository;
import com.itau.hr.people_management.integration.infrastructure.kafka.support.TestLogAppender;

import ch.qos.logback.classic.Logger;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("OutboxEventPublisher Integration Tests")
class OutboxEventPublisherIntegrationTest {

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
    private OutboxEventPublisher outboxEventPublisher;

    @Autowired
    private OutboxMessageRepository outboxMessageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private TestLogAppender testLogAppender;
    private Logger outboxEventPublisherLogger;

    @BeforeEach
    void setUp() {
        // Limpar dados
        outboxMessageRepository.deleteAll();
        outboxMessageRepository.flush();

        // Configurar logger para capturar logs
        outboxEventPublisherLogger = (Logger) LoggerFactory.getLogger(OutboxEventPublisher.class);
        outboxEventPublisherLogger.detachAndStopAllAppenders();
        
        testLogAppender = new TestLogAppender();
        testLogAppender.start();
        outboxEventPublisherLogger.addAppender(testLogAppender);
        outboxEventPublisherLogger.setAdditive(false);
    }

    @AfterEach
    void tearDown() {
        // Cleanup dos logs
        try {
            if (outboxEventPublisherLogger != null && testLogAppender != null) {
                outboxEventPublisherLogger.detachAppender(testLogAppender);
                testLogAppender.stop();
            }
        } catch (Exception e) {
            // Ignorar erros de cleanup
        }
        
        outboxMessageRepository.deleteAll();
        outboxMessageRepository.flush();
    }

    @Test
    @DisplayName("Should publish EmployeeCreatedEvent with real database persistence")
    void shouldPublishEmployeeCreatedEventWithRealDatabasePersistence() throws Exception {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        EmployeeCreatedEvent event = new EmployeeCreatedEvent(
            employeeId, 
            "Integration Test Employee", 
            "integration@test.com"
        );

        // Act
        outboxEventPublisher.publish(event);

        // Assert - Verificar persistência real no database
        List<OutboxMessage> savedMessages = outboxMessageRepository.findAll();
        assertThat(savedMessages, hasSize(1));

        OutboxMessage savedMessage = savedMessages.get(0);
        assertThat(savedMessage.getId(), is(event.getEventId()));
        assertThat(savedMessage.getAggregateId(), is(employeeId));
        assertThat(savedMessage.getAggregateType(), is("Employee"));
        assertThat(savedMessage.getEventType(), is(EventType.EMPLOYEE_CREATED_EVENT.name()));
        assertThat(savedMessage.getStatus(), is(OutboxMessageStatus.PENDING));
        assertThat(savedMessage.getNextAttemptAt(), is(notNullValue()));

        // Verificar JSON payload real
        String payload = savedMessage.getPayload();
        assertThat(payload, is(notNullValue()));
        
        JsonNode jsonNode = objectMapper.readTree(payload);
        assertThat(jsonNode.get("employeeId").asText(), is(employeeId.toString()));
        assertThat(jsonNode.get("employeeName").asText(), is("Integration Test Employee"));
        assertThat(jsonNode.get("employeeEmail").asText(), is("integration@test.com"));
        assertThat(jsonNode.get("eventId").asText(), is(event.getEventId().toString()));

        // Verificar logs reais
        assertThat(testLogAppender.getInfoMessages(), hasSize(1));
        String infoMessage = testLogAppender.getLastInfoMessage();
        assertThat(infoMessage, containsString("Event EmployeeCreatedEvent saved to outbox"));
        assertThat(infoMessage, containsString("aggregateType: Employee"));
        assertThat(infoMessage, containsString("aggregateId: " + employeeId));
    }

    @Test
    @DisplayName("Should publish EmployeeStatusChangedEvent with real database persistence")
    void shouldPublishEmployeeStatusChangedEventWithRealDatabasePersistence() throws Exception {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        EmployeeStatusChangedEvent event = new EmployeeStatusChangedEvent(
            employeeId,
            EmployeeStatus.ACTIVE,
            EmployeeStatus.TERMINATED
        );

        // Act
        outboxEventPublisher.publish(event);

        // Assert - Verificar persistência real no database
        List<OutboxMessage> savedMessages = outboxMessageRepository.findAll();
        assertThat(savedMessages, hasSize(1));

        OutboxMessage savedMessage = savedMessages.get(0);
        assertThat(savedMessage.getId(), is(event.getEventId()));
        assertThat(savedMessage.getAggregateId(), is(employeeId));
        assertThat(savedMessage.getAggregateType(), is("Employee"));
        assertThat(savedMessage.getEventType(), is(EventType.EMPLOYEE_STATUS_CHANGED_EVENT.name()));
        assertThat(savedMessage.getStatus(), is(OutboxMessageStatus.PENDING));

        // Verificar JSON payload específico do evento
        String payload = savedMessage.getPayload();
        JsonNode jsonNode = objectMapper.readTree(payload);
        assertThat(jsonNode.get("employeeId").asText(), is(employeeId.toString()));
        assertThat(jsonNode.get("oldStatus").asText(), is("ACTIVE"));
        assertThat(jsonNode.get("newStatus").asText(), is("TERMINATED"));

        // Verificar logs reais
        assertThat(testLogAppender.getInfoMessages(), hasSize(1));
        String infoMessage = testLogAppender.getLastInfoMessage();
        assertThat(infoMessage, containsString("Event EmployeeStatusChangedEvent saved to outbox"));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle database transaction rollback scenarios")
    void shouldHandleDatabaseTransactionRollbackScenarios() throws Exception {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        EmployeeCreatedEvent event = new EmployeeCreatedEvent(
            employeeId, 
            "Rollback Test Employee", 
            "rollback@test.com"
        );

        // Verificar estado inicial
        List<OutboxMessage> messagesBeforeTransaction = outboxMessageRepository.findAll();
        assertThat(messagesBeforeTransaction, empty());

        // Act - Executar transação com rollback explícito
        Exception caughtException = null;
        try {
            transactionTemplate.execute(status -> {
                // Publicar evento
                outboxEventPublisher.publish(event);
                
                // Verificar que foi salvo dentro da transação
                List<OutboxMessage> messagesInTransaction = outboxMessageRepository.findAll();
                assertThat(messagesInTransaction, hasSize(1));
                assertThat(messagesInTransaction.get(0).getAggregateId(), is(employeeId));
                
                // Marcar transação para rollback E lançar exception
                status.setRollbackOnly();
                throw new RuntimeException("Forced rollback for testing");
            });
        } catch (RuntimeException e) {
            caughtException = e;
        }

        // Assert - Verificar que exception foi capturada
        assertThat(caughtException, is(notNullValue()));
        assertThat(caughtException.getMessage(), is("Forced rollback for testing"));

        // Assert - Verificar que rollback funcionou - dados não devem estar no database
        List<OutboxMessage> messagesAfterRollback = outboxMessageRepository.findAll();
        assertThat(messagesAfterRollback, empty());

    }

    @Test
    @DisplayName("Should handle concurrent event publishing with real database constraints")
    void shouldHandleConcurrentEventPublishingWithRealDatabaseConstraints() {
        // Arrange - Múltiplos eventos únicos
        UUID employeeId1 = UUID.randomUUID();
        UUID employeeId2 = UUID.randomUUID();
        UUID employeeId3 = UUID.randomUUID();

        EmployeeCreatedEvent event1 = new EmployeeCreatedEvent(employeeId1, "Employee 1", "emp1@test.com");
        EmployeeCreatedEvent event2 = new EmployeeCreatedEvent(employeeId2, "Employee 2", "emp2@test.com");
        EmployeeStatusChangedEvent event3 = new EmployeeStatusChangedEvent(
            employeeId3, EmployeeStatus.ACTIVE, EmployeeStatus.TERMINATED
        );

        // Act - Publicar eventos sequencialmente (simulando concorrência)
        outboxEventPublisher.publish(event1);
        outboxEventPublisher.publish(event2);
        outboxEventPublisher.publish(event3);

        // Assert - Verificar que todos foram persistidos corretamente
        List<OutboxMessage> savedMessages = outboxMessageRepository.findAll();
        assertThat(savedMessages, hasSize(3));

        // Verificar IDs únicos
        List<UUID> eventIds = savedMessages.stream()
            .map(OutboxMessage::getId)
            .toList();
        assertThat(eventIds, containsInAnyOrder(
            event1.getEventId(), 
            event2.getEventId(), 
            event3.getEventId()
        ));

        // Verificar aggregate IDs
        List<UUID> aggregateIds = savedMessages.stream()
            .map(OutboxMessage::getAggregateId)
            .toList();
        assertThat(aggregateIds, containsInAnyOrder(employeeId1, employeeId2, employeeId3));

        // Verificar tipos de eventos
        List<String> eventTypes = savedMessages.stream()
            .map(OutboxMessage::getEventType)
            .toList();
        assertThat(eventTypes, containsInAnyOrder(
            EventType.EMPLOYEE_CREATED_EVENT.name(),
            EventType.EMPLOYEE_CREATED_EVENT.name(),
            EventType.EMPLOYEE_STATUS_CHANGED_EVENT.name()
        ));

        // Verificar logs para todos os eventos
        assertThat(testLogAppender.getInfoMessages(), hasSize(3));
    }

    @Test
    @DisplayName("Should verify integration with real Spring Boot ObjectMapper configuration")
    void shouldVerifyIntegrationWithRealSpringBootObjectMapperConfiguration() throws Exception {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        EmployeeCreatedEvent event = new EmployeeCreatedEvent(
            employeeId, 
            "ObjectMapper Test Employee", 
            "objectmapper@test.com"
        );

        // Act
        outboxEventPublisher.publish(event);

        // Assert - Verificar que ObjectMapper real do Spring Boot foi usado
        List<OutboxMessage> savedMessages = outboxMessageRepository.findAll();
        assertThat(savedMessages, hasSize(1));

        String payload = savedMessages.get(0).getPayload();
        
        // Verificar que JSON segue padrões do Spring Boot (camelCase, etc.)
        JsonNode jsonNode = objectMapper.readTree(payload);
        
        // Verificar estrutura esperada do JSON
        assertThat(jsonNode.has("eventId"), is(true));
        assertThat(jsonNode.has("occurredOn"), is(true));
        assertThat(jsonNode.has("employeeId"), is(true));
        assertThat(jsonNode.has("employeeName"), is(true));
        assertThat(jsonNode.has("employeeEmail"), is(true));

        // Verificar que pode ser deserializado de volta usando o mesmo ObjectMapper
        EmployeeCreatedEvent deserializedEvent = objectMapper.readValue(payload, EmployeeCreatedEvent.class);
        assertThat(deserializedEvent.employeeId(), is(event.employeeId()));
        assertThat(deserializedEvent.employeeName(), is(event.employeeName()));
        assertThat(deserializedEvent.employeeEmail(), is(event.employeeEmail()));
        assertThat(deserializedEvent.getEventId(), is(event.getEventId()));
    }

    @Test
    @DisplayName("Should handle large payload data with real database limits")
    void shouldHandleLargePayloadDataWithRealDatabaseLimits() throws Exception {
        // Arrange - Criar evento com payload grande (mas dentro dos limites)
        String largeEmployeeName = "A".repeat(1000); // Nome grande mas válido
        UUID employeeId = UUID.randomUUID();
        EmployeeCreatedEvent largeEvent = new EmployeeCreatedEvent(
            employeeId, 
            largeEmployeeName, 
            "large@test.com"
        );

        // Act
        outboxEventPublisher.publish(largeEvent);

        // Assert - Verificar que payload grande foi persistido corretamente
        List<OutboxMessage> savedMessages = outboxMessageRepository.findAll();
        assertThat(savedMessages, hasSize(1));

        OutboxMessage savedMessage = savedMessages.get(0);
        assertThat(savedMessage.getPayload().length(), greaterThan(1000));
        
        JsonNode jsonNode = objectMapper.readTree(savedMessage.getPayload());
        assertThat(jsonNode.get("employeeName").asText(), is(largeEmployeeName));
        assertThat(jsonNode.get("employeeName").asText().length(), is(1000));
    }
}
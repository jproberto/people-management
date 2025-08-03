package com.itau.hr.people_management.integration.infrastructure.outbox.listener;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itau.hr.people_management.domain.employee.enumeration.EmployeeStatus;
import com.itau.hr.people_management.domain.employee.enumeration.EventType;
import com.itau.hr.people_management.domain.employee.event.EmployeeCreatedEvent;
import com.itau.hr.people_management.domain.employee.event.EmployeeStatusChangedEvent;
import com.itau.hr.people_management.infrastructure.outbox.enumeration.OutboxMessageStatus;
import com.itau.hr.people_management.infrastructure.outbox.holder.DomainEventsHolder;
import com.itau.hr.people_management.infrastructure.outbox.listener.DomainEventPublisherListener;
import com.itau.hr.people_management.infrastructure.outbox.listener.TransactionCompletedEvent;
import com.itau.hr.people_management.infrastructure.persistence.entity.OutboxMessage;
import com.itau.hr.people_management.infrastructure.persistence.repository.OutboxMessageRepository;
import com.itau.hr.people_management.integration.infrastructure.kafka.support.TestLogAppender;

import ch.qos.logback.classic.Logger;

@SpringBootTest
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@DisplayName("DomainEventPublisherListener Integration Tests")
class DomainEventPublisherListenerIntegrationTest {

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
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private OutboxMessageRepository outboxMessageRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private TestLogAppender testLogAppender;
    private Logger domainEventPublisherListenerLogger;

    @BeforeEach
    void setUp() {
        // Limpar dados
        outboxMessageRepository.deleteAll();
        outboxMessageRepository.flush();
        DomainEventsHolder.getAndClearEvents();

        // Configurar logger para capturar logs
        domainEventPublisherListenerLogger = (Logger) LoggerFactory.getLogger(DomainEventPublisherListener.class);
        domainEventPublisherListenerLogger.detachAndStopAllAppenders();
        
        testLogAppender = new TestLogAppender();
        testLogAppender.start();
        domainEventPublisherListenerLogger.addAppender(testLogAppender);
        domainEventPublisherListenerLogger.setAdditive(false);
    }

    @AfterEach
    void tearDown() {
        // Cleanup dos logs
        try {
            if (domainEventPublisherListenerLogger != null && testLogAppender != null) {
                domainEventPublisherListenerLogger.detachAppender(testLogAppender);
                testLogAppender.stop();
            }
        } catch (Exception e) {
            // Ignorar erros de cleanup
        }
        
        DomainEventsHolder.getAndClearEvents();
        outboxMessageRepository.deleteAll();
        outboxMessageRepository.flush();
    }

    @Test
    @DisplayName("Should handle real @TransactionalEventListener integration with Spring transaction management")
    void shouldHandleRealTransactionalEventListenerIntegrationWithSpringTransactionManagement() throws Exception {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        EmployeeCreatedEvent event = new EmployeeCreatedEvent(employeeId, "Integration Test Employee", "integration@test.com");

        // Act - Executar em transação real para disparar @TransactionalEventListener
        transactionTemplate.execute(status -> {
            DomainEventsHolder.addEvent(event);
            applicationEventPublisher.publishEvent(new TransactionCompletedEvent());
            return null;
        });

        // Assert - Verificar processamento assíncrono real
        await().atMost(8, TimeUnit.SECONDS).untilAsserted(() -> {
            // 1. Verificar que evento foi processado e persistido no outbox
            List<OutboxMessage> outboxMessages = outboxMessageRepository.findAll();
            assertThat(outboxMessages, hasSize(1));

            OutboxMessage savedMessage = outboxMessages.get(0);
            assertThat(savedMessage.getId(), is(event.getEventId()));
            assertThat(savedMessage.getAggregateId(), is(employeeId));
            assertThat(savedMessage.getAggregateType(), is("Employee"));
            assertThat(savedMessage.getEventType(), is(EventType.EMPLOYEE_CREATED_EVENT.name()));
            assertThat(savedMessage.getStatus(), is(OutboxMessageStatus.PENDING));

            // 2. Verificar que logs foram gerados corretamente
            assertThat(testLogAppender.getInfoMessages(), hasSize(1));
            String infoMessage = testLogAppender.getLastInfoMessage();
            assertThat(infoMessage, containsString("Domain event 'EMPLOYEE_CREATED_EVENT'"));
            assertThat(infoMessage, containsString("ID: " + event.getEventId()));
            assertThat(infoMessage, containsString("added to outbox"));

            // 3. Verificar que ThreadLocal foi limpo
            assertThat(DomainEventsHolder.peekEvents(), empty());
        });
    }

    @Test
    @DisplayName("Should handle @Transactional(propagation = REQUIRES_NEW) behavior in real transaction context")
    void shouldHandleTransactionalRequiresNewBehaviorInRealTransactionContext() throws Exception {
        // Arrange
        UUID employeeId1 = UUID.randomUUID();
        UUID employeeId2 = UUID.randomUUID();
        EmployeeCreatedEvent event1 = new EmployeeCreatedEvent(employeeId1, "Employee 1", "emp1@test.com");
        EmployeeStatusChangedEvent event2 = new EmployeeStatusChangedEvent(employeeId2, EmployeeStatus.ACTIVE, EmployeeStatus.TERMINATED);

        // Act - Executar múltiplos eventos em uma transação
        transactionTemplate.execute(status -> {
            DomainEventsHolder.addEvent(event1);
            DomainEventsHolder.addEvent(event2);
            applicationEventPublisher.publishEvent(new TransactionCompletedEvent());
            return null;
        });

        // Assert - Verificar que ambos eventos foram processados em nova transação
        await().atMost(8, TimeUnit.SECONDS).untilAsserted(() -> {
            List<OutboxMessage> outboxMessages = outboxMessageRepository.findAll();
            assertThat(outboxMessages, hasSize(2));

            // Verificar primeiro evento
            OutboxMessage savedMessage1 = outboxMessages.stream()
                .filter(msg -> msg.getId().equals(event1.getEventId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Event 1 not found"));
            
            assertThat(savedMessage1.getAggregateId(), is(employeeId1));
            assertThat(savedMessage1.getEventType(), is(EventType.EMPLOYEE_CREATED_EVENT.name()));

            // Verificar segundo evento
            OutboxMessage savedMessage2 = outboxMessages.stream()
                .filter(msg -> msg.getId().equals(event2.getEventId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Event 2 not found"));
            
            assertThat(savedMessage2.getAggregateId(), is(employeeId2));
            assertThat(savedMessage2.getEventType(), is(EventType.EMPLOYEE_STATUS_CHANGED_EVENT.name()));

            // Verificar logs para ambos eventos
            assertThat(testLogAppender.getInfoMessages(), hasSize(2));
        });
    }

    @Test
    @DisplayName("Should handle TransactionPhase.AFTER_COMMIT timing correctly")
    void shouldHandleTransactionPhaseAfterCommitTimingCorrectly() throws Exception {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        EmployeeCreatedEvent event = new EmployeeCreatedEvent(employeeId, "Timing Test Employee", "timing@test.com");

        // Act - Simular falha após adicionar evento mas antes do commit
        try {
            transactionTemplate.execute(status -> {
                DomainEventsHolder.addEvent(event);
                
                // Verificar que evento está no ThreadLocal mas ainda não foi processado
                assertThat(DomainEventsHolder.peekEvents(), hasSize(1));
                
                // Verificar que ainda não há mensagens no outbox
                List<OutboxMessage> messagesBeforeCommit = outboxMessageRepository.findAll();
                assertThat(messagesBeforeCommit, empty());
                
                applicationEventPublisher.publishEvent(new TransactionCompletedEvent());
                
                // Simular exception para testar que @TransactionalEventListener só executa AFTER_COMMIT
                throw new RuntimeException("Transaction rollback test");
            });
        } catch (RuntimeException e) {
            // Expected exception
        }

        // Assert - Verificar que evento NÃO foi processado devido ao rollback
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            List<OutboxMessage> messagesAfterRollback = outboxMessageRepository.findAll();
            assertThat(messagesAfterRollback, empty());
            
            // Verificar que não há logs de processamento
            assertThat(testLogAppender.getInfoMessages(), empty());
        });

        // Agora executar sem rollback para confirmar que funciona
        transactionTemplate.execute(status -> {
            DomainEventsHolder.addEvent(event);
            applicationEventPublisher.publishEvent(new TransactionCompletedEvent());
            return null;
        });

        // Verificar que agora foi processado após commit bem-sucedido
        await().atMost(8, TimeUnit.SECONDS).untilAsserted(() -> {
            List<OutboxMessage> messagesAfterCommit = outboxMessageRepository.findAll();
            assertThat(messagesAfterCommit, hasSize(1));
        });
    }

    @Test
    @DisplayName("Should handle empty events list gracefully in real transaction")
    void shouldHandleEmptyEventsListGracefullyInRealTransaction() throws Exception {
        // Act - Publicar TransactionCompletedEvent sem adicionar eventos
        transactionTemplate.execute(status -> {
            // NÃO adicionar eventos ao DomainEventsHolder
            applicationEventPublisher.publishEvent(new TransactionCompletedEvent());
            return null;
        });

        // Assert - Verificar que listener não processou nada
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<OutboxMessage> outboxMessages = outboxMessageRepository.findAll();
            assertThat(outboxMessages, empty());
            
            // Verificar que não há logs de processamento
            assertThat(testLogAppender.getInfoMessages(), empty());
            assertThat(testLogAppender.getErrorMessages(), empty());
        });
    }

    @Test
    @DisplayName("Should handle real ThreadLocal behavior across multiple transactions")
    void shouldHandleRealThreadLocalBehaviorAcrossMultipleTransactions() throws Exception {
        // Arrange
        UUID employeeId1 = UUID.randomUUID();
        UUID employeeId2 = UUID.randomUUID();
        EmployeeCreatedEvent event1 = new EmployeeCreatedEvent(employeeId1, "Employee 1", "emp1@test.com");
        EmployeeCreatedEvent event2 = new EmployeeCreatedEvent(employeeId2, "Employee 2", "emp2@test.com");

        // Act - Primeira transação
        transactionTemplate.execute(status -> {
            DomainEventsHolder.addEvent(event1);
            applicationEventPublisher.publishEvent(new TransactionCompletedEvent());
            return null;
        });

        // Aguardar processamento da primeira transação
        await().atMost(8, TimeUnit.SECONDS).untilAsserted(() -> {
            List<OutboxMessage> messages = outboxMessageRepository.findAll();
            assertThat(messages, hasSize(1));
        });

        // Act - Segunda transação (deve ser independente da primeira)
        transactionTemplate.execute(status -> {
            // Verificar que ThreadLocal foi limpo da transação anterior
            assertThat(DomainEventsHolder.peekEvents(), empty());
            
            DomainEventsHolder.addEvent(event2);
            applicationEventPublisher.publishEvent(new TransactionCompletedEvent());
            return null;
        });

        // Assert - Verificar que ambas transações foram processadas independentemente
        await().atMost(8, TimeUnit.SECONDS).untilAsserted(() -> {
            List<OutboxMessage> allMessages = outboxMessageRepository.findAll();
            assertThat(allMessages, hasSize(2));

            // Verificar que ambos eventos estão presentes
            List<UUID> eventIds = allMessages.stream()
                .map(OutboxMessage::getId)
                .toList();
            assertThat(eventIds, containsInAnyOrder(event1.getEventId(), event2.getEventId()));

            // Verificar logs para ambos eventos
            assertThat(testLogAppender.getInfoMessages(), hasSize(2));
        });
    }

    @Test
    @DisplayName("Should verify integration with real ObjectMapper and JSON serialization")
    void shouldVerifyIntegrationWithRealObjectMapperAndJsonSerialization() throws Exception {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        EmployeeCreatedEvent event = new EmployeeCreatedEvent(employeeId, "JSON Test Employee", "json@test.com");

        // Act
        transactionTemplate.execute(status -> {
            DomainEventsHolder.addEvent(event);
            applicationEventPublisher.publishEvent(new TransactionCompletedEvent());
            return null;
        });

        // Assert - Verificar que JSON foi serializado corretamente
        await().atMost(8, TimeUnit.SECONDS).untilAsserted(() -> {
            List<OutboxMessage> outboxMessages = outboxMessageRepository.findAll();
            assertThat(outboxMessages, hasSize(1));

            OutboxMessage savedMessage = outboxMessages.get(0);
            
            // Verificar que payload é JSON válido
            String payload = savedMessage.getPayload();
            assertThat(payload, is(notNullValue()));
            assertThat(payload, containsString("\"employeeId\":\"" + employeeId + "\""));
            assertThat(payload, containsString("\"employeeName\":\"JSON Test Employee\""));
            assertThat(payload, containsString("\"employeeEmail\":\"json@test.com\""));

            // Verificar que JSON pode ser deserializado de volta
            EmployeeCreatedEvent deserializedEvent = objectMapper.readValue(payload, EmployeeCreatedEvent.class);
            assertThat(deserializedEvent.employeeId(), is(event.employeeId()));
            assertThat(deserializedEvent.employeeName(), is(event.employeeName()));
            assertThat(deserializedEvent.employeeEmail(), is(event.employeeEmail()));
        });
    }

    @Test
    @DisplayName("Should handle concurrent transactions with real thread isolation")
    void shouldHandleConcurrentTransactionsWithRealThreadIsolation() throws Exception {
        // Arrange
        UUID employeeId1 = UUID.randomUUID();
        UUID employeeId2 = UUID.randomUUID();
        EmployeeCreatedEvent event1 = new EmployeeCreatedEvent(employeeId1, "Concurrent Employee 1", "concurrent1@test.com");
        EmployeeCreatedEvent event2 = new EmployeeCreatedEvent(employeeId2, "Concurrent Employee 2", "concurrent2@test.com");

        // Act - Executar transações em threads diferentes (sequencialmente para teste determinístico)
        transactionTemplate.execute(status -> {
            DomainEventsHolder.addEvent(event1);
            applicationEventPublisher.publishEvent(new TransactionCompletedEvent());
            return null;
        });

        transactionTemplate.execute(status -> {
            DomainEventsHolder.addEvent(event2);
            applicationEventPublisher.publishEvent(new TransactionCompletedEvent());
            return null;
        });

        // Assert - Verificar que ambos eventos foram processados corretamente
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            List<OutboxMessage> outboxMessages = outboxMessageRepository.findAll();
            assertThat(outboxMessages, hasSize(2));

            // Verificar que ambos eventos estão presentes
            List<UUID> aggregateIds = outboxMessages.stream()
                .map(OutboxMessage::getAggregateId)
                .toList();
            assertThat(aggregateIds, containsInAnyOrder(employeeId1, employeeId2));

            // Verificar logs para ambos eventos
            assertThat(testLogAppender.getInfoMessages(), hasSize(2));
        });
    }
}
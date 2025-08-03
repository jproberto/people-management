package com.itau.hr.people_management.integration.infrastructure.kafka;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itau.hr.people_management.domain.employee.enumeration.EmployeeStatus;
import com.itau.hr.people_management.domain.employee.enumeration.EventType;
import com.itau.hr.people_management.domain.employee.event.EmployeeCreatedEvent;
import com.itau.hr.people_management.domain.employee.event.EmployeeStatusChangedEvent;
import com.itau.hr.people_management.domain.employee.history.EmployeeEvent;
import com.itau.hr.people_management.domain.employee.repository.EmployeeEventRepository;
import com.itau.hr.people_management.integration.infrastructure.kafka.support.TestLogAppender;

import ch.qos.logback.classic.Logger;

@SpringBootTest(properties = {
    // ========== KAFKA CONFIGURATION ==========
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.consumer.auto-offset-reset=earliest", 
    "spring.kafka.consumer.group-id=${random.uuid}",
    "spring.kafka.admin.properties.request.timeout.ms=20000",
    "spring.kafka.admin.properties.default.api.timeout.ms=20000",
    "spring.kafka.producer.retries=0",
    "spring.kafka.producer.acks=1", 
    "spring.kafka.consumer.session.timeout.ms=10000",
    "spring.kafka.consumer.heartbeat.interval.ms=3000",
    
    // ========== DATABASE CONFIGURATION (H2) ==========
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",  // ✅ Driver H2
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    
    // ========== JPA/HIBERNATE CONFIGURATION ==========
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",  // ✅ Dialeto H2
    "spring.jpa.show-sql=false",
    
    // ========== FLYWAY CONFIGURATION ==========
    "spring.flyway.enabled=false",  // ✅ Desabilitado para H2 in-memory
    
    // ========== LOGGING CONFIGURATION ==========
    "logging.level.org.springframework.kafka=WARN",
    "logging.level.org.hibernate=WARN",
    "logging.level.org.h2=WARN"
})
@EmbeddedKafka(
    partitions = 1,
    controlledShutdown = false,
    topics = {"employee.created", "employee.status.changed"},
    brokerProperties = {
        "offsets.topic.replication.factor=1",
        "auto.create.topics.enable=true",
        "log.flush.interval.messages=1", 
        "log.flush.interval.ms=1"
    }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("EmployeeHistoryUpdater Integration Tests with Embedded Kafka")
class EmployeeHistoryUpdaterIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmployeeEventRepository employeeEventRepository;

    private TestLogAppender testLogAppender;
    private Logger employeeHistoryUpdaterLogger;

    @BeforeEach
    void setUp() {
        employeeHistoryUpdaterLogger = (Logger) LoggerFactory.getLogger("com.itau.hr.people_management.infrastructure.kafka.EmployeeHistoryUpdater");
        testLogAppender = new TestLogAppender();
        testLogAppender.start();
        employeeHistoryUpdaterLogger.addAppender(testLogAppender);
    }

    @AfterEach
    void tearDown() {
        if (employeeHistoryUpdaterLogger != null && testLogAppender != null) {
            employeeHistoryUpdaterLogger.detachAppender(testLogAppender);
            testLogAppender.stop();
        }
        reset(employeeEventRepository);
    }

    @Test
    @DisplayName("Should consume EmployeeCreatedEvent and save to history repository")
    void shouldConsumeEmployeeCreatedEventAndSaveToHistoryRepository() throws Exception {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        EmployeeCreatedEvent event = new EmployeeCreatedEvent(employeeId, "John Doe", "john.doe@example.com");
        String eventJson = objectMapper.writeValueAsString(event);

        // Act
        kafkaTemplate.send("employee.created", eventJson);

        // Assert - Verificar integração completa: Kafka → Deserialization → Repository Save
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(employeeEventRepository, times(1)).save(argThat(savedEvent -> 
                savedEvent.getId().equals(event.eventId()) &&
                savedEvent.getEmployeeId().equals(event.employeeId()) &&
                savedEvent.getEventType() == EventType.EMPLOYEE_CREATED_EVENT &&
                savedEvent.getDescription().contains("John Doe") &&
                savedEvent.getDescription().contains("john.doe@example.com") &&
                savedEvent.getOccurredOn().equals(event.occurredOn()) &&
                savedEvent.getEventData().equals(eventJson)
            ));

            // Verificar logs de processamento
            assertThat(testLogAppender.getInfoMessages(), hasSize(greaterThan(0)));
            String infoMessage = testLogAppender.getLastInfoMessage();
            assertThat(infoMessage, containsString("KAFKA_CONSUMER: Handling EMPLOYEE_CREATED_EVENT for history update"));
            assertThat(infoMessage, containsString("Employee ID: " + employeeId));
        });
    }

    @Test
    @DisplayName("Should consume EmployeeStatusChangedEvent and save to history repository")
    void shouldConsumeEmployeeStatusChangedEventAndSaveToHistoryRepository() throws Exception {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        EmployeeStatusChangedEvent event = new EmployeeStatusChangedEvent(
            employeeId, EmployeeStatus.ACTIVE, EmployeeStatus.TERMINATED
        );
        String eventJson = objectMapper.writeValueAsString(event);

        // Act
        kafkaTemplate.send("employee.status.changed", eventJson);

        // Assert - Verificar integração completa
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(employeeEventRepository, times(1)).save(argThat(savedEvent -> 
                savedEvent.getId().equals(event.eventId()) &&
                savedEvent.getEmployeeId().equals(event.employeeId()) &&
                savedEvent.getEventType() == EventType.EMPLOYEE_STATUS_CHANGED_EVENT &&
                savedEvent.getDescription().contains("ACTIVE") &&
                savedEvent.getDescription().contains("TERMINATED") &&
                savedEvent.getOccurredOn().equals(event.occurredOn()) &&
                savedEvent.getEventData().equals(eventJson)
            ));

            // Verificar logs de processamento
            assertThat(testLogAppender.getInfoMessages(), hasSize(greaterThan(0)));
            String infoMessage = testLogAppender.getLastInfoMessage();
            assertThat(infoMessage, containsString("KAFKA_CONSUMER: Handling EMPLOYEE_STATUS_CHANGED_EVENT for history update"));
            assertThat(infoMessage, containsString("Employee ID: " + employeeId));
        });
    }

    @Test
    @DisplayName("Should handle malformed JSON message and log deserialization error")
    void shouldHandleMalformedJsonMessageAndLogDeserializationError() {
        // Arrange
        String malformedJson = "{\"invalid\":\"json\",\"missing\":";

        // Act
        kafkaTemplate.send("employee.created", malformedJson);

        // Assert - Verificar error handling e logging
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            // Verificar que não foi salvo no repository
            verify(employeeEventRepository, never()).save(any(EmployeeEvent.class));

            // Verificar logs de erro
            assertThat(testLogAppender.getErrorMessages(), hasSize(greaterThan(0)));
            String errorMessage = testLogAppender.getLastErrorMessage();
            assertThat(errorMessage, containsString("DESERIALIZATION_ERROR: Failed to deserialize EMPLOYEE_CREATED_EVENT for history"));
            assertThat(errorMessage, containsString("Invalid JSON"));
        });
    }

    @Test
    @DisplayName("Should handle repository save failure and log error")
    void shouldHandleRepositorySaveFailureAndLogError() throws Exception {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        EmployeeCreatedEvent event = new EmployeeCreatedEvent(employeeId, "John Doe", "john.doe@example.com");
        String eventJson = objectMapper.writeValueAsString(event);
        doThrow(new NullPointerException("Failed to save event")).when(employeeEventRepository).save(any(EmployeeEvent.class));

        // Act
        kafkaTemplate.send("employee.created", eventJson);

        // Assert - Verificar error handling
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(employeeEventRepository, times(1)).save(any(EmployeeEvent.class));

            // Verificar logs de erro
            assertThat(testLogAppender.getErrorMessages(), hasSize(greaterThan(0)));
            String errorMessage = testLogAppender.getLastErrorMessage();
            assertThat(errorMessage, containsString("HISTORY_SAVE_ERROR: Failed to save EMPLOYEE_CREATED_EVENT for history"));
        });
    }

    @Test
    @DisplayName("Should handle empty message and log error")
    void shouldHandleEmptyMessageAndLogError() {
        // Act
        kafkaTemplate.send("employee.created", "");

        // Assert
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(employeeEventRepository, never()).save(any(EmployeeEvent.class));

            assertThat(testLogAppender.getErrorMessages(), hasSize(greaterThan(0)));
            String errorMessage = testLogAppender.getLastErrorMessage();
            assertThat(errorMessage, containsString("KAFKA_ERROR: Error handling EMPLOYEE_CREATED_EVENT for history"));
        });
    }

    @Test
    @DisplayName("Should handle invalid event structure and log reflection error")
    void shouldHandleInvalidEventStructureAndLogReflectionError() throws Exception {
        // Arrange - JSON válido mas sem métodos esperados (employeeId, eventId, occurredOn)
        String invalidEventJson = objectMapper.writeValueAsString(
            new InvalidEvent("not-an-uuid", "invalid-structure")
        );

        // Act
        kafkaTemplate.send("employee.created", invalidEventJson);

        // Assert
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(employeeEventRepository, never()).save(any(EmployeeEvent.class));

            assertThat(testLogAppender.getErrorMessages(), hasSize(greaterThan(0)));
            String errorMessage = testLogAppender.getLastErrorMessage();
            assertThat(errorMessage, containsString("KAFKA_ERROR: Invalid EMPLOYEE_CREATED_EVENT received."));
        });
    }

    @Test
    @DisplayName("Should process multiple events from different topics with transactional behavior")
    void shouldProcessMultipleEventsFromDifferentTopicsWithTransactionalBehavior() throws Exception {
        // Arrange
        EmployeeCreatedEvent createdEvent = new EmployeeCreatedEvent(
            UUID.randomUUID(), "Alice Smith", "alice@example.com"
        );
        EmployeeStatusChangedEvent statusEvent = new EmployeeStatusChangedEvent(
            UUID.randomUUID(), EmployeeStatus.ACTIVE, EmployeeStatus.TERMINATED
        );

        String createdEventJson = objectMapper.writeValueAsString(createdEvent);
        String statusEventJson = objectMapper.writeValueAsString(statusEvent);

        // Act
        kafkaTemplate.send("employee.created", createdEventJson);
        kafkaTemplate.send("employee.status.changed", statusEventJson);

        // Assert - Verificar que ambos os eventos foram processados independentemente
        await().atMost(20, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(employeeEventRepository, atLeast(1)).save(any(EmployeeEvent.class));

            // Verificar logs para ambos os eventos
            assertThat(testLogAppender.getInfoMessages(), hasSize(greaterThanOrEqualTo(2)));
            
            assertThat(testLogAppender.getInfoMessages().stream()
                .anyMatch(msg -> msg.contains("EMPLOYEE_CREATED_EVENT") && msg.contains(createdEvent.employeeId().toString())), 
                is(true));
            
            assertThat(testLogAppender.getInfoMessages().stream()
                .anyMatch(msg -> msg.contains("EMPLOYEE_STATUS_CHANGED_EVENT") && msg.contains(statusEvent.employeeId().toString())), 
                is(true));
        });
    }

    @Test
    @DisplayName("Should verify complete integration chain: Kafka → Deserialization → Reflection → Repository → Transaction")
    void shouldVerifyCompleteIntegrationChainKafkaToDeserializationToReflectionToRepositoryToTransaction() throws Exception {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        EmployeeCreatedEvent event = new EmployeeCreatedEvent(employeeId, "Integration Test User", "integration@test.com");
        String eventJson = objectMapper.writeValueAsString(event);

        // Act
        kafkaTemplate.send("employee.created", eventJson);

        // Assert - Verificar cadeia completa de integração
         await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            // 1. Verificar que o repository foi chamado com dados corretos
            verify(employeeEventRepository, times(1)).save(argThat(savedEvent -> {
                return savedEvent.getId().equals(event.eventId()) &&
                       savedEvent.getEmployeeId().equals(event.employeeId()) &&
                       savedEvent.getEventType() == EventType.EMPLOYEE_CREATED_EVENT &&
                       savedEvent.getDescription().equals("Colaborador 'Integration Test User' criado com e-mail: integration@test.com") &&
                       savedEvent.getOccurredOn().equals(event.occurredOn()) &&
                       savedEvent.getEventData().equals(eventJson);
            }));

            // 2. Verificar logs de processamento (INFO level)
            assertThat(testLogAppender.getInfoMessages(), hasSize(1));
            String infoMessage = testLogAppender.getLastInfoMessage();
            assertThat(infoMessage, containsString("KAFKA_CONSUMER: Handling EMPLOYEE_CREATED_EVENT for history update"));
            assertThat(infoMessage, containsString("Employee ID: " + employeeId));

            // 3. Verificar que não há logs de erro
            assertThat(testLogAppender.getErrorMessages(), hasSize(0));
        });
    }

    @Test
    @DisplayName("Should handle large message payload and truncate in error logs")
    void shouldHandleLargeMessagePayloadAndTruncateInErrorLogs() {
        // Arrange - Mensagem com payload muito grande
        String largeDescription = "A".repeat(1000);
        String largeEventJson = String.format(
            "{\"employeeId\":\"%s\",\"employeeName\":\"%s\",\"employeeEmail\":\"large@example.com\",\"extraField\":\"invalid\"}",
            UUID.randomUUID(), largeDescription
        );

        // Act
        kafkaTemplate.send("employee.created", largeEventJson);

        // Assert - Verificar truncamento de mensagens grandes em logs de erro
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(employeeEventRepository, never()).save(any(EmployeeEvent.class));

            assertThat(testLogAppender.getErrorMessages(), hasSize(greaterThan(0)));
            String errorMessage = testLogAppender.getLastErrorMessage();
            
            // Verificar que a mensagem foi truncada (500 chars + "...")
            if (largeEventJson.length() > 500) {
                assertThat(errorMessage, containsString("..."));
                assertThat(errorMessage.length(), is(lessThan(largeEventJson.length())));
            }
        });
    }

    // Helper class para testar estrutura inválida
    private record InvalidEvent(String invalidField, String anotherField) {}
}
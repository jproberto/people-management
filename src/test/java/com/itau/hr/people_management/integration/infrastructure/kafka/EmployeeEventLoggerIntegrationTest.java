package com.itau.hr.people_management.integration.infrastructure.kafka;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itau.hr.people_management.domain.employee.enumeration.EmployeeStatus;
import com.itau.hr.people_management.domain.employee.event.EmployeeCreatedEvent;
import com.itau.hr.people_management.domain.employee.event.EmployeeStatusChangedEvent;
import com.itau.hr.people_management.integration.infrastructure.kafka.support.TestLogAppender;

import ch.qos.logback.classic.Logger;

@SpringBootTest(properties = {
    "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
    "spring.kafka.consumer.auto-offset-reset=earliest", 
    "spring.kafka.consumer.group-id=${random.uuid}",
    "spring.kafka.admin.properties.request.timeout.ms=20000",
    "spring.kafka.admin.properties.default.api.timeout.ms=20000",
    "spring.kafka.producer.retries=0",
    "spring.kafka.producer.acks=1", 
    "spring.kafka.consumer.session.timeout.ms=10000",
    "spring.kafka.consumer.heartbeat.interval.ms=3000"
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
@DisplayName("EmployeeEventLogger Integration Tests with Embedded Kafka")
class EmployeeEventLoggerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    private TestLogAppender testLogAppender;
    private Logger employeeEventLoggerLogger;

    @BeforeEach
    void setUp() {
        // ✅ CONECTAR o TestLogAppender ao logger real do EmployeeEventLogger
        employeeEventLoggerLogger = (Logger) LoggerFactory.getLogger("com.itau.hr.people_management.infrastructure.kafka.EmployeeEventLogger");
        testLogAppender = new TestLogAppender();
        testLogAppender.start();
        employeeEventLoggerLogger.addAppender(testLogAppender);
    }

    @AfterEach
    void tearDown() {
        // ✅ LIMPAR configuração após cada teste
        if (employeeEventLoggerLogger != null && testLogAppender != null) {
            employeeEventLoggerLogger.detachAppender(testLogAppender);
            testLogAppender.stop();
        }
    }

    @Test
    @DisplayName("Should consume EmployeeCreatedEvent from Kafka and log correctly")
    void shouldConsumeEmployeeCreatedEventFromKafkaAndLogCorrectly() throws Exception {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        EmployeeCreatedEvent event = new EmployeeCreatedEvent(employeeId, "John Doe", "john.doe@example.com");
        String eventJson = objectMapper.writeValueAsString(event);

        // Act
        kafkaTemplate.send("employee.created", eventJson);

        // Assert
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(testLogAppender.getLogEvents(), hasSize(greaterThan(0)));
            
            String logMessage = testLogAppender.getLastInfoMessage();
            assertThat(logMessage, containsString("EVENT_RECEIVED: EMPLOYEE_CREATED_EVENT"));
            assertThat(logMessage, containsString("EventId: " + event.eventId()));
            assertThat(logMessage, containsString("EmployeeId: " + event.employeeId()));
            assertThat(logMessage, containsString("Name: John Doe"));
            assertThat(logMessage, containsString("Email: john.doe@example.com"));
            assertThat(logMessage, containsString("OccurredOn: " + event.occurredOn()));
        });
    }

    @Test
    @DisplayName("Should consume EmployeeStatusChangedEvent from Kafka and log correctly")
    void shouldConsumeEmployeeStatusChangedEventFromKafkaAndLogCorrectly() throws Exception {
        // Arrange
        UUID employeeId = UUID.randomUUID();
        EmployeeStatusChangedEvent event = new EmployeeStatusChangedEvent(
            employeeId, EmployeeStatus.ACTIVE, EmployeeStatus.TERMINATED
        );
        String eventJson = objectMapper.writeValueAsString(event);

        // Act
        kafkaTemplate.send("employee.status.changed", eventJson);

        // Assert
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(testLogAppender.getLogEvents(), hasSize(greaterThan(0)));
            
            String logMessage = testLogAppender.getLastInfoMessage();
            assertThat(logMessage, containsString("EVENT_RECEIVED: EMPLOYEE_STATUS_CHANGED_EVENT"));
            assertThat(logMessage, containsString("EventId: " + event.eventId()));
            assertThat(logMessage, containsString("EmployeeId: " + event.employeeId()));
            assertThat(logMessage, containsString("OldStatus: ACTIVE"));
            assertThat(logMessage, containsString("NewStatus: TERMINATED"));
            assertThat(logMessage, containsString("OccurredOn: " + event.occurredOn()));
        });
    }

    @Test
    @DisplayName("Should handle malformed JSON message and log error")
    void shouldHandleMalformedJsonMessageAndLogError() {
        // Arrange
        String malformedJson = "{\"invalid\":\"json\",\"missing\":";

        // Act
        kafkaTemplate.send("employee.created", malformedJson);

        // Assert
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(testLogAppender.getErrorMessages(), hasSize(greaterThan(0)));
            
            String errorMessage = testLogAppender.getLastErrorMessage();
            assertThat(errorMessage, containsString("KAFKA_ERROR: Failed to process EmployeeCreatedEvent for logging"));
        });
    }

    @Test
    @DisplayName("Should handle invalid event structure and log error")
    void shouldHandleInvalidEventStructureAndLogError() throws Exception {
        // Arrange - JSON válido mas estrutura incorreta
        String invalidEventJson = objectMapper.writeValueAsString(
            new InvalidEvent("not-an-uuid", "invalid-structure")
        );

        // Act
        kafkaTemplate.send("employee.created", invalidEventJson);

        // Assert
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(testLogAppender.getErrorMessages(), hasSize(greaterThan(0)));
            
            String errorMessage = testLogAppender.getLastErrorMessage();
            assertThat(errorMessage, containsString("KAFKA_ERROR: Invalid EmployeeCreatedEvent received."));
        });
    }

    @Test
    @DisplayName("Should handle empty message gracefully and log error")
    void shouldHandleEmptyMessageGracefullyAndLogError() {
        // Act
        kafkaTemplate.send("employee.created", "");

        // Assert
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(testLogAppender.getErrorMessages(), hasSize(greaterThan(0)));
            
            String errorMessage = testLogAppender.getLastErrorMessage();
            assertThat(errorMessage, containsString("KAFKA_ERROR: Failed to process EmployeeCreatedEvent for logging"));
        });
    }

    @Test
    @DisplayName("Should handle null event gracefully and log warning")
    void shouldHandleNullEventGracefullyAndLogWarning() {
        // Arrange - Simular evento que resulta em null após deserialização
        String nullEventJson = "null";

        // Act
        kafkaTemplate.send("employee.created", nullEventJson);

        // Assert
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            // Pode gerar warning ou error dependendo da implementação
            assertThat(testLogAppender.getLogEvents(), hasSize(greaterThan(0)));
            
            boolean hasWarningOrError = !testLogAppender.getErrorMessages().isEmpty() || 
                                      testLogAppender.getLogEvents().stream()
                                          .anyMatch(event -> event.getLevel().toString().equals("WARN"));
            assertThat(hasWarningOrError, is(true));
        });
    }

    @Test
    @DisplayName("Should process multiple events from different topics concurrently")
    void shouldProcessMultipleEventsFromDifferentTopicsConcurrently() throws Exception {
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

        // Assert
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            assertThat(testLogAppender.getInfoMessages(), hasSize(greaterThanOrEqualTo(2)));
            
            assertThat(testLogAppender.getInfoMessages().stream()
                .anyMatch(msg -> msg.contains("EMPLOYEE_CREATED_EVENT") && msg.contains("Alice Smith")), 
                is(true));
            
            assertThat(testLogAppender.getInfoMessages().stream()
                .anyMatch(msg -> msg.contains("EMPLOYEE_STATUS_CHANGED_EVENT") && 
                                msg.contains("ACTIVE") && msg.contains("TERMINATED")), 
                is(true));
        });
    }

    @Test
    @DisplayName("Should verify complete integration chain: Kafka → Deserialization → Logging")
    void shouldVerifyCompleteIntegrationChainKafkaToDeserializationToLogging() throws Exception {
        // Arrange - Evento com todos os campos preenchidos
        UUID employeeId = UUID.randomUUID();
        EmployeeCreatedEvent event = new EmployeeCreatedEvent(employeeId, "Integration Test User", "integration@test.com");
        String eventJson = objectMapper.writeValueAsString(event);

        // Act
        kafkaTemplate.send("employee.created", eventJson);

        // Assert - Verificar integração completa
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            // Verificar que exatamente 1 log foi gerado
            assertThat(testLogAppender.getInfoMessages(), hasSize(1));
            
            String logMessage = testLogAppender.getLastInfoMessage();
            
            // Verificar estrutura completa do log
            assertThat(logMessage, containsString("EVENT_RECEIVED: EMPLOYEE_CREATED_EVENT"));
            assertThat(logMessage, containsString("EventId: " + event.eventId()));
            assertThat(logMessage, containsString("EmployeeId: " + event.employeeId()));
            assertThat(logMessage, containsString("Name: Integration Test User"));
            assertThat(logMessage, containsString("Email: integration@test.com"));
            assertThat(logMessage, containsString("OccurredOn: " + event.occurredOn()));
            
            // Verificar que não há logs de erro
            assertThat(testLogAppender.getErrorMessages(), hasSize(0));
        });
    }

    // Helper class para testar estrutura inválida
    private record InvalidEvent(String invalidField, String anotherField) {}
}
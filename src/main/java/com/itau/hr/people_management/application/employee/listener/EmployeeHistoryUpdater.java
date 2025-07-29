package com.itau.hr.people_management.application.employee.listener;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itau.hr.people_management.domain.employee.enumeration.EventType;
import com.itau.hr.people_management.domain.employee.event.EmployeeCreatedEvent;
import com.itau.hr.people_management.domain.employee.event.EmployeeStatusChangedEvent;
import com.itau.hr.people_management.domain.employee.history.EmployeeEvent;
import com.itau.hr.people_management.domain.employee.repository.EmployeeEventRepository;

@Component
public class EmployeeHistoryUpdater {

    private static final Logger log = LoggerFactory.getLogger(EmployeeHistoryUpdater.class);
    private final EmployeeEventRepository employeeEventRepository;
    private final ObjectMapper objectMapper;

    public EmployeeHistoryUpdater(EmployeeEventRepository employeeEventRepository, ObjectMapper objectMapper) {
        this.employeeEventRepository = employeeEventRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "employee.created", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void handleEmployeeCreatedEvent(String message) {
        processEvent(message, EmployeeCreatedEvent.class, EventType.EMPLOYEE_CREATED_EVENT, 
            event -> "Colaborador '" + event.employeeName() + "' criado com e-mail: " + event.employeeEmail());
    }

    @KafkaListener(topics = "employee.status.changed", groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void handleEmployeeStatusChangedEvent(String message) {
        processEvent(message, EmployeeStatusChangedEvent.class, EventType.EMPLOYEE_STATUS_CHANGED_EVENT, 
            event -> "Status do colaborador alterado de '" + event.oldStatus() + "' para '" + event.newStatus() + "'");
    }

    private <T> void processEvent(String message, Class<T> eventClass, EventType eventType, java.util.function.Function<T, String> descriptionProvider) {
        try {
            validateMessage(message);
            
            T event = deserializeEvent(message, eventClass);
            validateEvent(event);
            
            UUID employeeId = extractEmployeeId(event, eventClass);
            UUID eventId = extractEventId(event, eventClass);
            Instant occurredOn = extractOccurredOn(event, eventClass);

            log.info("KAFKA_CONSUMER: Handling {} for history update for Employee ID: {}", eventType, employeeId);

            EmployeeEvent historyEvent = createHistoryEvent(eventId, employeeId, eventType, descriptionProvider.apply(event), occurredOn, message);
            saveHistoryEvent(historyEvent);
            
            log.debug("KAFKA_CONSUMER: {} history saved for employee ID: {}", eventType, employeeId);
        } catch (JsonProcessingException e) {
            handleDeserializationError(eventType, message, e);
        } catch (ReflectiveOperationException e) {
            handleReflectionError(eventType, message, e);
        } catch (Exception e) {
            handleUnexpectedError(eventType, message, e);
        }
    }

    private void validateMessage(String message) {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be null or empty");
        }
    }

    private <T> T deserializeEvent(String message, Class<T> eventClass) throws JsonProcessingException {
        return objectMapper.readValue(message, eventClass);
    }

    private void validateEvent(Object event) {
        if (event == null) {
            throw new IllegalArgumentException("Deserialized event cannot be null");
        }
    }

    private <T> UUID extractEmployeeId(T event, Class<T> eventClass) throws ReflectiveOperationException {
        return (UUID) eventClass.getMethod("employeeId").invoke(event);
    }

    private <T> UUID extractEventId(T event, Class<T> eventClass) throws ReflectiveOperationException {
        return (UUID) eventClass.getMethod("eventId").invoke(event);
    }

    private <T> Instant extractOccurredOn(T event, Class<T> eventClass) throws ReflectiveOperationException {
        return (Instant) eventClass.getMethod("occurredOn").invoke(event);
    }

    private EmployeeEvent createHistoryEvent(UUID eventId, UUID employeeId, EventType eventType, 
                                           String description, Instant occurredOn, String rawMessage) {
        return EmployeeEvent.builder()
            .id(eventId)
            .employeeId(employeeId)
            .eventType(eventType)
            .description(description)
            .occurredOn(occurredOn)
            .eventData(rawMessage)
            .build();
    }

    private void saveHistoryEvent(EmployeeEvent historyEvent) {
        try {
            employeeEventRepository.save(historyEvent);
        } catch (Exception e) {
            log.error("DATABASE_ERROR: Failed to save history event {} for employee {}", historyEvent.getId(), historyEvent.getEmployeeId(), e);
            throw new RuntimeException("Failed to save employee history event", e);
        }
    }

    private void handleDeserializationError(EventType eventType, String message, JsonProcessingException e) {
        log.error("DESERIALIZATION_ERROR: Failed to deserialize {} for history. Invalid JSON: {}", eventType, truncateMessage(message), e);
        throw new RuntimeException("Failed to deserialize " + eventType.name() + " for history update.", e);
    }

    private void handleReflectionError(EventType eventType, String message, ReflectiveOperationException e) {
        log.error("REFLECTION_ERROR: Failed to extract properties from {} for history. Message: {}", eventType, truncateMessage(message), e);
        throw new RuntimeException("Failed to extract properties from " + eventType.name() + " for history update.", e);
    }

    private void handleUnexpectedError(EventType eventType, String message, Exception e) {
        log.error("KAFKA_ERROR: Error handling {} for history. Message: {}", eventType, truncateMessage(message), e);
        throw new RuntimeException("Failed to process " + eventType.name() + " for history update.", e);
    }

    private String truncateMessage(String message) {
        final int maxLength = 500;
        return message != null && message.length() > maxLength 
            ? message.substring(0, maxLength) + "..." 
            : message;
    }
}

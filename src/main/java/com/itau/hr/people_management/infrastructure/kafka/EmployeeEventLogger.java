package com.itau.hr.people_management.infrastructure.kafka;

import java.util.UUID;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itau.hr.people_management.domain.employee.enumeration.EventType;
import com.itau.hr.people_management.domain.employee.event.EmployeeCreatedEvent;
import com.itau.hr.people_management.domain.employee.event.EmployeeStatusChangedEvent;

@Component
public class EmployeeEventLogger {

    private static final Logger log = LoggerFactory.getLogger(EmployeeEventLogger.class);
    private final ObjectMapper objectMapper;
    private static final String EVENT_RECEIVED_TEMPLATE = "EVENT_RECEIVED: {} | EventId: {} | EmployeeId: {} | {}";
    
    public EmployeeEventLogger(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "employee.created", groupId = "${spring.kafka.consumer.group-id}")
    public void listenEmployeeCreated(String message) {
        processEvent(message, EmployeeCreatedEvent.class, this::logEmployeeCreatedEvent, "EmployeeCreatedEvent");
    }

    @KafkaListener(topics = "employee.status.changed", groupId = "${spring.kafka.consumer.group-id}")
    public void listenEmployeeStatusChanged(String message) {
        processEvent(message, EmployeeStatusChangedEvent.class, this::logEmployeeStatusChangedEvent, "EmployeeStatusChangedEvent");
    }

    private <T> void processEvent(String message, Class<T> eventClass, Consumer<T> logFunction, String eventType) {
        try {
            T event = deserializeEvent(message, eventClass);
            logFunction.accept(event);
        } catch (JsonProcessingException e) {
            log.error("KAFKA_ERROR: Failed to process {} for logging.", eventType, e);
        } catch (Exception e) {
            log.error("KAFKA_ERROR: Error processing {} for logging. Message: {}", eventType, message, e);
        }
    }

    private void logEmployeeCreatedEvent(EmployeeCreatedEvent event) {
        if (event == null) {
            log.warn("KAFKA_WARNING: Received null EmployeeCreatedEvent");
            return;
        }
        
        logEvent(
            EventType.EMPLOYEE_CREATED_EVENT,
            event.eventId(),
            event.employeeId(),
            String.format("Name: %s | Email: %s | OccurredOn: %s",
                event.employeeName(),
                event.employeeEmail(),
                event.occurredOn())
        );
    }

    private void logEmployeeStatusChangedEvent(EmployeeStatusChangedEvent event) {
        if (event == null) {
            log.warn("KAFKA_WARNING: Received null EmployeeStatusChangedEvent");
            return;
        }
        
        logEvent(
            EventType.EMPLOYEE_STATUS_CHANGED_EVENT,
            event.eventId(),
            event.employeeId(),
            String.format("OldStatus: %s | NewStatus: %s | OccurredOn: %s",
                event.oldStatus(),
                event.newStatus(),
                event.occurredOn())
        );
    }

    private void logEvent(EventType eventType, UUID eventId, UUID employeeId, String eventDetails) {
        log.info(EVENT_RECEIVED_TEMPLATE, eventType.name(), eventId, employeeId, eventDetails);
    }

    private <T> T deserializeEvent(String message, Class<T> eventClass) throws JsonProcessingException {
        return objectMapper.readValue(message, eventClass);
    }
}
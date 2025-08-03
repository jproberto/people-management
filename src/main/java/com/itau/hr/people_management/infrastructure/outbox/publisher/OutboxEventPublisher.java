package com.itau.hr.people_management.infrastructure.outbox.publisher;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itau.hr.people_management.domain.employee.event.EmployeeCreatedEvent;
import com.itau.hr.people_management.domain.employee.event.EmployeeStatusChangedEvent;
import com.itau.hr.people_management.domain.employee.event.EventPublisher;
import com.itau.hr.people_management.domain.shared.event.DomainEvent;
import com.itau.hr.people_management.infrastructure.outbox.enumeration.OutboxMessageStatus;
import com.itau.hr.people_management.infrastructure.outbox.exception.OutboxEventSerializationException;
import com.itau.hr.people_management.infrastructure.persistence.entity.OutboxMessage;
import com.itau.hr.people_management.infrastructure.persistence.repository.OutboxMessageRepository;

@Component
public class OutboxEventPublisher implements EventPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutboxEventPublisher.class);
    private final OutboxMessageRepository outboxMessageRepository;
    private final ObjectMapper objectMapper;

    public OutboxEventPublisher(OutboxMessageRepository outboxMessageRepository, ObjectMapper objectMapper) {
        this.outboxMessageRepository = outboxMessageRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(DomainEvent event) {
        try {
            UUID aggregateId = null;
            String aggregateType = null;

            switch (event) {
                case EmployeeCreatedEvent employeeCreatedEvent -> {
                    aggregateId = employeeCreatedEvent.getEmployeeId();
                    aggregateType = "Employee";
                }
                case EmployeeStatusChangedEvent employeeStatusChangedEvent -> {
                    aggregateId = employeeStatusChangedEvent.getEmployeeId();
                    aggregateType = "Employee";
                }
                default -> log.warn("Unknown DomainEvent type: {}. Cannot determine aggregateId/Type. Storing with nulls.", event.getClass().getName());
            }

            String payload = objectMapper.writeValueAsString(event);

            OutboxMessage outboxMessage = OutboxMessage.builder()
                .id(event.getEventId())
                .occurredOn(event.getOccurredOn())
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(event.getEventType().name())
                .payload(payload)
                .status(OutboxMessageStatus.PENDING)
                .nextAttemptAt(Instant.now())
                .build();

            outboxMessageRepository.save(outboxMessage);
            log.info("Event {} saved to outbox for aggregateType: {}, aggregateId: {}", event.getClass().getSimpleName(), aggregateType, aggregateId);

        } catch (JsonProcessingException e) {
            throw new OutboxEventSerializationException("Failed to serialize event of type " + event.getClass().getName() + " with ID " + event.getEventId() + " to JSON for outbox", e);
        } catch (Exception e) {
            log.error("Failed to publish event of type " + event.getClass().getName() + " with ID " + event.getEventId() + " to outbox", e);
        }
    }
}

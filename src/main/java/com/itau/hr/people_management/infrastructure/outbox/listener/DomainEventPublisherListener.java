package com.itau.hr.people_management.infrastructure.outbox.listener;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import com.itau.hr.people_management.domain.shared.event.DomainEvent;
import com.itau.hr.people_management.infrastructure.outbox.holder.DomainEventsHolder;
import com.itau.hr.people_management.infrastructure.outbox.publisher.OutboxEventPublisher;

@Component
public class DomainEventPublisherListener {

    private static final Logger log = LoggerFactory.getLogger(DomainEventPublisherListener.class);
    private final OutboxEventPublisher outboxEventPublisher;

    public DomainEventPublisherListener(OutboxEventPublisher outboxEventPublisher) {
        this.outboxEventPublisher = outboxEventPublisher;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDomainEventsAfterCommit(TransactionCompletedEvent event) {
        List<DomainEvent> events = DomainEventsHolder.getAndClearEvents();

        if (events.isEmpty()) {
            return;
        }

        log.debug("Found {} domain events to publish after transaction commit (Thread: {}).", events.size(), Thread.currentThread().getName());

        events.forEach(this::publishEvent);
    }

    private void publishEvent(DomainEvent event) {
        try {
            outboxEventPublisher.publish(event);
            log.info("Domain event '{}' (ID: {}) added to outbox.", event.getEventType(), event.getEventId());
        } catch (Exception e) {
            log.error("Failed to add domain event '{}' (ID: {}) to outbox. Error: {}", event.getEventType(), event.getEventId(), e.getMessage());
        }
    }
}

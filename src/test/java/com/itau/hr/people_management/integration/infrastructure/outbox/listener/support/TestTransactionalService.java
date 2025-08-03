package com.itau.hr.people_management.integration.infrastructure.outbox.listener.support;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.itau.hr.people_management.domain.shared.event.DomainEvent;
import com.itau.hr.people_management.infrastructure.outbox.holder.DomainEventsHolder;

@Component
public class TestTransactionalService {
    @Transactional(propagation = Propagation.REQUIRED)
    public void doSomethingAndAddEvent(DomainEvent event) {
        // Adicionar evento ao holder. Se for null, o catch no listener vai lidar
        if (event != null) {
            DomainEventsHolder.addEvent(event);
        }
        // Não é preciso publicar TransactionCompletedEvent, o Spring faz isso.
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void doSomethingAndAddMultipleEvents(DomainEvent... events) {
        for (DomainEvent event : events) {
            if (event != null) {
                DomainEventsHolder.addEvent(event);
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void doSomethingWithoutAddingEvents() {
        // Transação comita sem eventos no holder
    }
}
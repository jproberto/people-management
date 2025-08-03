package com.itau.hr.people_management.domain.shared.event;

import java.time.Instant;
import java.util.UUID;

import com.itau.hr.people_management.domain.employee.enumeration.EventType;

public interface DomainEvent {
    UUID getEventId();
    Instant getOccurredOn();
    EventType getEventType();
}

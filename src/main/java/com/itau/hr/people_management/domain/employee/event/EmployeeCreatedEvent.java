package com.itau.hr.people_management.domain.employee.event;

import java.time.Instant;
import java.util.UUID;

import com.itau.hr.people_management.domain.employee.enumeration.EventType;
import com.itau.hr.people_management.domain.shared.event.DomainEvent;

import lombok.Getter;

public record EmployeeCreatedEvent(
    @Getter UUID eventId,
    @Getter Instant occurredOn,
    @Getter EventType eventType,
    @Getter UUID employeeId,
    @Getter String employeeName,
    @Getter String employeeEmail
) implements DomainEvent {
    public EmployeeCreatedEvent(UUID employeeId, String employeeName, String employeeEmail) {
        this(UUID.randomUUID(), Instant.now(), EventType.EMPLOYEE_CREATED_EVENT, employeeId, employeeName, employeeEmail);
    }
}
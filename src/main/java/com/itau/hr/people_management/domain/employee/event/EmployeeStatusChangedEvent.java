package com.itau.hr.people_management.domain.employee.event;

import java.time.Instant;
import java.util.UUID;

import com.itau.hr.people_management.domain.employee.enumeration.EmployeeStatus;
import com.itau.hr.people_management.domain.employee.enumeration.EventType;
import com.itau.hr.people_management.domain.shared.event.DomainEvent;

import lombok.Getter;

public record EmployeeStatusChangedEvent(
    @Getter UUID eventId,
    @Getter Instant occurredOn,
    @Getter EventType eventType,
    @Getter UUID employeeId,
    @Getter EmployeeStatus oldStatus,
    @Getter EmployeeStatus newStatus
) implements DomainEvent {
    public EmployeeStatusChangedEvent(UUID employeeId, EmployeeStatus oldStatus, EmployeeStatus newStatus) {
        this(UUID.randomUUID(), Instant.now(), EventType.EMPLOYEE_STATUS_CHANGED_EVENT, employeeId, oldStatus, newStatus);
    }
}

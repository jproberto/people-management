package com.itau.hr.people_management.domain.employee.history;

import java.time.Instant;
import java.util.UUID;

import com.itau.hr.people_management.domain.employee.enumeration.EventType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeEvent {
    private UUID id;
    private UUID employeeId;
    private EventType eventType;
    private String description;
    private Instant occurredOn;
    private String eventData;
}
package com.itau.hr.people_management.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import com.itau.hr.people_management.domain.employee.enumeration.EventType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "employee_events_history") 
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeEventJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID employeeId; 

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private EventType eventType;

    @Column(nullable = false)
    private Instant occurredOn; 

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String eventData;

}
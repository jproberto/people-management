package com.itau.hr.people_management.infrastructure.outbox.entity;

import java.time.Instant;
import java.util.UUID;

import com.itau.hr.people_management.infrastructure.outbox.enumeration.OutboxMessageStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "outbox_messages")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxMessage {
    @Id
    private UUID id;

    @Column(nullable = false)
    private Instant occurredOn;

    @Column(nullable = false)
    private String aggregateType; 

    @Column(nullable = false)
    private UUID aggregateId; 

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    @Enumerated(jakarta.persistence.EnumType.STRING)
    private OutboxMessageStatus status;

    private Instant processedAt;

    @Column(nullable = false)
    private int retryAttempts;

    private Instant nextAttemptAt;
}

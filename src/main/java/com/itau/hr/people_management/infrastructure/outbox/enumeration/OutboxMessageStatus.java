package com.itau.hr.people_management.infrastructure.outbox.enumeration;

public enum OutboxMessageStatus {
    PENDING,
    SENT,
    FAILED,
    DEAD_LETTER;
}

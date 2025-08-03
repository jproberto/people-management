package com.itau.hr.people_management.infrastructure.outbox.exception;

public class OutboxEventSerializationException extends RuntimeException {
    public OutboxEventSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
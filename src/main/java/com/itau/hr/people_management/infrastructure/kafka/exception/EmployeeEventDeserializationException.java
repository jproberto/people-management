package com.itau.hr.people_management.infrastructure.kafka.exception;

public class EmployeeEventDeserializationException extends RuntimeException {
    public EmployeeEventDeserializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
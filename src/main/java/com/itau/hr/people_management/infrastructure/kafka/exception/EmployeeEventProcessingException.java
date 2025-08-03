package com.itau.hr.people_management.infrastructure.kafka.exception;

public class EmployeeEventProcessingException extends RuntimeException {
    public EmployeeEventProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
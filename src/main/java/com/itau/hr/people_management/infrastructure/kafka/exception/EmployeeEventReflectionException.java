package com.itau.hr.people_management.infrastructure.kafka.exception;

public class EmployeeEventReflectionException extends RuntimeException {
    public EmployeeEventReflectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
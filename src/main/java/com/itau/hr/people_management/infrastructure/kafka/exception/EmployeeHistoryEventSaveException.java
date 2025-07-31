package com.itau.hr.people_management.infrastructure.kafka.exception;

public class EmployeeHistoryEventSaveException extends RuntimeException {
    public EmployeeHistoryEventSaveException(String message, Throwable cause) {
        super(message, cause);
    }
}
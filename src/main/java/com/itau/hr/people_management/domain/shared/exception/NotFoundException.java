package com.itau.hr.people_management.domain.shared.exception;

public class NotFoundException extends BusinessException {
    public NotFoundException(String messageKey, Object... args) {
        super(messageKey, args);
    }
}

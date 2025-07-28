package com.itau.hr.people_management.domain.shared.exception;

public class ConflictException  extends BusinessException {
    public ConflictException(String messageKey, Object... args) {
        super(messageKey, args);
    }
}

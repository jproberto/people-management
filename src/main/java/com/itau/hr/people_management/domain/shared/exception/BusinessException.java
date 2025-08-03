package com.itau.hr.people_management.domain.shared.exception;

public class BusinessException extends RuntimeException {
    private final String messageKey;
    private final transient Object[] args;

    public BusinessException(String messageKey, Object... args) {
        super();
        this.messageKey = messageKey;
        this.args = args;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public Object[] getArgs() {
        return args;
    }
}

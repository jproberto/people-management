package com.itau.hr.people_management.domain.shared.message;

public interface DomainMessageSource {
    String getMessage(String key, Object... args);
}

package com.itau.hr.people_management.infrastructure.shared;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import com.itau.hr.people_management.domain.shared.DomainMessageSource;

@Component
public class SpringDomainMessageSource implements DomainMessageSource {
    private final MessageSource messageSource;

    public SpringDomainMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String getMessage(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}

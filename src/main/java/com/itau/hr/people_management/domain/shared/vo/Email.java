package com.itau.hr.people_management.domain.shared.vo;

import com.itau.hr.people_management.domain.shared.message.DomainMessageSource;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@ToString
public class Email {
    private static DomainMessageSource messageSource;
    public static void setMessageSource(DomainMessageSource ms) {
        Email.messageSource = ms;
    }
    
    private final String address;

    public static Email create(String address) {
        validateAddress(address);
        return new Email(address);
    }

    private static void validateAddress(String address) {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.email.address.blank"));
        }
        if (address.length() < 6 || address.length() > 100) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.email.address.length", 6, 100));
        }
    
        String emailRegex = "^[a-zA-Z0-9](?:[a-zA-Z0-9._+-]*[a-zA-Z0-9])?@[a-zA-Z0-9](?:[a-zA-Z0-9.-]*[a-zA-Z0-9])?\\.[a-zA-Z]{2,}$";
        if (!address.matches(emailRegex)) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.email.address.invalid"));
        }
    }
}

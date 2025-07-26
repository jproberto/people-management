package com.itau.hr.people_management.domain.position;

import java.util.UUID;

import com.itau.hr.people_management.domain.shared.DomainMessageSource;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "name"})
public class PositionLevel {
    private static DomainMessageSource messageSource;
    public static void setMessageSource(DomainMessageSource ms) {
        PositionLevel.messageSource = ms;
    }
    
    private final UUID id;
    private final String name;

    public static PositionLevel create(UUID id, String name) {
        validateId(id);
        validateName(name);
        return new PositionLevel(id, name);
    }

    private static void validateId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.positionlevel.id.null"));
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.positionlevel.name.blank"));
        }
    }
}

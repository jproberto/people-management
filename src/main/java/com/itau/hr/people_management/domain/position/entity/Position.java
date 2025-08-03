package com.itau.hr.people_management.domain.position.entity;

import java.util.UUID;

import com.itau.hr.people_management.domain.position.enumeration.PositionLevel;
import com.itau.hr.people_management.domain.shared.message.DomainMessageSource;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "title"})
public class Position {
    private static DomainMessageSource messageSource;
    public static void setMessageSource(DomainMessageSource ms) {
        Position.messageSource = ms;
    }

    private final UUID id;
    private final String title;
    private final PositionLevel positionLevel;

    public static Position create(UUID id, String title, PositionLevel positionLevel) {
        validateId(id);
        validateTitle(title);
        validatePositionLevel(positionLevel);
        return new Position(id, title, positionLevel);
    }

    private static void validateId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.position.id.null"));
        }
    }

    private static void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.position.title.blank"));
        }
        if (title.length() < 2) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.position.title.lenght", 2));
        }
    }

    private static void validatePositionLevel(PositionLevel positionLevel) {
        if (positionLevel == null) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.position.positionlevel.null"));
        }
    }
}

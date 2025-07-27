package com.itau.hr.people_management.domain.position;

import com.itau.hr.people_management.domain.shared.DomainMessageSource;

public enum PositionLevel {
    JUNIOR("Júnior"),
    PLENO("Pleno"),
    SENIOR("Sênior");

    private final String displayName;

    PositionLevel(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static PositionLevel fromString(String text, DomainMessageSource messageSource) {
        for (PositionLevel level : PositionLevel.values()) {
            if (level.name().equalsIgnoreCase(text) || level.getDisplayName().equalsIgnoreCase(text)) {
                return level;
            }
        }
        throw new IllegalArgumentException(messageSource.getMessage("validation.positionlevel.invalid", text));
    }
}

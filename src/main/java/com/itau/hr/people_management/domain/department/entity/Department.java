package com.itau.hr.people_management.domain.department.entity;

import java.util.UUID;

import com.itau.hr.people_management.domain.shared.message.DomainMessageSource;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "name"})
public class Department {
    private static DomainMessageSource messageSource;
    public static void setMessageSource(DomainMessageSource ms) {
        Department.messageSource = ms;
    }

    private final UUID id;
    private final String name;
    private final String costCenterCode;

    public static Department create(UUID id, String name, String costCenterCode) {
        validateId(id);
        validateName(name);
        validateCostCenterCode(costCenterCode);

        return new Department(id, name.trim(), costCenterCode.trim());
    }

    private static void validateId(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.department.id.null"));
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.department.name.blank"));
        }
        if (name.length() < 2) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.department.name.length", 2));
        }
    }

    private static void validateCostCenterCode(String costCenterCode) {
        if (costCenterCode == null || costCenterCode.isBlank()) {
            throw new IllegalArgumentException(messageSource.getMessage("validation.department.costcentercode.blank"));
        }
    }
}

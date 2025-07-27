package com.itau.hr.people_management.domain.employee;

import java.util.Optional;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EmployeeSearchCriteria {
    private String name;
    private String emailAddress;
    private EmployeeStatus status;
    private UUID departmentId;
    private String departmentName;
    private UUID positionId;
    private String positionTitle;

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<String> getEmailAddress() {
        return Optional.ofNullable(emailAddress);
    }

    public Optional<EmployeeStatus> getStatus() {
        return Optional.ofNullable(status);
    }

    public Optional<UUID> getDepartmentId() {
        return Optional.ofNullable(departmentId);
    }

    public Optional<String> getDepartmentName() {
        return Optional.ofNullable(departmentName);
    }

    public Optional<UUID> getPositionId() {
        return Optional.ofNullable(positionId);
    }

    public Optional<String> getPositionTitle() {
        return Optional.ofNullable(positionTitle);
    }
}

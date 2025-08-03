package com.itau.hr.people_management.domain.employee.criteria;

import java.util.Optional;
import java.util.UUID;

import com.itau.hr.people_management.domain.employee.enumeration.EmployeeStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class EmployeeSearchCriteria {
    private String name;
    private String emailAddress;
    private EmployeeStatus employeeStatus;
    private UUID departmentId;
    private String departmentName;
    private UUID positionId;
    private String positionTitle;
    private String positionLevel;

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<String> getEmailAddress() {
        return Optional.ofNullable(emailAddress);
    }

    public Optional<EmployeeStatus> getEmployeeStatus() {
        return Optional.ofNullable(employeeStatus);
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

    public Optional<String> getPositionLevel() {
        return Optional.ofNullable(positionLevel);
    }
}

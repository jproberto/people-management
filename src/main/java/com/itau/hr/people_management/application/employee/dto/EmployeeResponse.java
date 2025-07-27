package com.itau.hr.people_management.application.employee.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.itau.hr.people_management.domain.employee.Employee;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponse {
    private UUID id;
    private String name;
    private String email;
    private LocalDate hireDate;
    private String employeeStatus;
    private UUID departmentId;
    private String departmentName;
    private UUID positionId;
    private String positionName;
    private String positionLevelName;

    public EmployeeResponse(Employee employee) {
        if (employee == null) {
            return;
        }

        this.id = employee.getId();
        this.name = employee.getName();
        this.email = employee.getEmail() != null ? employee.getEmail().getAddress() : null;
        this.hireDate = employee.getHireDate();
        this.employeeStatus = employee.getStatus() != null ? employee.getStatus().name() : null;

        if (employee.getDepartment() != null) {
            this.departmentId = employee.getDepartment().getId();
            this.departmentName = employee.getDepartment().getName();
        }

        if (employee.getPosition() != null) {
            this.positionId = employee.getPosition().getId();
            this.positionName = employee.getPosition().getTitle();
            if (employee.getPosition().getPositionLevel() != null) {
                this.positionLevelName = employee.getPosition().getPositionLevel().getDisplayName();
            }
        }
    }
}

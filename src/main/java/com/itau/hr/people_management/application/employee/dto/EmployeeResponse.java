package com.itau.hr.people_management.application.employee.dto;

import java.util.UUID;

import com.itau.hr.people_management.application.department.dto.DepartmentResponse;
import com.itau.hr.people_management.application.position.dto.PositionResponse;
import com.itau.hr.people_management.domain.employee.entity.Employee;

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
    private String employeeStatus;
    private DepartmentResponse department;
    private PositionResponse position;

    public EmployeeResponse(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }

        this.id = employee.getId();
        this.name = employee.getName();
        this.email = employee.getEmail() != null ? employee.getEmail().getAddress() : null;
        this.employeeStatus = employee.getStatus() != null ? employee.getStatus().name() : null;
        this.department = employee.getDepartment() != null ? new DepartmentResponse(employee.getDepartment()) : null;
        this.position = employee.getPosition() != null ? new PositionResponse(employee.getPosition()) : null;
    }
}

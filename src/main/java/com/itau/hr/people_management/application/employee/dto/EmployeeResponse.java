package com.itau.hr.people_management.application.employee.dto;

import java.time.LocalDate;
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
    private LocalDate hireDate;
    private String employeeStatus;
    private DepartmentResponse department;
    private PositionResponse position;

    public EmployeeResponse(Employee employee) {
        if (employee == null) {
            return;
        }

        this.id = employee.getId();
        this.name = employee.getName();
        this.email = employee.getEmail() != null ? employee.getEmail().getAddress() : null;
        this.hireDate = employee.getHireDate();
        this.employeeStatus = employee.getStatus() != null ? employee.getStatus().name() : null;
        this.department = new DepartmentResponse(employee.getDepartment());
        this.position = new PositionResponse(employee.getPosition());
    }
}

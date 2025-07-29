package com.itau.hr.people_management.application.department.dto;

import java.util.UUID;

import com.itau.hr.people_management.domain.department.entity.Department;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {
    private UUID id;
    private String name;
    private String costCenterCode;

    public DepartmentResponse(Department department) {
        this.id = department.getId();
        this.name = department.getName();
        this.costCenterCode = department.getCostCenterCode();
    }
}

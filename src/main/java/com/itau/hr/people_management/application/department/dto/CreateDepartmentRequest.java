package com.itau.hr.people_management.application.department.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateDepartmentRequest {

    @NotBlank(message = "{validation.department.name.blank}")
    @Size(min = 2, max = 100, message = "{validation.department.name.length}")
    private String name;

    @NotBlank(message = "{validation.department.costcenter.blank}")
    @Size(min = 2, max = 50, message = "{validation.department.costcenter.length}")
    private String costCenterCode; 
}

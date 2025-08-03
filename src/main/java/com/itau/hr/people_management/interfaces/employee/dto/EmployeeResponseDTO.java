package com.itau.hr.people_management.interfaces.employee.dto;

import java.util.UUID;

import com.itau.hr.people_management.domain.employee.enumeration.EmployeeStatus;
import com.itau.hr.people_management.interfaces.department.dto.DepartmentResponseDTO;
import com.itau.hr.people_management.interfaces.position.dto.PositionResponseDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Response DTO for Employee operations")
public class EmployeeResponseDTO {

    @Schema(description = "Unique identifier of the employee", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    private UUID id;

    @Schema(description = "Full name of the employee", example = "João Paulo")
    private String name;

    @Schema(description = "Email address of the employee", example = "joaopaulo@email.com")
    private String email;

    @Schema(description = "Current status of the employee", example = "ACTIVE")
    private EmployeeStatus employeeStatus;

    @Schema(description = "Department the employee belongs to")
    private DepartmentResponseDTO department; 

    @Schema(description = "Position the employee holds")
    private PositionResponseDTO position;
}

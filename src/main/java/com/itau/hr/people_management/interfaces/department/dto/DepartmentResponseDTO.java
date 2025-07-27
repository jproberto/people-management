package com.itau.hr.people_management.interfaces.department.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Response DTO for Department operations")
public class DepartmentResponseDTO {

    @Schema(description = "Unique identifier of the department", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef")
    private UUID id;

    @Schema(description = "Name of the department", example = "Human Resources")
    private String name;

    @Schema(description = "Unique code for the cost center", example = "CC1001")
    private String costCenterCode;
}
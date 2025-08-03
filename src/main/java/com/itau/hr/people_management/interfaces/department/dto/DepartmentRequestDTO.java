package com.itau.hr.people_management.interfaces.department.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request DTO for creating or updating a Department")
public class DepartmentRequestDTO {
    @NotBlank(message = "Department name cannot be blank")
    @Size(max = 100, message = "Department name cannot exceed 100 characters")
    @Schema(description = "Name of the department", example = "Human Resources", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "Cost center code cannot be blank")
    @Size(max = 50, message = "Cost center code cannot exceed 50 characters")
    @Schema(description = "Unique code for the cost center", example = "CC1001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String costCenterCode;
}

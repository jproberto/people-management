package com.itau.hr.people_management.interfaces.employee.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@Schema(description = "Request DTO for creating a new Employee")
public class EmployeeRequestDTO {

    @NotBlank(message = "Employee name cannot be blank")
    @Size(max = 255, message = "Employee name cannot exceed 255 characters")
    @Schema(description = "Full name of the employee", example = "Joao Paulo", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "Employee email cannot be blank")
    @Email(message = "Employee email must be a valid email address")
    @Size(max = 255, message = "Employee email cannot exceed 255 characters")
    @Schema(description = "Email address of the employee", example = "joaopaulo@email.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotNull(message = "Hire date cannot be null")
    @PastOrPresent(message = "Hire date cannot be in the future")
    @Schema(description = "Date when the employee was hired (YYYY-MM-DD)", example = "2025-08-10", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate hireDate;

    @NotNull(message = "Department ID cannot be null")
    @Schema(description = "Unique identifier of the department the employee belongs to", example = "a1b2c3d4-e5f6-7890-1234-567890abcdef", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID departmentId;

    @NotNull(message = "Position ID cannot be null")
    @Schema(description = "Unique identifier of the position the employee holds", example = "f5e4d3c2-b1a0-9876-5432-10fedcba9876", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID positionId;
}

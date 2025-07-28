package com.itau.hr.people_management.interfaces.employee.dto;

import java.util.UUID;

import com.itau.hr.people_management.domain.employee.EmployeeStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "Request DTO for searching employees by various criteria")
public class EmployeeSearchRequestDTO {
    @Schema(description = "Name of the employee", example = "Joao Paulo")
    private String name;

    @Schema(description = "Email address of the employee", example = "joao.paulo@example.com")
    private String emailAddress;

    @Schema(description = "Status of the employee", example = "ACTIVE")
    private EmployeeStatus status;

    @Schema(description = "ID of the department", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID departmentId;

    @Schema(description = "Name of the department", example = "Technology")
    private String department;

    @Schema(description = "ID of the position", example = "123e4567-e89b-12d3-a456-426614174001")
    private UUID positionId;

    @Schema(description = "Title of the position", example = "Software Engineer")
    private String position;

    @Schema(description = "Level of the position", example = "Senior")
    private String positionLevel;
}

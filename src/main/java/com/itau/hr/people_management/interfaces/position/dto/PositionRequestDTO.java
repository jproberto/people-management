package com.itau.hr.people_management.interfaces.position.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request DTO for creating or updating a Position")
public class PositionRequestDTO {

    @NotBlank(message = "Position title cannot be blank")
    @Size(max = 100, message = "Position title cannot exceed 100 characters")
    @Schema(description = "Title of the position", example = "Software Engineer", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotNull(message = "Position level cannot be null")
    @Schema(description = "Level of the position (e.g., JUNIOR, PLENO, SENIOR)", example = "PLENO", requiredMode = Schema.RequiredMode.REQUIRED)
    private String positionLevel;
}

package com.itau.hr.people_management.interfaces.position.dto;

import java.util.UUID;

import com.itau.hr.people_management.domain.position.PositionLevel;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Response DTO for Position operations")
public class PositionResponseDTO {

    @Schema(description = "Unique identifier of the position", example = "f5e4d3c2-b1a0-9876-5432-10fedcba9876")
    private UUID id;

    @Schema(description = "Title of the position", example = "Software Engineer")
    private String title;

    @Schema(description = "Level of the position", example = "PLENO")
    private PositionLevel positionLevel;
}
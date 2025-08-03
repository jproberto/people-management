package com.itau.hr.people_management.interfaces.shared.dto;

import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard error response format for API exceptions")
public class ApiErrorResponse {

    @Schema(description = "Timestamp when the error occurred", example = "2024-07-26T10:30:00Z")
    private Instant timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private Integer status;

    @Schema(description = "Descriptive error message", example = "Validation failed for request")
    private String error;

    @Schema(description = "List of specific error messages, often for validation errors", example = "[\"name: must not be blank\", \"email: must be a valid email\"]")
    private List<String> messages;

    @Schema(description = "The path of the request that caused the error", example = "/api/v1/employees")
    private String path;

}

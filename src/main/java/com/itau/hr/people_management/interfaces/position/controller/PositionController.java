package com.itau.hr.people_management.interfaces.position.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itau.hr.people_management.application.position.dto.CreatePositionRequest;
import com.itau.hr.people_management.application.position.dto.PositionResponse;
import com.itau.hr.people_management.application.position.usecase.CreatePositionUseCase;
import com.itau.hr.people_management.interfaces.position.dto.PositionRequestDTO;
import com.itau.hr.people_management.interfaces.position.dto.PositionResponseDTO;
import com.itau.hr.people_management.interfaces.position.mapper.PositionControllerMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/positions") 
@Tag(name = "Position", description = "Operations related to Positions") 
public class PositionController {

    private final CreatePositionUseCase createPositionUseCase;
    private final PositionControllerMapper positionControllerMapper;

    public PositionController(CreatePositionUseCase createPositionUseCase, PositionControllerMapper positionControllerMapper) {
        this.createPositionUseCase = createPositionUseCase;
        this.positionControllerMapper = positionControllerMapper;
    }

    @Operation(summary = "Create a new position", description = "Creates a new position with the provided details")
    @ApiResponse(responseCode = "201", description = "Position created successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = PositionResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid position data supplied",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "409", description = "Position with given title already exists",
            content = @Content(mediaType = "application/json"))
    @PostMapping 
    public ResponseEntity<PositionResponseDTO> createPosition(@Valid @RequestBody PositionRequestDTO requestDTO) {
        CreatePositionRequest applicationRequest = positionControllerMapper.toApplicationRequest(requestDTO);
        PositionResponse applicationResponse = createPositionUseCase.execute(applicationRequest);
        PositionResponseDTO responseDTO = positionControllerMapper.toPositionResponseDTO(applicationResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
}

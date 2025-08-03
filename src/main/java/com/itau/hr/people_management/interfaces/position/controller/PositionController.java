package com.itau.hr.people_management.interfaces.position.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itau.hr.people_management.application.position.dto.CreatePositionRequest;
import com.itau.hr.people_management.application.position.dto.PositionResponse;
import com.itau.hr.people_management.application.position.usecase.CreatePositionUseCase;
import com.itau.hr.people_management.application.position.usecase.GetPositionUseCase;
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
    private final GetPositionUseCase getPositionUseCase;
    private final PositionControllerMapper positionControllerMapper;

    public PositionController(CreatePositionUseCase createPositionUseCase, GetPositionUseCase getPositionUseCase, PositionControllerMapper positionControllerMapper) {
        this.createPositionUseCase = createPositionUseCase;
        this.getPositionUseCase = getPositionUseCase;   
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
    @Operation(summary = "Get all positions", description = "Retrieves a list of all positions")
    @ApiResponse(responseCode = "200", description = "List of positions retrieved successfully",    
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = PositionResponseDTO.class)))
    @GetMapping
    public ResponseEntity<List<PositionResponseDTO>> getAllPositions() {
        List<PositionResponse> applicationResponses = getPositionUseCase.getAll();
        List<PositionResponseDTO> responseDTOs = positionControllerMapper.toPositionResponseDTOList(applicationResponses);
        return ResponseEntity.ok(responseDTOs);
    }
}

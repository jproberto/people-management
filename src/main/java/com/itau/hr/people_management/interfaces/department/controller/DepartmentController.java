package com.itau.hr.people_management.interfaces.department.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itau.hr.people_management.application.department.dto.CreateDepartmentRequest;
import com.itau.hr.people_management.application.department.dto.DepartmentResponse;
import com.itau.hr.people_management.application.department.usecase.CreateDepartmentUseCase;
import com.itau.hr.people_management.interfaces.department.dto.DepartmentRequestDTO;
import com.itau.hr.people_management.interfaces.department.dto.DepartmentResponseDTO;
import com.itau.hr.people_management.interfaces.department.mapper.DepartmentControllerMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/departments")
@Tag(name = "Department", description = "Operations related to Departments")
public class DepartmentController {
    private final CreateDepartmentUseCase createDepartmentUseCase;
    private final DepartmentControllerMapper departmentControllerMapper; 

    public DepartmentController(CreateDepartmentUseCase createDepartmentUseCase, DepartmentControllerMapper departmentControllerMapper) {
        this.createDepartmentUseCase = createDepartmentUseCase;
        this.departmentControllerMapper = departmentControllerMapper;
    }

    @Operation(summary = "Create a new department", description = "Creates a new department with the provided details")
    @ApiResponse(responseCode = "201", description = "Department created successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = DepartmentResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid department data supplied",
            content = @Content(mediaType = "application/json")) // Podemos adicionar um schema de erro aqui no futuro
    @ApiResponse(responseCode = "409", description = "Department with given cost center code already exists",
            content = @Content(mediaType = "application/json"))
    @PostMapping // Mapeia requisições POST para /api/v1/departments
    public ResponseEntity<DepartmentResponseDTO> createDepartment(@Valid @RequestBody DepartmentRequestDTO requestDTO) {
        // Mapeia o DTO de Requisição da API para o DTO de Requisição da Camada de Aplicação
        CreateDepartmentRequest applicationRequest = departmentControllerMapper.toApplicationRequest(requestDTO);

        // Executa o Use Case
        DepartmentResponse applicationResponse = createDepartmentUseCase.execute(applicationRequest);

        // Mapeia o DTO de Resposta da Camada de Aplicação para o DTO de Resposta da API
        DepartmentResponseDTO responseDTO = departmentControllerMapper.toDepartmentResponseDTO(applicationResponse);

        // Retorna a resposta com status 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }
}

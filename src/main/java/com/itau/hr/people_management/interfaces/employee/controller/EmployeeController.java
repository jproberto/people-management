package com.itau.hr.people_management.interfaces.employee.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itau.hr.people_management.application.employee.dto.CreateEmployeeRequest;
import com.itau.hr.people_management.application.employee.dto.EmployeeResponse;
import com.itau.hr.people_management.application.employee.usecase.CreateEmployeeUseCase;
import com.itau.hr.people_management.application.employee.usecase.DeleteEmployeeUseCase;
import com.itau.hr.people_management.application.employee.usecase.GetEmployeeUseCase;
import com.itau.hr.people_management.application.employee.usecase.SearchEmployeeUseCase;
import com.itau.hr.people_management.domain.employee.EmployeeSearchCriteria;
import com.itau.hr.people_management.interfaces.employee.dto.EmployeeRequestDTO;
import com.itau.hr.people_management.interfaces.employee.dto.EmployeeResponseDTO;
import com.itau.hr.people_management.interfaces.employee.dto.EmployeeSearchRequestDTO;
import com.itau.hr.people_management.interfaces.employee.mapper.EmployeeControllerMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/employees")
@Tag(name = "Employee", description = "Operations related to Employees")
public class EmployeeController {
    private final GetEmployeeUseCase getEmployeeUseCase;
    private final CreateEmployeeUseCase createEmployeeUseCase;
    private final DeleteEmployeeUseCase deleteEmployeeUseCase;
    private final SearchEmployeeUseCase getEmployeesByCriteriaUseCase;
    private final EmployeeControllerMapper employeeControllerMapper;

    public EmployeeController(GetEmployeeUseCase getEmployeeUseCase,
                              CreateEmployeeUseCase createEmployeeUseCase,
                              DeleteEmployeeUseCase deleteEmployeeUseCase,
                              SearchEmployeeUseCase getEmployeesByCriteriaUseCase,
                              EmployeeControllerMapper employeeControllerMapper) {
        this.getEmployeeUseCase = getEmployeeUseCase;
        this.createEmployeeUseCase = createEmployeeUseCase;
        this.deleteEmployeeUseCase = deleteEmployeeUseCase;
        this.getEmployeesByCriteriaUseCase = getEmployeesByCriteriaUseCase;
        this.employeeControllerMapper = employeeControllerMapper;
    }

    @Operation(summary = "Get all employees", description = "Retrieves a list of all employees in the system")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of employees",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = EmployeeResponseDTO.class))))
    @GetMapping
    public ResponseEntity<List<EmployeeResponseDTO>> getAllEmployees() {
        List<EmployeeResponse> applicationResponses = getEmployeeUseCase.getAll();
        List<EmployeeResponseDTO> responseDTOs = employeeControllerMapper.toEmployeeResponseDTOList(applicationResponses);
        return ResponseEntity.status(HttpStatus.OK).body(responseDTOs);
    }

    @Operation(summary = "Get employee by ID", description = "Retrieves an employee by their unique identifier")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved employee",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = EmployeeResponseDTO.class)))
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDTO> getEmployee(@PathVariable("id") UUID id) {
        EmployeeResponse applicationResponse = getEmployeeUseCase.getById(id);
        EmployeeResponseDTO responseDTO = employeeControllerMapper.toEmployeeResponseDTO(applicationResponse);
        return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
    }

    @Operation(summary = "Create a new employee", description = "Creates a new employee with the provided details, linking to existing department and position.")
    @ApiResponse(responseCode = "201", description = "Employee created successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = EmployeeResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid employee data supplied (e.g., missing fields, invalid email, future hire date)",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "404", description = "Department or Position not found with the provided IDs",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "409", description = "Employee with given email already exists",
            content = @Content(mediaType = "application/json"))
    @PostMapping
    public ResponseEntity<EmployeeResponseDTO> createEmployee(@Valid @RequestBody EmployeeRequestDTO requestDTO) {
        CreateEmployeeRequest applicationRequest = employeeControllerMapper.toApplicationRequest(requestDTO);
        EmployeeResponse applicationResponse = createEmployeeUseCase.execute(applicationRequest);
        EmployeeResponseDTO responseDTO = employeeControllerMapper.toEmployeeResponseDTO(applicationResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @Operation(summary = "Delete an employee", description = "Deletes an employee by their unique identifier")
    @ApiResponse(responseCode = "204", description = "Employee deleted successfully (No content)")
    @ApiResponse(responseCode = "404", description = "Employee not found with the provided ID",
            content = @Content(mediaType = "application/json"))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable UUID id) {
        deleteEmployeeUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search employees by criteria", description = "Retrieves a list of employees filtered by specified criteria, e.g., department name, position, etc.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved filtered list of employees",
                content = @Content(mediaType = "application/json",
                                array = @ArraySchema(schema = @Schema(implementation = EmployeeResponseDTO.class))))
    @ApiResponse(responseCode = "400", description = "Invalid search parameters supplied",
                content = @Content(mediaType = "application/json"))
    @GetMapping("/search")
    public ResponseEntity<List<EmployeeResponseDTO>> searchEmployees(@Valid EmployeeSearchRequestDTO searchRequestDTO) {
        EmployeeSearchCriteria  employeeSearchCriteria  = employeeControllerMapper.toEmployeeSearchCriteria (searchRequestDTO);
        List<EmployeeResponse> applicationResponses = getEmployeesByCriteriaUseCase.execute(employeeSearchCriteria);
        List<EmployeeResponseDTO> responseDTOs = employeeControllerMapper.toEmployeeResponseDTOList(applicationResponses);
        return ResponseEntity.status(HttpStatus.OK).body(responseDTOs);
    }
}

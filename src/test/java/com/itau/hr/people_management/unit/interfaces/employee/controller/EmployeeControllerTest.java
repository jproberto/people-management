package com.itau.hr.people_management.unit.interfaces.employee.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.itau.hr.people_management.application.employee.dto.ChangeEmployeeStatusRequest;
import com.itau.hr.people_management.application.employee.dto.CreateEmployeeRequest;
import com.itau.hr.people_management.application.employee.dto.EmployeeResponse;
import com.itau.hr.people_management.application.employee.usecase.ChangeEmployeeStatusUseCase;
import com.itau.hr.people_management.application.employee.usecase.CreateEmployeeUseCase;
import com.itau.hr.people_management.application.employee.usecase.DeleteEmployeeUseCase;
import com.itau.hr.people_management.application.employee.usecase.GetEmployeeUseCase;
import com.itau.hr.people_management.application.employee.usecase.ReactivateEmployeeUseCase;
import com.itau.hr.people_management.application.employee.usecase.SearchEmployeeUseCase;
import com.itau.hr.people_management.domain.employee.criteria.EmployeeSearchCriteria;
import com.itau.hr.people_management.domain.employee.enumeration.EmployeeStatus;
import com.itau.hr.people_management.interfaces.employee.controller.EmployeeController;
import com.itau.hr.people_management.interfaces.employee.dto.EmployeeRequestDTO;
import com.itau.hr.people_management.interfaces.employee.dto.EmployeeResponseDTO;
import com.itau.hr.people_management.interfaces.employee.dto.EmployeeSearchRequestDTO;
import com.itau.hr.people_management.interfaces.employee.mapper.EmployeeControllerMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeController Unit Tests")
class EmployeeControllerTest {

    @Mock private GetEmployeeUseCase getEmployeeUseCase;
    @Mock private CreateEmployeeUseCase createEmployeeUseCase;
    @Mock private DeleteEmployeeUseCase deleteEmployeeUseCase;
    @Mock private SearchEmployeeUseCase searchEmployeeUseCase;
    @Mock private ChangeEmployeeStatusUseCase changeEmployeeStatusUseCase;
    @Mock private ReactivateEmployeeUseCase reactivateEmployeeUseCase;
    @Mock private EmployeeControllerMapper employeeControllerMapper;

    @Mock private EmployeeRequestDTO requestDTO;
    @Mock private EmployeeSearchRequestDTO searchRequestDTO;
    @Mock private CreateEmployeeRequest applicationRequest;
    @Mock private EmployeeSearchCriteria searchCriteria;
    @Mock private EmployeeResponse applicationResponse;
    @Mock private EmployeeResponseDTO responseDTO;

    private EmployeeController controller;
    private UUID validId;

    @BeforeEach
    void setUp() {
        controller = new EmployeeController(
            getEmployeeUseCase, createEmployeeUseCase, deleteEmployeeUseCase,
            searchEmployeeUseCase, changeEmployeeStatusUseCase, reactivateEmployeeUseCase,
            employeeControllerMapper
        );
        validId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should get all employees and return 200 OK")
    void shouldGetAllEmployeesAndReturn200Ok() {
        // Arrange
        List<EmployeeResponse> applicationResponses = List.of(applicationResponse);
        List<EmployeeResponseDTO> responseDTOs = List.of(responseDTO);
        when(getEmployeeUseCase.getAll()).thenReturn(applicationResponses);
        when(employeeControllerMapper.toEmployeeResponseDTOList(applicationResponses)).thenReturn(responseDTOs);

        // Act
        ResponseEntity<List<EmployeeResponseDTO>> result = controller.getAllEmployees();

        // Assert
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), is(responseDTOs));
        verify(getEmployeeUseCase).getAll();
        verify(employeeControllerMapper).toEmployeeResponseDTOList(applicationResponses);
    }

    @Test
    @DisplayName("Should get employee by ID and return 200 OK")
    void shouldGetEmployeeByIdAndReturn200Ok() {
        // Arrange
        when(getEmployeeUseCase.getById(validId)).thenReturn(applicationResponse);
        when(employeeControllerMapper.toEmployeeResponseDTO(applicationResponse)).thenReturn(responseDTO);

        // Act
        ResponseEntity<EmployeeResponseDTO> result = controller.getEmployee(validId);

        // Assert
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), is(responseDTO));
        verify(getEmployeeUseCase).getById(validId);
        verify(employeeControllerMapper).toEmployeeResponseDTO(applicationResponse);
    }

    @Test
    @DisplayName("Should create employee and return 201 CREATED")
    void shouldCreateEmployeeAndReturn201Created() {
        // Arrange
        when(employeeControllerMapper.toApplicationRequest(requestDTO)).thenReturn(applicationRequest);
        when(createEmployeeUseCase.execute(applicationRequest)).thenReturn(applicationResponse);
        when(employeeControllerMapper.toEmployeeResponseDTO(applicationResponse)).thenReturn(responseDTO);

        // Act
        ResponseEntity<EmployeeResponseDTO> result = controller.createEmployee(requestDTO);

        // Assert
        assertThat(result.getStatusCode(), is(HttpStatus.CREATED));
        assertThat(result.getBody(), is(responseDTO));
        verify(employeeControllerMapper).toApplicationRequest(requestDTO);
        verify(createEmployeeUseCase).execute(applicationRequest);
        verify(employeeControllerMapper).toEmployeeResponseDTO(applicationResponse);
    }

    @Test
    @DisplayName("Should delete employee and return 204 NO_CONTENT")
    void shouldDeleteEmployeeAndReturn204NoContent() {
        // Act
        ResponseEntity<Void> result = controller.deleteEmployee(validId);

        // Assert
        assertThat(result.getStatusCode(), is(HttpStatus.NO_CONTENT));
        assertThat(result.getBody(), is(nullValue()));
        verify(deleteEmployeeUseCase).execute(validId);
    }

    @Test
    @DisplayName("Should search employees and return 200 OK")
    void shouldSearchEmployeesAndReturn200Ok() {
        // Arrange
        List<EmployeeResponse> applicationResponses = List.of(applicationResponse);
        List<EmployeeResponseDTO> responseDTOs = List.of(responseDTO);
        when(employeeControllerMapper.toEmployeeSearchCriteria(searchRequestDTO)).thenReturn(searchCriteria);
        when(searchEmployeeUseCase.execute(searchCriteria)).thenReturn(applicationResponses);
        when(employeeControllerMapper.toEmployeeResponseDTOList(applicationResponses)).thenReturn(responseDTOs);

        // Act
        ResponseEntity<List<EmployeeResponseDTO>> result = controller.searchEmployees(searchRequestDTO);

        // Assert
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), is(responseDTOs));
        verify(employeeControllerMapper).toEmployeeSearchCriteria(searchRequestDTO);
        verify(searchEmployeeUseCase).execute(searchCriteria);
        verify(employeeControllerMapper).toEmployeeResponseDTOList(applicationResponses);
    }

    @Test
    @DisplayName("Should change employee status and return 204 NO_CONTENT")
    void shouldChangeEmployeeStatusAndReturn204NoContent() {
        // Arrange
        ChangeEmployeeStatusRequest statusRequest = new ChangeEmployeeStatusRequest(EmployeeStatus.TERMINATED);

        // Act
        ResponseEntity<Void> result = controller.changeEmployeeStatus(validId, statusRequest);

        // Assert
        assertThat(result.getStatusCode(), is(HttpStatus.NO_CONTENT));
        assertThat(result.getBody(), is(nullValue()));
        verify(changeEmployeeStatusUseCase).execute(validId, EmployeeStatus.TERMINATED);
    }

    @Test
    @DisplayName("Should reactivate employee and return 204 NO_CONTENT")
    void shouldReactivateEmployeeAndReturn204NoContent() {
        // Act
        ResponseEntity<Void> result = controller.reactivateEmployee(validId);

        // Assert
        assertThat(result.getStatusCode(), is(HttpStatus.NO_CONTENT));
        assertThat(result.getBody(), is(nullValue()));
        verify(reactivateEmployeeUseCase).execute(validId);
    }

    @Test
    @DisplayName("Should handle empty lists gracefully")
    void shouldHandleEmptyListsGracefully() {
        // Arrange
        List<EmployeeResponse> emptyApplicationResponses = List.of();
        List<EmployeeResponseDTO> emptyResponseDTOs = List.of();
        when(getEmployeeUseCase.getAll()).thenReturn(emptyApplicationResponses);
        when(employeeControllerMapper.toEmployeeResponseDTOList(emptyApplicationResponses)).thenReturn(emptyResponseDTOs);

        // Act
        ResponseEntity<List<EmployeeResponseDTO>> result = controller.getAllEmployees();

        // Assert
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), is(empty()));
    }

    @Test
    @DisplayName("Should propagate exceptions from use cases")
    void shouldPropagateExceptionsFromUseCases() {
        // Arrange
        RuntimeException useCaseException = new RuntimeException("Use case error");
        when(getEmployeeUseCase.getById(validId)).thenThrow(useCaseException);

        // Act & Assert
        RuntimeException exception = org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () ->
            controller.getEmployee(validId)
        );

        assertThat(exception.getMessage(), is("Use case error"));
        verify(getEmployeeUseCase).getById(validId);
        verifyNoInteractions(employeeControllerMapper);
    }
}
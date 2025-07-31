package com.itau.hr.people_management.interfaces.department.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.itau.hr.people_management.application.department.dto.CreateDepartmentRequest;
import com.itau.hr.people_management.application.department.dto.DepartmentResponse;
import com.itau.hr.people_management.application.department.usecase.CreateDepartmentUseCase;
import com.itau.hr.people_management.application.department.usecase.GetDepartmentUseCase;
import com.itau.hr.people_management.interfaces.department.dto.DepartmentRequestDTO;
import com.itau.hr.people_management.interfaces.department.dto.DepartmentResponseDTO;
import com.itau.hr.people_management.interfaces.department.mapper.DepartmentControllerMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("DepartmentController Unit Tests")
class DepartmentControllerTest {

    @Mock
    private CreateDepartmentUseCase createDepartmentUseCase;

    @Mock
    private GetDepartmentUseCase getDepartmentUseCase;

    @Mock
    private DepartmentControllerMapper departmentControllerMapper;

    @Mock
    private DepartmentRequestDTO requestDTO;

    @Mock
    private CreateDepartmentRequest applicationRequest;

    @Mock
    private DepartmentResponse applicationResponse;

    @Mock
    private DepartmentResponseDTO responseDTO;

    private DepartmentController controller;

    @BeforeEach
    void setUp() {
        controller = new DepartmentController(createDepartmentUseCase, getDepartmentUseCase, departmentControllerMapper);
    }

    @Test
    @DisplayName("Should create department and return 201 CREATED")
    void shouldCreateDepartmentAndReturn201Created() {
        // Arrange
        when(departmentControllerMapper.toApplicationRequest(requestDTO)).thenReturn(applicationRequest);
        when(createDepartmentUseCase.execute(applicationRequest)).thenReturn(applicationResponse);
        when(departmentControllerMapper.toDepartmentResponseDTO(applicationResponse)).thenReturn(responseDTO);

        // Act
        ResponseEntity<DepartmentResponseDTO> result = controller.createDepartment(requestDTO);

        // Assert
        assertThat(result.getStatusCode(), is(HttpStatus.CREATED));
        assertThat(result.getBody(), is(responseDTO));
        
        verify(departmentControllerMapper).toApplicationRequest(requestDTO);
        verify(createDepartmentUseCase).execute(applicationRequest);
        verify(departmentControllerMapper).toDepartmentResponseDTO(applicationResponse);
    }

    @Test
    @DisplayName("Should get all departments and return 200 OK")
    void shouldGetAllDepartmentsAndReturn200Ok() {
        // Arrange
        List<DepartmentResponse> applicationResponses = List.of(applicationResponse);
        List<DepartmentResponseDTO> responseDTOs = List.of(responseDTO);
        
        when(getDepartmentUseCase.getAll()).thenReturn(applicationResponses);
        when(departmentControllerMapper.toDepartmentResponseDTOList(applicationResponses)).thenReturn(responseDTOs);

        // Act
        ResponseEntity<List<DepartmentResponseDTO>> result = controller.getAllDepartments();

        // Assert
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), is(responseDTOs));
        assertThat(result.getBody(), hasSize(1));
        
        verify(getDepartmentUseCase).getAll();
        verify(departmentControllerMapper).toDepartmentResponseDTOList(applicationResponses);
    }

    @Test
    @DisplayName("Should handle empty list for get all departments")
    void shouldHandleEmptyListForGetAllDepartments() {
        // Arrange
        List<DepartmentResponse> emptyApplicationResponses = List.of();
        List<DepartmentResponseDTO> emptyResponseDTOs = List.of();
        
        when(getDepartmentUseCase.getAll()).thenReturn(emptyApplicationResponses);
        when(departmentControllerMapper.toDepartmentResponseDTOList(emptyApplicationResponses)).thenReturn(emptyResponseDTOs);

        // Act
        ResponseEntity<List<DepartmentResponseDTO>> result = controller.getAllDepartments();

        // Assert
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), is(emptyResponseDTOs));
        assertThat(result.getBody(), is(empty()));
    }

    @Test
    @DisplayName("Should follow correct workflow for create department")
    void shouldFollowCorrectWorkflowForCreateDepartment() {
        // Arrange
        when(departmentControllerMapper.toApplicationRequest(requestDTO)).thenReturn(applicationRequest);
        when(createDepartmentUseCase.execute(applicationRequest)).thenReturn(applicationResponse);
        when(departmentControllerMapper.toDepartmentResponseDTO(applicationResponse)).thenReturn(responseDTO);

        // Act
        controller.createDepartment(requestDTO);

        // Assert - Verify execution order
        verify(departmentControllerMapper).toApplicationRequest(requestDTO);
        verify(createDepartmentUseCase).execute(applicationRequest);
        verify(departmentControllerMapper).toDepartmentResponseDTO(applicationResponse);
    }

    @Test
    @DisplayName("Should delegate exception handling to framework")
    void shouldDelegateExceptionHandlingToFramework() {
        // Arrange
        RuntimeException useCaseException = new RuntimeException("Use case error");
        when(departmentControllerMapper.toApplicationRequest(requestDTO)).thenReturn(applicationRequest);
        when(createDepartmentUseCase.execute(applicationRequest)).thenThrow(useCaseException);

        // Act & Assert
        RuntimeException exception = org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () ->
            controller.createDepartment(requestDTO)
        );

        assertThat(exception.getMessage(), is("Use case error"));
        verify(departmentControllerMapper).toApplicationRequest(requestDTO);
        verify(createDepartmentUseCase).execute(applicationRequest);
        verifyNoMoreInteractions(departmentControllerMapper); // Should not call response mapping
    }
}
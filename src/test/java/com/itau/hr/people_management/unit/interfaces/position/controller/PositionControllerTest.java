package com.itau.hr.people_management.unit.interfaces.position.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.itau.hr.people_management.application.position.dto.CreatePositionRequest;
import com.itau.hr.people_management.application.position.dto.PositionResponse;
import com.itau.hr.people_management.application.position.usecase.CreatePositionUseCase;
import com.itau.hr.people_management.application.position.usecase.GetPositionUseCase;
import com.itau.hr.people_management.interfaces.position.controller.PositionController;
import com.itau.hr.people_management.interfaces.position.dto.PositionRequestDTO;
import com.itau.hr.people_management.interfaces.position.dto.PositionResponseDTO;
import com.itau.hr.people_management.interfaces.position.mapper.PositionControllerMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("PositionController Unit Tests")
class PositionControllerTest {

    @Mock
    private CreatePositionUseCase createPositionUseCase;

    @Mock
    private GetPositionUseCase getPositionUseCase;

    @Mock
    private PositionControllerMapper positionControllerMapper;

    @Mock
    private PositionRequestDTO requestDTO;

    @Mock
    private CreatePositionRequest applicationRequest;

    @Mock
    private PositionResponse applicationResponse;

    @Mock
    private PositionResponseDTO responseDTO;

    private PositionController controller;

    @BeforeEach
    void setUp() {
        controller = new PositionController(createPositionUseCase, getPositionUseCase, positionControllerMapper);
    }

    @Test
    @DisplayName("Should create position and return 201 CREATED")
    void shouldCreatePositionAndReturn201Created() {
        // Arrange
        when(positionControllerMapper.toApplicationRequest(requestDTO)).thenReturn(applicationRequest);
        when(createPositionUseCase.execute(applicationRequest)).thenReturn(applicationResponse);
        when(positionControllerMapper.toPositionResponseDTO(applicationResponse)).thenReturn(responseDTO);

        // Act
        ResponseEntity<PositionResponseDTO> result = controller.createPosition(requestDTO);

        // Assert
        assertThat(result.getStatusCode(), is(HttpStatus.CREATED));
        assertThat(result.getBody(), is(responseDTO));
        
        verify(positionControllerMapper).toApplicationRequest(requestDTO);
        verify(createPositionUseCase).execute(applicationRequest);
        verify(positionControllerMapper).toPositionResponseDTO(applicationResponse);
    }

    @Test
    @DisplayName("Should get all positions and return 200 OK")
    void shouldGetAllPositionsAndReturn200Ok() {
        // Arrange
        List<PositionResponse> applicationResponses = List.of(applicationResponse);
        List<PositionResponseDTO> responseDTOs = List.of(responseDTO);
        
        when(getPositionUseCase.getAll()).thenReturn(applicationResponses);
        when(positionControllerMapper.toPositionResponseDTOList(applicationResponses)).thenReturn(responseDTOs);

        // Act
        ResponseEntity<List<PositionResponseDTO>> result = controller.getAllPositions();

        // Assert
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), is(responseDTOs));
        assertThat(result.getBody(), hasSize(1));
        
        verify(getPositionUseCase).getAll();
        verify(positionControllerMapper).toPositionResponseDTOList(applicationResponses);
    }

    @Test
    @DisplayName("Should handle empty list for get all positions")
    void shouldHandleEmptyListForGetAllPositions() {
        // Arrange
        List<PositionResponse> emptyApplicationResponses = List.of();
        List<PositionResponseDTO> emptyResponseDTOs = List.of();
        
        when(getPositionUseCase.getAll()).thenReturn(emptyApplicationResponses);
        when(positionControllerMapper.toPositionResponseDTOList(emptyApplicationResponses)).thenReturn(emptyResponseDTOs);

        // Act
        ResponseEntity<List<PositionResponseDTO>> result = controller.getAllPositions();

        // Assert
        assertThat(result.getStatusCode(), is(HttpStatus.OK));
        assertThat(result.getBody(), is(emptyResponseDTOs));
        assertThat(result.getBody(), is(empty()));
    }

    @Test
    @DisplayName("Should follow correct workflow for create position")
    void shouldFollowCorrectWorkflowForCreatePosition() {
        // Arrange
        when(positionControllerMapper.toApplicationRequest(requestDTO)).thenReturn(applicationRequest);
        when(createPositionUseCase.execute(applicationRequest)).thenReturn(applicationResponse);
        when(positionControllerMapper.toPositionResponseDTO(applicationResponse)).thenReturn(responseDTO);

        // Act
        controller.createPosition(requestDTO);

        // Assert - Verify execution order
        verify(positionControllerMapper).toApplicationRequest(requestDTO);
        verify(createPositionUseCase).execute(applicationRequest);
        verify(positionControllerMapper).toPositionResponseDTO(applicationResponse);
    }

    @Test
    @DisplayName("Should delegate exception handling to framework")
    void shouldDelegateExceptionHandlingToFramework() {
        // Arrange
        RuntimeException useCaseException = new RuntimeException("Use case error");
        when(positionControllerMapper.toApplicationRequest(requestDTO)).thenReturn(applicationRequest);
        when(createPositionUseCase.execute(applicationRequest)).thenThrow(useCaseException);

        // Act & Assert
        RuntimeException exception = org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () ->
            controller.createPosition(requestDTO)
        );

        assertThat(exception.getMessage(), is("Use case error"));
        verify(positionControllerMapper).toApplicationRequest(requestDTO);
        verify(createPositionUseCase).execute(applicationRequest);
        verifyNoMoreInteractions(positionControllerMapper); // Should not call response mapping
    }
}
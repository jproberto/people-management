package com.itau.hr.people_management.interfaces.position.mapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.application.position.dto.CreatePositionRequest;
import com.itau.hr.people_management.application.position.dto.PositionResponse;
import com.itau.hr.people_management.domain.position.enumeration.PositionLevel;
import com.itau.hr.people_management.domain.shared.message.DomainMessageSource;
import com.itau.hr.people_management.interfaces.position.dto.PositionRequestDTO;
import com.itau.hr.people_management.interfaces.position.dto.PositionResponseDTO;

@ExtendWith(MockitoExtension.class)
@DisplayName("PositionControllerMapper Unit Tests")
class PositionControllerMapperTest {

    @Mock
    private DomainMessageSource domainMessageSource;

    private PositionControllerMapper mapper;
    private PositionRequestDTO requestDTO;
    private PositionResponse applicationResponse;

    @BeforeEach
    void setUp() {
        mapper = new PositionControllerMapper(domainMessageSource);
        
        requestDTO = new PositionRequestDTO();
        requestDTO.setTitle("Software Engineer");
        requestDTO.setPositionLevel("SENIOR");
        
        applicationResponse = new PositionResponse(
            UUID.randomUUID(), 
            "Software Engineer", 
            "SENIOR"
        );
    }

    @Test
    @DisplayName("Should map API request to application request")
    void shouldMapApiRequestToApplicationRequest() {
        // Act
        CreatePositionRequest result = mapper.toApplicationRequest(requestDTO);

        // Assert
        assertThat(result.getTitle(), is("Software Engineer"));
        assertThat(result.getPositionLevelName(), is("SENIOR"));
    }

    @Test
    @DisplayName("Should return null when API request is null")
    void shouldReturnNullWhenApiRequestIsNull() {
        // Act
        CreatePositionRequest result = mapper.toApplicationRequest(null);

        // Assert
        assertThat(result, is(nullValue()));
    }

    @Test
    @DisplayName("Should map application response to DTO with position level conversion")
    void shouldMapApplicationResponseToDtoWithPositionLevelConversion() {
        // Act
        PositionResponseDTO result = mapper.toPositionResponseDTO(applicationResponse);

        // Assert
        assertThat(result.getId(), is(applicationResponse.getId()));
        assertThat(result.getTitle(), is("Software Engineer"));
        assertThat(result.getPositionLevel(), is(PositionLevel.SENIOR));
    }

    @Test
    @DisplayName("Should map application response with null position level")
    void shouldMapApplicationResponseWithNullPositionLevel() {
        // Arrange
        PositionResponse responseWithNullLevel = new PositionResponse(
            UUID.randomUUID(), 
            "Software Engineer", 
            null
        );

        // Act
        PositionResponseDTO result = mapper.toPositionResponseDTO(responseWithNullLevel);

        // Assert
        assertThat(result.getId(), is(responseWithNullLevel.getId()));
        assertThat(result.getTitle(), is("Software Engineer"));
        assertThat(result.getPositionLevel(), is(nullValue()));
        verifyNoInteractions(domainMessageSource);
    }

    @Test
    @DisplayName("Should return null when application response is null")
    void shouldReturnNullWhenApplicationResponseIsNull() {
        // Act
        PositionResponseDTO result = mapper.toPositionResponseDTO(null);

        // Assert
        assertThat(result, is(nullValue()));
    }

    @Test
    @DisplayName("Should map list of application responses to DTOs")
    void shouldMapListOfApplicationResponsesToDtos() {
        // Arrange
        PositionResponse response2 = new PositionResponse(
            UUID.randomUUID(), 
            "Senior Developer", 
            "SENIOR"
        );
        List<PositionResponse> applicationResponses = List.of(applicationResponse, response2);
        
        // Act
        List<PositionResponseDTO> result = mapper.toPositionResponseDTOList(applicationResponses);

        // Assert
        assertThat(result, hasSize(2));
        assertThat(result.get(0).getTitle(), is("Software Engineer"));
        assertThat(result.get(1).getTitle(), is("Senior Developer"));
    }

    @Test
    @DisplayName("Should return empty list when input list is null")
    void shouldReturnEmptyListWhenInputListIsNull() {
        // Act
        List<PositionResponseDTO> result = mapper.toPositionResponseDTOList(null);

        // Assert
        assertThat(result, is(empty()));
    }

    @Test
    @DisplayName("Should handle null elements in list gracefully")
    void shouldHandleNullElementsInListGracefully() {
        // Arrange
        List<PositionResponse> listWithNull = new ArrayList<>();
        listWithNull.add(applicationResponse);
        listWithNull.add(null);

        // Act
        List<PositionResponseDTO> result = mapper.toPositionResponseDTOList(listWithNull);

        // Assert
        assertThat(result, hasSize(2));
        assertThat(result.get(0), is(notNullValue()));
        assertThat(result.get(1), is(nullValue()));
    }

    @Test
    @DisplayName("Should propagate PositionLevel conversion exceptions")
    void shouldPropagatePositionLevelConversionExceptions() {
        // Arrange
        PositionResponse responseWithInvalidLevel = new PositionResponse(
            UUID.randomUUID(), 
            "Software Engineer", 
            "INVALID_LEVEL"
        );
        when(domainMessageSource.getMessage(anyString(), anyString()))
            .thenReturn("Invalid position level: INVALID_LEVEL");

        // Act & Assert
        IllegalArgumentException exception = org.junit.jupiter.api.Assertions.assertThrows(
            IllegalArgumentException.class, () ->
                mapper.toPositionResponseDTO(responseWithInvalidLevel)
        );

        assertThat(exception.getMessage(), is("Invalid position level: INVALID_LEVEL"));
    }
}
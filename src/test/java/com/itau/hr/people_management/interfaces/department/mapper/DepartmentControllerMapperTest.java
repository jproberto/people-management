package com.itau.hr.people_management.interfaces.department.mapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.itau.hr.people_management.application.department.dto.CreateDepartmentRequest;
import com.itau.hr.people_management.application.department.dto.DepartmentResponse;
import com.itau.hr.people_management.interfaces.department.dto.DepartmentRequestDTO;
import com.itau.hr.people_management.interfaces.department.dto.DepartmentResponseDTO;

@DisplayName("DepartmentControllerMapper Unit Tests")
class DepartmentControllerMapperTest {

    private DepartmentControllerMapper mapper;
    private DepartmentRequestDTO requestDTO;
    private DepartmentResponse applicationResponse;

    @BeforeEach
    void setUp() {
        mapper = new DepartmentControllerMapper();
        
        requestDTO = new DepartmentRequestDTO();
        requestDTO.setName("Information Technology");
        requestDTO.setCostCenterCode("IT001");
        
        applicationResponse = new DepartmentResponse(
            UUID.randomUUID(), 
            "Information Technology", 
            "IT001"
        );
    }

    @Test
    @DisplayName("Should map API request to application request")
    void shouldMapApiRequestToApplicationRequest() {
        // Act
        CreateDepartmentRequest result = mapper.toApplicationRequest(requestDTO);

        // Assert
        assertThat(result.getName(), is("Information Technology"));
        assertThat(result.getCostCenterCode(), is("IT001"));
    }

    @Test
    @DisplayName("Should return null when API request is null")
    void shouldReturnNullWhenApiRequestIsNull() {
        // Act
        CreateDepartmentRequest result = mapper.toApplicationRequest(null);

        // Assert
        assertThat(result, is(nullValue()));
    }

    @Test
    @DisplayName("Should map application response to DTO")
    void shouldMapApplicationResponseToDto() {
        // Act
        DepartmentResponseDTO result = mapper.toDepartmentResponseDTO(applicationResponse);

        // Assert
        assertThat(result.getId(), is(applicationResponse.getId()));
        assertThat(result.getName(), is(applicationResponse.getName()));
        assertThat(result.getCostCenterCode(), is(applicationResponse.getCostCenterCode()));
    }

    @Test
    @DisplayName("Should return null when application response is null")
    void shouldReturnNullWhenApplicationResponseIsNull() {
        // Act
        DepartmentResponseDTO result = mapper.toDepartmentResponseDTO(null);

        // Assert
        assertThat(result, is(nullValue()));
    }

    @Test
    @DisplayName("Should map list of application responses to DTOs")
    void shouldMapListOfApplicationResponsesToDtos() {
        // Arrange
        DepartmentResponse response2 = new DepartmentResponse(
            UUID.randomUUID(), 
            "Human Resources", 
            "HR001"
        );
        List<DepartmentResponse> applicationResponses = List.of(applicationResponse, response2);

        // Act
        List<DepartmentResponseDTO> result = mapper.toDepartmentResponseDTOList(applicationResponses);

        // Assert
        assertThat(result, hasSize(2));
        assertThat(result.get(0).getName(), is("Information Technology"));
        assertThat(result.get(1).getName(), is("Human Resources"));
    }

    @Test
    @DisplayName("Should return empty list when input list is null or empty")
    void shouldReturnEmptyListWhenInputListIsNullOrEmpty() {
        // Act & Assert
        assertThat(mapper.toDepartmentResponseDTOList(null), is(empty()));
        assertThat(mapper.toDepartmentResponseDTOList(List.of()), is(empty()));
    }

    @Test
    @DisplayName("Should handle null elements in list gracefully")
    void shouldHandleNullElementsInListGracefully() {
        // Arrange
        List<DepartmentResponse> listWithNull = new ArrayList<>();
        listWithNull.add(applicationResponse);
        listWithNull.add(null);

        // Act
        List<DepartmentResponseDTO> result = mapper.toDepartmentResponseDTOList(listWithNull);

        // Assert
        assertThat(result, hasSize(2));
        assertThat(result.get(0), is(notNullValue()));
        assertThat(result.get(1), is(nullValue()));
    }
}
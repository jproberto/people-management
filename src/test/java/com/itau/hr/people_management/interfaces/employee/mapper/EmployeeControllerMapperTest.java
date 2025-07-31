package com.itau.hr.people_management.interfaces.employee.mapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.application.department.dto.DepartmentResponse;
import com.itau.hr.people_management.application.employee.dto.CreateEmployeeRequest;
import com.itau.hr.people_management.application.employee.dto.EmployeeResponse;
import com.itau.hr.people_management.application.position.dto.PositionResponse;
import com.itau.hr.people_management.domain.employee.criteria.EmployeeSearchCriteria;
import com.itau.hr.people_management.domain.employee.enumeration.EmployeeStatus;
import com.itau.hr.people_management.interfaces.department.dto.DepartmentResponseDTO;
import com.itau.hr.people_management.interfaces.department.mapper.DepartmentControllerMapper;
import com.itau.hr.people_management.interfaces.employee.dto.EmployeeRequestDTO;
import com.itau.hr.people_management.interfaces.employee.dto.EmployeeResponseDTO;
import com.itau.hr.people_management.interfaces.employee.dto.EmployeeSearchRequestDTO;
import com.itau.hr.people_management.interfaces.position.dto.PositionResponseDTO;
import com.itau.hr.people_management.interfaces.position.mapper.PositionControllerMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeControllerMapper Unit Tests")
class EmployeeControllerMapperTest {

    @Mock private DepartmentControllerMapper departmentControllerMapper;
    @Mock private PositionControllerMapper positionControllerMapper;
    @Mock private DepartmentResponse departmentResponse;
    @Mock private PositionResponse positionResponse;
    @Mock private DepartmentResponseDTO departmentResponseDTO;
    @Mock private PositionResponseDTO positionResponseDTO;

    private EmployeeControllerMapper mapper;
    private EmployeeRequestDTO requestDTO;
    private EmployeeResponse applicationResponse;
    private EmployeeSearchRequestDTO searchRequestDTO;
    @BeforeEach
    void setUp() throws Exception {
        mapper = new EmployeeControllerMapper();
        
        // Use reflection to set private fields
        Field departmentField = EmployeeControllerMapper.class.getDeclaredField("departmentControllerMapper");
        departmentField.setAccessible(true);
        departmentField.set(mapper, departmentControllerMapper);
        
        Field positionField = EmployeeControllerMapper.class.getDeclaredField("positionControllerMapper");
        positionField.setAccessible(true);
        positionField.set(mapper, positionControllerMapper);

        requestDTO = new EmployeeRequestDTO();
        requestDTO.setName("John Doe");
        requestDTO.setEmail("john.doe@example.com");
        requestDTO.setHireDate(LocalDate.of(2023, 1, 15));
        requestDTO.setDepartmentId(UUID.randomUUID());
        requestDTO.setPositionId(UUID.randomUUID());

        applicationResponse = new EmployeeResponse(
            UUID.randomUUID(),
            "John Doe",
            "john.doe@example.com",
            LocalDate.of(2023, 1, 15),
            "ACTIVE",
            departmentResponse,
            positionResponse
        );

        searchRequestDTO = EmployeeSearchRequestDTO.builder()
            .name("John")
            .emailAddress("john@example.com")
            .status(EmployeeStatus.ACTIVE)
            .departmentId(UUID.randomUUID())
            .department("IT")
            .positionId(UUID.randomUUID())
            .position("Developer")
            .positionLevel("Senior")
            .build();
    }

    @Test
    @DisplayName("Should map API request to application request")
    void shouldMapApiRequestToApplicationRequest() {
        // Act
        CreateEmployeeRequest result = mapper.toApplicationRequest(requestDTO);

        // Assert
        assertThat(result.getName(), is("John Doe"));
        assertThat(result.getEmail(), is("john.doe@example.com"));
        assertThat(result.getHireDate(), is(LocalDate.of(2023, 1, 15)));
        assertThat(result.getDepartmentId(), is(requestDTO.getDepartmentId()));
        assertThat(result.getPositionId(), is(requestDTO.getPositionId()));
    }

    @Test
    @DisplayName("Should return null when API request is null")
    void shouldReturnNullWhenApiRequestIsNull() {
        // Act
        CreateEmployeeRequest result = mapper.toApplicationRequest(null);

        // Assert
        assertThat(result, is(nullValue()));
    }

    @Test
    @DisplayName("Should map application response to DTO with nested objects")
    void shouldMapApplicationResponseToDtoWithNestedObjects() {
        // Arrange
        when(departmentControllerMapper.toDepartmentResponseDTO(departmentResponse)).thenReturn(departmentResponseDTO);
        when(positionControllerMapper.toPositionResponseDTO(positionResponse)).thenReturn(positionResponseDTO);

        // Act
        EmployeeResponseDTO result = mapper.toEmployeeResponseDTO(applicationResponse);

        // Assert
        assertThat(result.getId(), is(applicationResponse.getId()));
        assertThat(result.getName(), is("John Doe"));
        assertThat(result.getEmail(), is("john.doe@example.com"));
        assertThat(result.getHireDate(), is(LocalDate.of(2023, 1, 15)));
        assertThat(result.getEmployeeStatus(), is(EmployeeStatus.ACTIVE));
        assertThat(result.getDepartment(), is(departmentResponseDTO));
        assertThat(result.getPosition(), is(positionResponseDTO));

        verify(departmentControllerMapper).toDepartmentResponseDTO(departmentResponse);
        verify(positionControllerMapper).toPositionResponseDTO(positionResponse);
    }

    @Test
    @DisplayName("Should map application response with null nested objects")
    void shouldMapApplicationResponseWithNullNestedObjects() {
        // Arrange
        EmployeeResponse responseWithNulls = new EmployeeResponse(
            UUID.randomUUID(), "John Doe", "john@example.com", 
            LocalDate.now(), "ACTIVE", null, null
        );

        // Act
        EmployeeResponseDTO result = mapper.toEmployeeResponseDTO(responseWithNulls);

        // Assert
        assertThat(result.getName(), is("John Doe"));
        assertThat(result.getDepartment(), is(nullValue()));
        assertThat(result.getPosition(), is(nullValue()));
        verifyNoInteractions(departmentControllerMapper, positionControllerMapper);
    }

    @Test
    @DisplayName("Should return null when application response is null")
    void shouldReturnNullWhenApplicationResponseIsNull() {
        // Act
        EmployeeResponseDTO result = mapper.toEmployeeResponseDTO(null);

        // Assert
        assertThat(result, is(nullValue()));
    }

    @Test
    @DisplayName("Should map list of application responses to DTOs")
    void shouldMapListOfApplicationResponsesToDtos() {
        // Arrange
        List<EmployeeResponse> applicationResponses = List.of(applicationResponse);
        when(departmentControllerMapper.toDepartmentResponseDTO(departmentResponse)).thenReturn(departmentResponseDTO);
        when(positionControllerMapper.toPositionResponseDTO(positionResponse)).thenReturn(positionResponseDTO);

        // Act
        List<EmployeeResponseDTO> result = mapper.toEmployeeResponseDTOList(applicationResponses);

        // Assert
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getName(), is("John Doe"));
    }

    @Test
    @DisplayName("Should return empty list when input list is null")
    void shouldReturnEmptyListWhenInputListIsNull() {
        // Act
        List<EmployeeResponseDTO> result = mapper.toEmployeeResponseDTOList(null);

        // Assert
        assertThat(result, is(empty()));
    }

    @Test
    @DisplayName("Should map search request to criteria")
    void shouldMapSearchRequestToCriteria() {
        // Act
        EmployeeSearchCriteria result = mapper.toEmployeeSearchCriteria(searchRequestDTO);

        // Assert
        assertThat(result.getName().get(), is("John"));
        assertThat(result.getEmailAddress().get(), is("john@example.com"));
        assertThat(result.getEmployeeStatus().get(), is(EmployeeStatus.ACTIVE));
        assertThat(result.getDepartmentId().get(), is(searchRequestDTO.getDepartmentId()));
        assertThat(result.getDepartmentName().get(), is("IT"));
        assertThat(result.getPositionId().get(), is(searchRequestDTO.getPositionId()));
        assertThat(result.getPositionTitle().get(), is("Developer"));
        assertThat(result.getPositionLevel().get(), is("Senior"));
    }

    @Test
    @DisplayName("Should return empty criteria when search request is null")
    void shouldReturnEmptyCriteriaWhenSearchRequestIsNull() {
        // Act
        EmployeeSearchCriteria result = mapper.toEmployeeSearchCriteria(null);

        // Assert
        assertThat(result.getName().isPresent(), is(false));
        assertThat(result.getEmailAddress().isPresent(), is(false));
        assertThat(result.getEmployeeStatus().isPresent(), is(false));
        assertThat(result.getDepartmentId().isPresent(), is(false));
        assertThat(result.getDepartmentName().isPresent(), is(false));
        assertThat(result.getPositionId().isPresent(), is(false));
        assertThat(result.getPositionTitle().isPresent(), is(false));
        assertThat(result.getPositionLevel().isPresent(), is(false));
    }
}
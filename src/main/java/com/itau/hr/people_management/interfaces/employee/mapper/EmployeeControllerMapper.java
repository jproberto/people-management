package com.itau.hr.people_management.interfaces.employee.mapper;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itau.hr.people_management.application.employee.dto.CreateEmployeeRequest;
import com.itau.hr.people_management.application.employee.dto.EmployeeResponse;
import com.itau.hr.people_management.domain.employee.EmployeeSearchCriteria;
import com.itau.hr.people_management.domain.employee.EmployeeStatus;
import com.itau.hr.people_management.interfaces.department.mapper.DepartmentControllerMapper;
import com.itau.hr.people_management.interfaces.employee.dto.EmployeeRequestDTO;
import com.itau.hr.people_management.interfaces.employee.dto.EmployeeResponseDTO;
import com.itau.hr.people_management.interfaces.employee.dto.EmployeeSearchRequestDTO;
import com.itau.hr.people_management.interfaces.position.mapper.PositionControllerMapper;

@Component
public class EmployeeControllerMapper {

    @Autowired
    private DepartmentControllerMapper departmentControllerMapper;
    @Autowired
    private PositionControllerMapper positionControllerMapper;

    public CreateEmployeeRequest toApplicationRequest(EmployeeRequestDTO apiRequestDTO) {
        if (apiRequestDTO == null) {
            return null;
        }
        

        return new CreateEmployeeRequest(
            apiRequestDTO.getName(),
            apiRequestDTO.getEmail(),
            apiRequestDTO.getHireDate(),
            apiRequestDTO.getDepartmentId(),
            apiRequestDTO.getPositionId()
        );
    }

    public EmployeeResponseDTO toEmployeeResponseDTO(EmployeeResponse applicationResponse) {
        if (applicationResponse == null) {
            return null;
        }

        EmployeeResponseDTO responseDTO = new EmployeeResponseDTO();
        responseDTO.setId(applicationResponse.getId());
        responseDTO.setName(applicationResponse.getName());
        responseDTO.setEmail(applicationResponse.getEmail());  
        responseDTO.setHireDate(applicationResponse.getHireDate());
        responseDTO.setEmployeeStatus(EmployeeStatus.valueOf(applicationResponse.getEmployeeStatus()));

        if (applicationResponse.getDepartment() != null) {
            responseDTO.setDepartment(departmentControllerMapper.toDepartmentResponseDTO(applicationResponse.getDepartment()));
        }

        if (applicationResponse.getPosition() != null) {
            responseDTO.setPosition(positionControllerMapper.toPositionResponseDTO(applicationResponse.getPosition()));
        }

        return responseDTO;
    }

    public List<EmployeeResponseDTO> toEmployeeResponseDTOList(List<EmployeeResponse> applicationRequests) {
        if (applicationRequests == null) {
            return List.of();
        }
        return applicationRequests.stream()
                .map(this::toEmployeeResponseDTO)
                .toList();
    }

    public EmployeeSearchCriteria toEmployeeSearchCriteria(EmployeeSearchRequestDTO apiRequestDTO) {
        if (apiRequestDTO == null) {
            return EmployeeSearchCriteria.builder().build();
        }
        return EmployeeSearchCriteria
                .builder()
                .name(apiRequestDTO.getName())
                .emailAddress(apiRequestDTO.getEmailAddress())
                .employeeStatus(apiRequestDTO.getStatus())
                .departmentId(apiRequestDTO.getDepartmentId())
                .departmentName(apiRequestDTO.getDepartment())
                .positionId(apiRequestDTO.getPositionId())
                .positionTitle(apiRequestDTO.getPosition())
                .positionLevel(apiRequestDTO.getPositionLevel())
                .build();
    }
}

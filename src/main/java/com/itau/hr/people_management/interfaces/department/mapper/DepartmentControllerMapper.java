package com.itau.hr.people_management.interfaces.department.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.itau.hr.people_management.application.department.dto.CreateDepartmentRequest;
import com.itau.hr.people_management.application.department.dto.DepartmentResponse;
import com.itau.hr.people_management.interfaces.department.dto.DepartmentRequestDTO;
import com.itau.hr.people_management.interfaces.department.dto.DepartmentResponseDTO;

@Component 
public class DepartmentControllerMapper {

    public CreateDepartmentRequest toApplicationRequest(DepartmentRequestDTO apiRequestDTO) {
        if (apiRequestDTO == null) {
            return null;
        }
        return new CreateDepartmentRequest(apiRequestDTO.getName(), apiRequestDTO.getCostCenterCode());
    }

    public DepartmentResponseDTO toDepartmentResponseDTO(DepartmentResponse applicationResponse) {
        if (applicationResponse == null) {
            return null;
        }
        
        DepartmentResponseDTO responseDTO = new DepartmentResponseDTO();
        responseDTO.setId(applicationResponse.getId());
        responseDTO.setName(applicationResponse.getName());
        responseDTO.setCostCenterCode(applicationResponse.getCostCenterCode());
        return responseDTO;
    }

    public List<DepartmentResponseDTO> toDepartmentResponseDTOList(List<DepartmentResponse> applicationResponses) {
        if (applicationResponses == null || applicationResponses.isEmpty()) {
            return List.of();
        }
        return applicationResponses.stream()
                .map(this::toDepartmentResponseDTO)
                .toList();
    }
}
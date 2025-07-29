package com.itau.hr.people_management.interfaces.position.mapper;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.itau.hr.people_management.application.position.dto.CreatePositionRequest;
import com.itau.hr.people_management.application.position.dto.PositionResponse;
import com.itau.hr.people_management.domain.position.enumeration.PositionLevel;
import com.itau.hr.people_management.domain.shared.message.DomainMessageSource;
import com.itau.hr.people_management.interfaces.position.dto.PositionRequestDTO;
import com.itau.hr.people_management.interfaces.position.dto.PositionResponseDTO;

@Component
public class PositionControllerMapper {

    private final DomainMessageSource domainMessageSource;

    @Autowired
    public PositionControllerMapper(DomainMessageSource domainMessageSource) {
        this.domainMessageSource = domainMessageSource;
    }

    public CreatePositionRequest toApplicationRequest(PositionRequestDTO apiRequestDTO) {
        if (apiRequestDTO == null) {
            return null;
        }
        return new CreatePositionRequest(apiRequestDTO.getTitle(), apiRequestDTO.getPositionLevel());
    }

    public PositionResponseDTO toPositionResponseDTO(PositionResponse applicationResponse) {
        if (applicationResponse == null) {
            return null;
        }
        
        PositionResponseDTO responseDTO = new PositionResponseDTO();
        responseDTO.setId(applicationResponse.getId());
        responseDTO.setTitle(applicationResponse.getTitle());
        if (applicationResponse.getPositionLevelName() != null) {
            responseDTO.setPositionLevel(PositionLevel.fromString(applicationResponse.getPositionLevelName(), domainMessageSource));
        }
        return responseDTO;
    }

     public List<PositionResponseDTO> toPositionResponseDTOList(List<PositionResponse> applicationResponses) {
        if (applicationResponses == null) {
            return Collections.emptyList();
        }
        return applicationResponses.stream()
                .map(this::toPositionResponseDTO)
                .toList();
    }
}

package com.itau.hr.people_management.application.position.usecase;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.itau.hr.people_management.application.position.dto.PositionResponse;
import com.itau.hr.people_management.domain.position.repository.PositionRepository;

@Service
@Transactional(readOnly = true)
public class GetPositionUseCase {
    private final PositionRepository positionRepository;

    public GetPositionUseCase(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    public List<PositionResponse> getAll() {
        return positionRepository.findAll()
                .stream()
                .map(PositionResponse::new)
                .toList();
    }
}

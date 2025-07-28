package com.itau.hr.people_management.application.position.usecase;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.itau.hr.people_management.application.position.dto.CreatePositionRequest;
import com.itau.hr.people_management.application.position.dto.PositionResponse;
import com.itau.hr.people_management.domain.position.Position;
import com.itau.hr.people_management.domain.position.PositionLevel;
import com.itau.hr.people_management.domain.position.repository.PositionRepository;
import com.itau.hr.people_management.domain.shared.exception.ConflictException;
import com.itau.hr.people_management.domain.shared.message.DomainMessageSource;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class CreatePositionUseCase {
    private final PositionRepository positionRepository;
    private final DomainMessageSource messageSource;

    public CreatePositionUseCase(PositionRepository positionRepository, DomainMessageSource messageSource) {
        this.positionRepository = positionRepository;
        this.messageSource = messageSource;
    }

    public PositionResponse execute(CreatePositionRequest request) {
        PositionLevel positionLevel = PositionLevel.fromString(request.getPositionLevelName(), messageSource);
        
        if (positionRepository.findByTitleAndPositionLevel(request.getTitle(), positionLevel).isPresent()) {
            throw new ConflictException("error.position.title.positionlevel.exists", request.getTitle(), positionLevel.getDisplayName());
        }

        Position position = Position.create(
                UUID.randomUUID(), 
                request.getTitle(),
                positionLevel 
        );

        Position savedPosition = positionRepository.save(position);

        return new PositionResponse(savedPosition);
    }
}

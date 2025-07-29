package com.itau.hr.people_management.application.position.dto;

import java.util.UUID;

import com.itau.hr.people_management.domain.position.entity.Position;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PositionResponse {
    private UUID id;
    private String title;
    private String positionLevelName;

    public PositionResponse(Position position) {
        this.id = position.getId();
        this.title = position.getTitle();
        
        if (position.getPositionLevel() != null) {
            this.positionLevelName = position.getPositionLevel().getDisplayName();
        }
    }
}
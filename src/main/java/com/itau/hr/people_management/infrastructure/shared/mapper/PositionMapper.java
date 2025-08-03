package com.itau.hr.people_management.infrastructure.shared.mapper;

import com.itau.hr.people_management.domain.position.entity.Position;
import com.itau.hr.people_management.infrastructure.persistence.entity.PositionJpaEntity;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PositionMapper {
    public static PositionJpaEntity toJpaEntity(Position domainPosition) {
        if (domainPosition == null) {
            return null;
        }
        
        return PositionJpaEntity.builder()
                .id(domainPosition.getId())
                .title(domainPosition.getTitle())
                .positionLevel(domainPosition.getPositionLevel())
                .build();
    }

    public static Position toDomainEntity(PositionJpaEntity jpaPosition) {
        if (jpaPosition == null) {
            return null;
        }
        
        return Position.create(
                jpaPosition.getId(),
                jpaPosition.getTitle(),
                jpaPosition.getPositionLevel()
        );
    }
}

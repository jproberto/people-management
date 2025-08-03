package com.itau.hr.people_management.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.itau.hr.people_management.domain.position.enumeration.PositionLevel;
import com.itau.hr.people_management.infrastructure.persistence.entity.PositionJpaEntity;

@Repository
public interface JpaPositionRepository extends JpaRepository<PositionJpaEntity, UUID> {
    Optional<PositionJpaEntity> findByTitleAndPositionLevel(String title, PositionLevel positionLevel);
}

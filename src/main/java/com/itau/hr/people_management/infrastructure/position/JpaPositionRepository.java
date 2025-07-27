package com.itau.hr.people_management.infrastructure.position;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.itau.hr.people_management.infrastructure.position.entity.PositionJpaEntity;

@Repository
public interface JpaPositionRepository extends JpaRepository<PositionJpaEntity, UUID> {
    Optional<PositionJpaEntity> findByTitle(String title);
}

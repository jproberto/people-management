package com.itau.hr.people_management.domain.position.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.itau.hr.people_management.domain.position.entity.Position;
import com.itau.hr.people_management.domain.position.enumeration.PositionLevel;

public interface PositionRepository {
    Position save(Position position);
    Optional<Position> findById(UUID id);
    void delete(Position position);
    List<Position> findAll();
    Optional<Position> findByTitleAndPositionLevel(String title, PositionLevel positionLevel);
}

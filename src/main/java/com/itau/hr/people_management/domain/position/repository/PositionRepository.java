package com.itau.hr.people_management.domain.position.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.itau.hr.people_management.domain.position.Position;

public interface PositionRepository {
    Position save(Position position);
    Optional<Position> findById(UUID id);
    void delete(Position position);
    List<Position> findAll();
    Optional<Position> findByTitle(String title);
}

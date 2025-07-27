package com.itau.hr.people_management.infrastructure.position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.itau.hr.people_management.domain.position.Position;
import com.itau.hr.people_management.domain.position.repository.PositionRepository;

@Repository
public class InMemoryPositionRepository implements PositionRepository {
    private final Map<UUID, Position> positions = new HashMap<>();

    @Override
    public Position save(Position position) {
        positions.put(position.getId(), position);
        return position;
    }

    @Override
    public Optional<Position> findById(UUID id) {
        return Optional.ofNullable(positions.get(id));
    }

    @Override
    public void delete(Position position) {
        positions.remove(position.getId());
    }

    @Override
    public List<Position> findAll() {
        return new ArrayList<>(positions.values());
    }

    @Override
    public Optional<Position> findByTitle(String title) {
        return positions.values().stream()
                .filter(position -> position.getTitle().equals(title))
                .findFirst();
    }
    
}

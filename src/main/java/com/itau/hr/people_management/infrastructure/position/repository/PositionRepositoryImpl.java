package com.itau.hr.people_management.infrastructure.position.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.itau.hr.people_management.domain.position.Position;
import com.itau.hr.people_management.domain.position.PositionLevel;
import com.itau.hr.people_management.domain.position.repository.PositionRepository;
import com.itau.hr.people_management.infrastructure.position.entity.PositionJpaEntity;
import com.itau.hr.people_management.infrastructure.shared.mapper.PositionMapper;

@Component
public class PositionRepositoryImpl implements PositionRepository {

    private final JpaPositionRepository jpaPositionRepository;

    public PositionRepositoryImpl(JpaPositionRepository jpaPositionRepository) {
        this.jpaPositionRepository = jpaPositionRepository;
    }

    @Override
    public Optional<Position> findById(UUID id) {
        return jpaPositionRepository.findById(id)
                .map(PositionMapper::toDomainEntity);
    }

    @Override
    public List<Position> findAll() {
        return jpaPositionRepository.findAll()
                .stream()
                .map(PositionMapper::toDomainEntity)
                .toList();
    }

    @Override
    public Position save(Position position) {
        PositionJpaEntity jpaEntity = PositionMapper.toJpaEntity(position);
        return PositionMapper.toDomainEntity(jpaPositionRepository.save(jpaEntity));
    }

    @Override
    public void delete(Position position) {
        jpaPositionRepository.delete(PositionMapper.toJpaEntity(position));
    }

    @Override
    public Optional<Position> findByTitleAndPositionLevel(String title, PositionLevel positionLevel) {
        return jpaPositionRepository.findByTitleAndPositionLevel(title, positionLevel)
                .map(PositionMapper::toDomainEntity);
    }
}

package com.itau.hr.people_management.unit.infrastructure.persistence.repository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.domain.position.entity.Position;
import com.itau.hr.people_management.domain.position.enumeration.PositionLevel;
import com.itau.hr.people_management.infrastructure.persistence.entity.PositionJpaEntity;
import com.itau.hr.people_management.infrastructure.persistence.repository.JpaPositionRepository;
import com.itau.hr.people_management.infrastructure.persistence.repository.PositionRepositoryImpl;
import com.itau.hr.people_management.infrastructure.shared.mapper.PositionMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("PositionRepositoryImpl Unit Tests")
class PositionRepositoryImplTest {

    @Mock
    private JpaPositionRepository jpaPositionRepository;

    @Mock
    private Position position;

    @Mock
    private PositionJpaEntity jpaEntity;

    private PositionRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new PositionRepositoryImpl(jpaPositionRepository);
    }

    @Test
    @DisplayName("Should delegate findById with mapping")
    void shouldDelegateFindByIdWithMapping() {
        try (MockedStatic<PositionMapper> mapperMock = mockStatic(PositionMapper.class)) {
            // Arrange
            UUID positionId = UUID.randomUUID();
            when(jpaPositionRepository.findById(positionId)).thenReturn(Optional.of(jpaEntity));
            mapperMock.when(() -> PositionMapper.toDomainEntity(jpaEntity)).thenReturn(position);

            // Act
            Optional<Position> result = repository.findById(positionId);

            // Assert
            assertThat(result.isPresent(), is(true));
            assertThat(result.get(), is(sameInstance(position)));
        }
    }

    @Test
    @DisplayName("Should delegate save with bidirectional mapping")
    void shouldDelegateSaveWithBidirectionalMapping() {
        try (MockedStatic<PositionMapper> mapperMock = mockStatic(PositionMapper.class)) {
            // Arrange
            mapperMock.when(() -> PositionMapper.toJpaEntity(position)).thenReturn(jpaEntity);
            when(jpaPositionRepository.save(jpaEntity)).thenReturn(jpaEntity);
            mapperMock.when(() -> PositionMapper.toDomainEntity(jpaEntity)).thenReturn(position);

            // Act
            Position result = repository.save(position);

            // Assert
            assertThat(result, is(sameInstance(position)));
        }
    }

    @Test
    @DisplayName("Should delegate delete with mapping")
    void shouldDelegateDeleteWithMapping() {
        try (MockedStatic<PositionMapper> mapperMock = mockStatic(PositionMapper.class)) {
            // Arrange
            mapperMock.when(() -> PositionMapper.toJpaEntity(position)).thenReturn(jpaEntity);

            // Act
            repository.delete(position);

            // Assert
            verify(jpaPositionRepository).delete(jpaEntity);
        }
    }

    @Test
    @DisplayName("Should delegate findAll with stream mapping")
    void shouldDelegateFindAllWithStreamMapping() {
        try (MockedStatic<PositionMapper> mapperMock = mockStatic(PositionMapper.class)) {
            // Arrange
            when(jpaPositionRepository.findAll()).thenReturn(List.of(jpaEntity));
            mapperMock.when(() -> PositionMapper.toDomainEntity(jpaEntity)).thenReturn(position);

            // Act
            List<Position> result = repository.findAll();

            // Assert
            assertThat(result, hasSize(1));
        }
    }

    @Test
    @DisplayName("Should delegate findByTitleAndPositionLevel with mapping")
    void shouldDelegateFindByTitleAndPositionLevelWithMapping() {
        try (MockedStatic<PositionMapper> mapperMock = mockStatic(PositionMapper.class)) {
            // Arrange
            String title = "Software Engineer";
            PositionLevel level = PositionLevel.SENIOR;
            when(jpaPositionRepository.findByTitleAndPositionLevel(title, level))
                .thenReturn(Optional.of(jpaEntity));
            mapperMock.when(() -> PositionMapper.toDomainEntity(jpaEntity)).thenReturn(position);

            // Act
            Optional<Position> result = repository.findByTitleAndPositionLevel(title, level);

            // Assert
            assertThat(result.isPresent(), is(true));
            assertThat(result.get(), is(sameInstance(position)));
        }
    }

    @Test
    @DisplayName("Should return empty for not found operations")
    void shouldReturnEmptyForNotFoundOperations() {
        // Arrange
        UUID positionId = UUID.randomUUID();
        when(jpaPositionRepository.findById(positionId)).thenReturn(Optional.empty());
        when(jpaPositionRepository.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThat(repository.findById(positionId).isEmpty(), is(true));
        assertThat(repository.findAll(), is(empty()));
    }
}
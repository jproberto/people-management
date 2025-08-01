package com.itau.hr.people_management.unit.infrastructure.shared.mapper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.domain.position.entity.Position;
import com.itau.hr.people_management.domain.position.enumeration.PositionLevel;
import com.itau.hr.people_management.infrastructure.position.entity.PositionJpaEntity;
import com.itau.hr.people_management.infrastructure.shared.mapper.PositionMapper;

@ExtendWith(MockitoExtension.class)
@DisplayName("PositionMapper Unit Tests")
class PositionMapperTest {

    @Mock
    private Position domainPosition;

    @Mock
    private PositionJpaEntity jpaPosition;

    private UUID positionId;
    private String title;
    private PositionLevel positionLevel;

    @BeforeEach
    void setUp() {
        positionId = UUID.randomUUID();
        title = "Senior Software Engineer";
        positionLevel = PositionLevel.SENIOR;
    }

    @Test
    @DisplayName("Should map domain to JPA entity")
    void shouldMapDomainToJpaEntity() {
        // Arrange
        when(domainPosition.getId()).thenReturn(positionId);
        when(domainPosition.getTitle()).thenReturn(title);
        when(domainPosition.getPositionLevel()).thenReturn(positionLevel);

        // Act
        PositionJpaEntity result = PositionMapper.toJpaEntity(domainPosition);

        // Assert
        assertThat(result.getId(), is(positionId));
        assertThat(result.getTitle(), is(title));
        assertThat(result.getPositionLevel(), is(positionLevel));
    }

    @Test
    @DisplayName("Should map JPA entity to domain")
    void shouldMapJpaEntityToDomain() {
        // Arrange
        when(jpaPosition.getId()).thenReturn(positionId);
        when(jpaPosition.getTitle()).thenReturn(title);
        when(jpaPosition.getPositionLevel()).thenReturn(positionLevel);

        // Act
        Position result = PositionMapper.toDomainEntity(jpaPosition);

        // Assert
        assertThat(result.getId(), is(positionId));
        assertThat(result.getTitle(), is(title));
        assertThat(result.getPositionLevel(), is(positionLevel));
    }

    @Test
    @DisplayName("Should handle null inputs")
    void shouldHandleNullInputs() {
        // Act & Assert
        assertThat(PositionMapper.toJpaEntity(null), is(nullValue()));
        assertThat(PositionMapper.toDomainEntity(null), is(nullValue()));
    }
}
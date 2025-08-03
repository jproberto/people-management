package com.itau.hr.people_management.unit.application.position.usecase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.application.position.dto.PositionResponse;
import com.itau.hr.people_management.application.position.usecase.GetPositionUseCase;
import com.itau.hr.people_management.domain.position.entity.Position;
import com.itau.hr.people_management.domain.position.repository.PositionRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetPositionUseCase Unit Tests")
class GetPositionUseCaseTest {

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private Position position;

    private GetPositionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetPositionUseCase(positionRepository);
    }

    @Test
    @DisplayName("Should return empty list when no positions exist")
    void shouldReturnEmptyListWhenNoPositionsExist() {
        // Arrange
        when(positionRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<PositionResponse> result = useCase.getAll();

        // Assert
        assertThat(result, is(empty()));
        verify(positionRepository).findAll();
    }

    @Test
    @DisplayName("Should return mapped position responses")
    void shouldReturnMappedPositionResponses() {
        // Arrange
        UUID positionId = UUID.randomUUID();
        when(position.getId()).thenReturn(positionId);
        when(positionRepository.findAll()).thenReturn(List.of(position));

        // Act
        List<PositionResponse> result = useCase.getAll();

        // Assert
        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), is(positionId));
        verify(positionRepository).findAll();
    }

    @Test
    @DisplayName("Should preserve order from repository")
    void shouldPreserveOrderFromRepository() {
        // Arrange
        Position position1 = mock(Position.class);
        Position position2 = mock(Position.class);
        
        when(position1.getId()).thenReturn(UUID.randomUUID());
        when(position1.getTitle()).thenReturn("First");
        
        when(position2.getId()).thenReturn(UUID.randomUUID());
        when(position2.getTitle()).thenReturn("Second");
        
        when(positionRepository.findAll()).thenReturn(List.of(position1, position2));

        // Act
        List<PositionResponse> result = useCase.getAll();

        // Assert
        assertThat(result, hasSize(2));
        assertThat(result.get(0).getTitle(), is("First"));
        assertThat(result.get(1).getTitle(), is("Second"));
    }
}
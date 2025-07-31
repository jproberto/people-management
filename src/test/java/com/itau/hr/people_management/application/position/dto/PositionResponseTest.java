package com.itau.hr.people_management.application.position.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.domain.position.entity.Position;
import com.itau.hr.people_management.domain.position.enumeration.PositionLevel;

@ExtendWith(MockitoExtension.class)
@DisplayName("PositionResponse Unit Tests")
class PositionResponseTest {

    @Mock
    private Position position;

    @Mock
    private PositionLevel positionLevel;

    @Test
    @DisplayName("Should create response with complete position data")
    void shouldCreateResponseWithCompletePositionData() {
        // Arrange
        UUID positionId = UUID.randomUUID();
        String title = "Senior Software Engineer";
        String levelDisplayName = "Senior Level";
        
        when(position.getId()).thenReturn(positionId);
        when(position.getTitle()).thenReturn(title);
        when(position.getPositionLevel()).thenReturn(positionLevel);
        when(positionLevel.getDisplayName()).thenReturn(levelDisplayName);

        // Act
        PositionResponse response = new PositionResponse(position);

        // Assert
        assertThat(response.getId(), is(positionId));
        assertThat(response.getTitle(), is(title));
        assertThat(response.getPositionLevelName(), is(levelDisplayName));
    }

    @Test
    @DisplayName("Should handle null position level")
    void shouldHandleNullPositionLevel() {
        // Arrange
        UUID positionId = UUID.randomUUID();
        String title = "Software Engineer";
        
        when(position.getId()).thenReturn(positionId);
        when(position.getTitle()).thenReturn(title);
        when(position.getPositionLevel()).thenReturn(null);

        // Act
        PositionResponse response = new PositionResponse(position);

        // Assert
        assertThat(response.getId(), is(positionId));
        assertThat(response.getTitle(), is(title));
        assertThat(response.getPositionLevelName(), is(nullValue()));
    }

    @Test
    @DisplayName("Should handle null display name from position level")
    void shouldHandleNullDisplayNameFromPositionLevel() {
        // Arrange
        UUID positionId = UUID.randomUUID();
        String title = "Software Engineer";
        
        when(position.getId()).thenReturn(positionId);
        when(position.getTitle()).thenReturn(title);
        when(position.getPositionLevel()).thenReturn(positionLevel);
        when(positionLevel.getDisplayName()).thenReturn(null);

        // Act
        PositionResponse response = new PositionResponse(position);

        // Assert
        assertThat(response.getPositionLevelName(), is(nullValue()));
    }

    @Test
    @DisplayName("Should handle null values from position")
    void shouldHandleNullValuesFromPosition() {
        // Arrange
        when(position.getId()).thenReturn(null);
        when(position.getTitle()).thenReturn(null);
        when(position.getPositionLevel()).thenReturn(null);

        // Act
        PositionResponse response = new PositionResponse(position);

        // Assert
        assertThat(response.getId(), is(nullValue()));
        assertThat(response.getTitle(), is(nullValue()));
        assertThat(response.getPositionLevelName(), is(nullValue()));
    }

    @Test
    @DisplayName("Should throw exception when position is null")
    void shouldThrowExceptionWhenPositionIsNull() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new PositionResponse(null);
        });
        
        assertThat(exception.getMessage(), is("Position cannot be null"));
    }
}
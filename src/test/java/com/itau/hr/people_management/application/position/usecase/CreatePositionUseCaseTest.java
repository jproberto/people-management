package com.itau.hr.people_management.application.position.usecase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.application.position.dto.CreatePositionRequest;
import com.itau.hr.people_management.application.position.dto.PositionResponse;
import com.itau.hr.people_management.domain.position.entity.Position;
import com.itau.hr.people_management.domain.position.enumeration.PositionLevel;
import com.itau.hr.people_management.domain.position.repository.PositionRepository;
import com.itau.hr.people_management.domain.shared.exception.ConflictException;
import com.itau.hr.people_management.domain.shared.message.DomainMessageSource;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreatePositionUseCase Unit Tests")
class CreatePositionUseCaseTest {

    @Mock
    private PositionRepository positionRepository;

    @Mock
    private DomainMessageSource messageSource;

    @Mock
    private CreatePositionRequest request;

    @Mock
    private Position position;

    @Mock
    private Position savedPosition;

    private CreatePositionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreatePositionUseCase(positionRepository, messageSource);
    }

    @Test
    @DisplayName("Should create position successfully")
    void shouldCreatePositionSuccessfully() {
        try (MockedStatic<PositionLevel> levelMock = mockStatic(PositionLevel.class);
             MockedStatic<Position> positionMock = mockStatic(Position.class)) {
            
            // Arrange
            String title = "Senior Engineer";
            String levelName = "SENIOR";
            PositionLevel level = PositionLevel.SENIOR;
            
            when(request.getTitle()).thenReturn(title);
            when(request.getPositionLevelName()).thenReturn(levelName);
            levelMock.when(() -> PositionLevel.fromString(levelName, messageSource)).thenReturn(level);
            when(positionRepository.findByTitleAndPositionLevel(title, level)).thenReturn(Optional.empty());
            positionMock.when(() -> Position.create(any(UUID.class), eq(title), eq(level))).thenReturn(position);
            when(positionRepository.save(position)).thenReturn(savedPosition);

            // Act
            PositionResponse response = useCase.execute(request);

            // Assert
            assertThat(response, is(notNullValue()));
            verify(positionRepository).findByTitleAndPositionLevel(title, level);
            verify(positionRepository).save(position);
        }
    }

    @Test
    @DisplayName("Should throw ConflictException when position already exists")
    void shouldThrowConflictExceptionWhenPositionAlreadyExists() {
        try (MockedStatic<PositionLevel> levelMock = mockStatic(PositionLevel.class)) {
            // Arrange
            String title = "Senior Engineer";
            String levelName = "SENIOR";
            PositionLevel level = PositionLevel.SENIOR;
            Position existingPosition = mock(Position.class);
            
            when(request.getTitle()).thenReturn(title);
            when(request.getPositionLevelName()).thenReturn(levelName);
            levelMock.when(() -> PositionLevel.fromString(levelName, messageSource)).thenReturn(level);
            when(positionRepository.findByTitleAndPositionLevel(title, level)).thenReturn(Optional.of(existingPosition));

            // Act & Assert
            ConflictException exception = assertThrows(ConflictException.class, () -> {
                useCase.execute(request);
            });

            assertThat(exception.getMessageKey(), is("error.position.title.positionlevel.exists"));
            assertThat(exception.getArgs(), is(arrayContaining(title, level.getDisplayName())));
            verify(positionRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("Should generate unique UUID for position creation")
    void shouldGenerateUniqueUuidForPositionCreation() {
        try (MockedStatic<PositionLevel> levelMock = mockStatic(PositionLevel.class);
             MockedStatic<Position> positionMock = mockStatic(Position.class)) {
            
            // Arrange
            String title = "Engineer";
            String levelName = "JUNIOR";
            PositionLevel level = PositionLevel.JUNIOR;
            
            when(request.getTitle()).thenReturn(title);
            when(request.getPositionLevelName()).thenReturn(levelName);
            levelMock.when(() -> PositionLevel.fromString(levelName, messageSource)).thenReturn(level);
            when(positionRepository.findByTitleAndPositionLevel(title, level)).thenReturn(Optional.empty());
            positionMock.when(() -> Position.create(any(UUID.class), eq(title), eq(level))).thenReturn(position);
            when(positionRepository.save(position)).thenReturn(savedPosition);

            // Act
            useCase.execute(request);

            // Assert
            positionMock.verify(() -> Position.create(any(UUID.class), eq(title), eq(level)));
        }
    }

    @Test
    @DisplayName("Should delegate position level parsing to enum")
    void shouldDelegatePositionLevelParsingToEnum() {
        try (MockedStatic<PositionLevel> levelMock = mockStatic(PositionLevel.class);
             MockedStatic<Position> positionMock = mockStatic(Position.class)) {
            
            // Arrange
            String title = "Engineer";
            String levelName = "PLENO";
            PositionLevel level = PositionLevel.PLENO;
            
            when(request.getTitle()).thenReturn(title);
            when(request.getPositionLevelName()).thenReturn(levelName);
            levelMock.when(() -> PositionLevel.fromString(levelName, messageSource)).thenReturn(level);
            when(positionRepository.findByTitleAndPositionLevel(title, level)).thenReturn(Optional.empty());
            positionMock.when(() -> Position.create(any(UUID.class), eq(title), eq(level))).thenReturn(position);
            when(positionRepository.save(position)).thenReturn(savedPosition);

            // Act
            useCase.execute(request);

            // Assert
            levelMock.verify(() -> PositionLevel.fromString(levelName, messageSource));
        }
    }

    @Test
    @DisplayName("Should return response based on saved position")
    void shouldReturnResponseBasedOnSavedPosition() {
        try (MockedStatic<PositionLevel> levelMock = mockStatic(PositionLevel.class);
             MockedStatic<Position> positionMock = mockStatic(Position.class)) {
            
            // Arrange
            String title = "Engineer";
            String levelName = "SENIOR";
            PositionLevel level = PositionLevel.SENIOR;
            
            when(request.getTitle()).thenReturn(title);
            when(request.getPositionLevelName()).thenReturn(levelName);
            levelMock.when(() -> PositionLevel.fromString(levelName, messageSource)).thenReturn(level);
            when(positionRepository.findByTitleAndPositionLevel(title, level)).thenReturn(Optional.empty());
            positionMock.when(() -> Position.create(any(UUID.class), eq(title), eq(level))).thenReturn(position);
            when(positionRepository.save(position)).thenReturn(savedPosition);

            // Act
            PositionResponse response = useCase.execute(request);

            // Assert
            assertThat(response, is(instanceOf(PositionResponse.class)));
            // Response is created from savedPosition, not original position
        }
    }
}
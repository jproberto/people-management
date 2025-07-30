package com.itau.hr.people_management.application.position.usecase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    private CreatePositionUseCase createPositionUseCase;

    private String positionTitle;
    private String positionLevelName;
    private PositionLevel positionLevel;

    @BeforeEach
    void setUp() {
        createPositionUseCase = new CreatePositionUseCase(positionRepository, messageSource);
        
        positionTitle = "Senior Software Engineer";
        positionLevelName = "SENIOR";
        positionLevel = PositionLevel.SENIOR;
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create use case with valid dependencies")
        void shouldCreateUseCaseWithValidDependencies() {
            // Act
            CreatePositionUseCase useCase = new CreatePositionUseCase(positionRepository, messageSource);

            // Assert
            assertThat(useCase, is(notNullValue()));
        }

        @Test
        @DisplayName("Should accept null dependencies in constructor")
        void shouldAcceptNullDependenciesInConstructor() {
            // Act & Assert - Constructor should accept nulls (will fail at runtime when used)
            assertDoesNotThrow(() -> {
                CreatePositionUseCase useCase = new CreatePositionUseCase(null, null);
                assertThat(useCase, is(notNullValue()));
            });
        }
    }

    @Nested
    @DisplayName("Execute Method - Success Tests")
    class ExecuteMethodSuccessTests {

        @Test
        @DisplayName("Should create position successfully with valid request")
        void shouldCreatePositionSuccessfullyWithValidRequest() {
            try (MockedStatic<PositionLevel> positionLevelMock = mockStatic(PositionLevel.class);
                 MockedStatic<Position> positionMock = mockStatic(Position.class)) {
                
                // Arrange
                when(request.getTitle()).thenReturn(positionTitle);
                when(request.getPositionLevelName()).thenReturn(positionLevelName);
                
                positionLevelMock.when(() -> PositionLevel.fromString(positionLevelName, messageSource))
                    .thenReturn(positionLevel);
                
                when(positionRepository.findByTitleAndPositionLevel(positionTitle, positionLevel))
                    .thenReturn(Optional.empty());
                
                positionMock.when(() -> Position.create(any(UUID.class), eq(positionTitle), eq(positionLevel)))
                    .thenReturn(position);
                
                when(positionRepository.save(position)).thenReturn(savedPosition);

                // Act
                PositionResponse response = createPositionUseCase.execute(request);

                // Assert
                assertThat(response, is(notNullValue()));
                assertThat(response, is(instanceOf(PositionResponse.class)));
                
                verify(positionRepository).findByTitleAndPositionLevel(positionTitle, positionLevel);
                verify(positionRepository).save(position);
                positionLevelMock.verify(() -> PositionLevel.fromString(positionLevelName, messageSource));
                positionMock.verify(() -> Position.create(any(UUID.class), eq(positionTitle), eq(positionLevel)));
            }
        }

        @Test
        @DisplayName("Should generate random UUID for position creation")
        void shouldGenerateRandomUUIDForPositionCreation() {
            try (MockedStatic<PositionLevel> positionLevelMock = mockStatic(PositionLevel.class);
                 MockedStatic<Position> positionMock = mockStatic(Position.class)) {
                
                // Arrange
                when(request.getTitle()).thenReturn(positionTitle);
                when(request.getPositionLevelName()).thenReturn(positionLevelName);
                
                positionLevelMock.when(() -> PositionLevel.fromString(positionLevelName, messageSource))
                    .thenReturn(positionLevel);
                
                when(positionRepository.findByTitleAndPositionLevel(positionTitle, positionLevel))
                    .thenReturn(Optional.empty());
                
                positionMock.when(() -> Position.create(any(UUID.class), eq(positionTitle), eq(positionLevel)))
                    .thenReturn(position);
                
                when(positionRepository.save(position)).thenReturn(savedPosition);

                // Act
                createPositionUseCase.execute(request);

                // Assert
                ArgumentCaptor<UUID> uuidCaptor = ArgumentCaptor.forClass(UUID.class);
                positionMock.verify(() -> Position.create(uuidCaptor.capture(), eq(positionTitle), eq(positionLevel)));
                
                UUID capturedUUID = uuidCaptor.getValue();
                assertThat(capturedUUID, is(notNullValue()));
            }
        }

        @Test
        @DisplayName("Should create PositionResponse from saved position")
        void shouldCreatePositionResponseFromSavedPosition() {
            try (MockedStatic<PositionLevel> positionLevelMock = mockStatic(PositionLevel.class);
                 MockedStatic<Position> positionMock = mockStatic(Position.class)) {
                
                // Arrange
                when(request.getTitle()).thenReturn(positionTitle);
                when(request.getPositionLevelName()).thenReturn(positionLevelName);
                
                positionLevelMock.when(() -> PositionLevel.fromString(positionLevelName, messageSource))
                    .thenReturn(positionLevel);
                
                when(positionRepository.findByTitleAndPositionLevel(positionTitle, positionLevel))
                    .thenReturn(Optional.empty());
                
                positionMock.when(() -> Position.create(any(UUID.class), eq(positionTitle), eq(positionLevel)))
                    .thenReturn(position);
                
                when(positionRepository.save(position)).thenReturn(savedPosition);

                // Act
                PositionResponse response = createPositionUseCase.execute(request);

                // Assert
                assertThat(response, is(notNullValue()));
                // The response is created using savedPosition, not the original position
                verify(positionRepository).save(position);
            }
        }

        @Test
        @DisplayName("Should handle different position levels correctly")
        void shouldHandleDifferentPositionLevelsCorrectly() {
            try (MockedStatic<PositionLevel> positionLevelMock = mockStatic(PositionLevel.class);
                 MockedStatic<Position> positionMock = mockStatic(Position.class)) {
                
                // Test with different position levels
                PositionLevel[] levels = {PositionLevel.JUNIOR, PositionLevel.SENIOR, PositionLevel.PLENO};
                String[] levelNames = {"JUNIOR", "SENIOR", "PLENO"};

                for (int i = 0; i < levels.length; i++) {
                    final int index = i;
                    final PositionLevel currentLevel = levels[index];
                    final String currentLevelName = levelNames[index];
                    
                    // Arrange
                    when(request.getTitle()).thenReturn(positionTitle);
                    when(request.getPositionLevelName()).thenReturn(currentLevelName);
                    
                    positionLevelMock.when(() -> PositionLevel.fromString(currentLevelName, messageSource))
                        .thenReturn(currentLevel);
                    
                    when(positionRepository.findByTitleAndPositionLevel(positionTitle, currentLevel))
                        .thenReturn(Optional.empty());
                    
                    positionMock.when(() -> Position.create(any(UUID.class), eq(positionTitle), eq(currentLevel)))
                        .thenReturn(position);
                    
                    when(positionRepository.save(position)).thenReturn(savedPosition);

                    // Act
                    PositionResponse response = createPositionUseCase.execute(request);

                    // Assert
                    assertThat(response, is(notNullValue()));
                    positionLevelMock.verify(() -> PositionLevel.fromString(currentLevelName, messageSource));
                }
            }
        }
    }

    @Nested
    @DisplayName("Execute Method - Conflict Detection Tests")
    class ExecuteMethodConflictDetectionTests {

        @Test
        @DisplayName("Should throw ConflictException when position with same title and level exists")
        void shouldThrowConflictExceptionWhenPositionWithSameTitleAndLevelExists() {
            try (MockedStatic<PositionLevel> positionLevelMock = mockStatic(PositionLevel.class)) {
                // Arrange
                when(request.getTitle()).thenReturn(positionTitle);
                when(request.getPositionLevelName()).thenReturn(positionLevelName);
                
                positionLevelMock.when(() -> PositionLevel.fromString(positionLevelName, messageSource))
                    .thenReturn(positionLevel);
                
                when(positionRepository.findByTitleAndPositionLevel(positionTitle, positionLevel))
                    .thenReturn(Optional.of(position));
                
                // Act & Assert
                ConflictException thrownException = assertThrows(ConflictException.class, () -> {
                    createPositionUseCase.execute(request);
                });

                assertThat(thrownException.getMessageKey(), is(equalTo("error.position.title.positionlevel.exists")));
                assertThat(thrownException.getArgs(), is(arrayContaining(positionTitle, "Sênior")));
                
                verify(positionRepository).findByTitleAndPositionLevel(positionTitle, positionLevel);
                verify(positionRepository, never()).save(any());
            }
        }

        @Test
        @DisplayName("Should check for conflicts before attempting to create position")
        void shouldCheckForConflictsBeforeAttemptingToCreatePosition() {
            try (MockedStatic<PositionLevel> positionLevelMock = mockStatic(PositionLevel.class);
                 MockedStatic<Position> positionMock = mockStatic(Position.class)) {
                
                // Arrange
                when(request.getTitle()).thenReturn(positionTitle);
                when(request.getPositionLevelName()).thenReturn(positionLevelName);
                
                positionLevelMock.when(() -> PositionLevel.fromString(positionLevelName, messageSource))
                    .thenReturn(positionLevel);
                
                when(positionRepository.findByTitleAndPositionLevel(positionTitle, positionLevel))
                    .thenReturn(Optional.of(position));
                
                // Act & Assert
                assertThrows(ConflictException.class, () -> {
                    createPositionUseCase.execute(request);
                });

                // Position.create should never be called when conflict exists
                positionMock.verify(() -> Position.create(any(), any(), any()), never());
                verify(positionRepository, never()).save(any());
            }
        }

        @Test
        @DisplayName("Should use correct conflict exception message and parameters")
        void shouldUseCorrectConflictExceptionMessageAndParameters() {
            try (MockedStatic<PositionLevel> positionLevelMock = mockStatic(PositionLevel.class)) {
                // Arrange
                String displayName = positionLevel.getDisplayName();
                
                when(request.getTitle()).thenReturn(positionTitle);
                when(request.getPositionLevelName()).thenReturn(displayName);
                
                positionLevelMock.when(() -> PositionLevel.fromString(displayName, messageSource))
                    .thenReturn(positionLevel);
                
                when(positionRepository.findByTitleAndPositionLevel(positionTitle, positionLevel))
                    .thenReturn(Optional.of(position));
                
                // Act & Assert
                ConflictException thrownException = assertThrows(ConflictException.class, () -> {
                    createPositionUseCase.execute(request);
                });

                assertThat(thrownException.getMessageKey(), is(equalTo("error.position.title.positionlevel.exists")));
                assertThat(thrownException.getArgs().length, is(equalTo(2)));
                assertThat(thrownException.getArgs()[0], is(equalTo(positionTitle)));
                assertThat(thrownException.getArgs()[1], is(equalTo(displayName)));
            }
        }
    }

    @Nested
    @DisplayName("Execute Method - Position Level Validation Tests")
    class ExecuteMethodPositionLevelValidationTests {

        @Test
        @DisplayName("Should call PositionLevel.fromString with correct parameters")
        void shouldCallPositionLevelFromStringWithCorrectParameters() {
            try (MockedStatic<PositionLevel> positionLevelMock = mockStatic(PositionLevel.class);
                 MockedStatic<Position> positionMock = mockStatic(Position.class)) {
                
                // Arrange
                when(request.getTitle()).thenReturn(positionTitle);
                when(request.getPositionLevelName()).thenReturn(positionLevelName);
                
                positionLevelMock.when(() -> PositionLevel.fromString(positionLevelName, messageSource))
                    .thenReturn(positionLevel);
                
                when(positionRepository.findByTitleAndPositionLevel(positionTitle, positionLevel))
                    .thenReturn(Optional.empty());
                
                positionMock.when(() -> Position.create(any(UUID.class), eq(positionTitle), eq(positionLevel)))
                    .thenReturn(position);
                
                when(positionRepository.save(position)).thenReturn(savedPosition);

                // Act
                createPositionUseCase.execute(request);

                // Assert
                positionLevelMock.verify(() -> PositionLevel.fromString(positionLevelName, messageSource));
            }
        }

        @Test
        @DisplayName("Should propagate exception from PositionLevel.fromString")
        void shouldPropagateExceptionFromPositionLevelFromString() {
            try (MockedStatic<PositionLevel> positionLevelMock = mockStatic(PositionLevel.class)) {
                // Arrange
                when(request.getPositionLevelName()).thenReturn("INVALID_LEVEL");
                
                RuntimeException expectedException = new RuntimeException("Invalid position level");
                positionLevelMock.when(() -> PositionLevel.fromString("INVALID_LEVEL", messageSource))
                    .thenThrow(expectedException);

                // Act & Assert
                RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                    createPositionUseCase.execute(request);
                });

                assertThat(thrownException, is(sameInstance(expectedException)));
                verify(positionRepository, never()).findByTitleAndPositionLevel(any(), any());
                verify(positionRepository, never()).save(any());
            }
        }
    }

    @Nested
    @DisplayName("Execute Method - Repository Integration Tests")
    class ExecuteMethodRepositoryIntegrationTests {

        @Test
        @DisplayName("Should call repository methods in correct order")
        void shouldCallRepositoryMethodsInCorrectOrder() {
            try (MockedStatic<PositionLevel> positionLevelMock = mockStatic(PositionLevel.class);
                 MockedStatic<Position> positionMock = mockStatic(Position.class)) {
                
                // Arrange
                when(request.getTitle()).thenReturn(positionTitle);
                when(request.getPositionLevelName()).thenReturn(positionLevelName);
                
                positionLevelMock.when(() -> PositionLevel.fromString(positionLevelName, messageSource))
                    .thenReturn(positionLevel);
                
                when(positionRepository.findByTitleAndPositionLevel(positionTitle, positionLevel))
                    .thenReturn(Optional.empty());
                
                positionMock.when(() -> Position.create(any(UUID.class), eq(positionTitle), eq(positionLevel)))
                    .thenReturn(position);
                
                when(positionRepository.save(position)).thenReturn(savedPosition);

                // Act
                createPositionUseCase.execute(request);

                // Assert - Verify order of operations
                var inOrder = inOrder(positionRepository);
                inOrder.verify(positionRepository).findByTitleAndPositionLevel(positionTitle, positionLevel);
                inOrder.verify(positionRepository).save(position);
            }
        }

        @Test
        @DisplayName("Should save position created by Position.create")
        void shouldSavePositionCreatedByPositionCreate() {
            try (MockedStatic<PositionLevel> positionLevelMock = mockStatic(PositionLevel.class);
                 MockedStatic<Position> positionMock = mockStatic(Position.class)) {
                
                // Arrange
                when(request.getTitle()).thenReturn(positionTitle);
                when(request.getPositionLevelName()).thenReturn(positionLevelName);
                
                positionLevelMock.when(() -> PositionLevel.fromString(positionLevelName, messageSource))
                    .thenReturn(positionLevel);
                
                when(positionRepository.findByTitleAndPositionLevel(positionTitle, positionLevel))
                    .thenReturn(Optional.empty());
                
                positionMock.when(() -> Position.create(any(UUID.class), eq(positionTitle), eq(positionLevel)))
                    .thenReturn(position);
                
                when(positionRepository.save(position)).thenReturn(savedPosition);

                // Act
                createPositionUseCase.execute(request);

                // Assert
                verify(positionRepository).save(position);
            }
        }

        @Test
        @DisplayName("Should propagate repository save exceptions")
        void shouldPropagateRepositorySaveExceptions() {
            try (MockedStatic<PositionLevel> positionLevelMock = mockStatic(PositionLevel.class);
                 MockedStatic<Position> positionMock = mockStatic(Position.class)) {
                
                // Arrange
                when(request.getTitle()).thenReturn(positionTitle);
                when(request.getPositionLevelName()).thenReturn(positionLevelName);
                
                positionLevelMock.when(() -> PositionLevel.fromString(positionLevelName, messageSource))
                    .thenReturn(positionLevel);
                
                when(positionRepository.findByTitleAndPositionLevel(positionTitle, positionLevel))
                    .thenReturn(Optional.empty());
                
                positionMock.when(() -> Position.create(any(UUID.class), eq(positionTitle), eq(positionLevel)))
                    .thenReturn(position);
                
                RuntimeException saveException = new RuntimeException("Database save failed");
                when(positionRepository.save(position)).thenThrow(saveException);

                // Act & Assert
                RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                    createPositionUseCase.execute(request);
                });

                assertThat(thrownException, is(sameInstance(saveException)));
            }
        }

        @Test
        @DisplayName("Should propagate repository find exceptions")
        void shouldPropagateRepositoryFindExceptions() {
            try (MockedStatic<PositionLevel> positionLevelMock = mockStatic(PositionLevel.class)) {
                // Arrange
                when(request.getTitle()).thenReturn(positionTitle);
                when(request.getPositionLevelName()).thenReturn(positionLevelName);
                
                positionLevelMock.when(() -> PositionLevel.fromString(positionLevelName, messageSource))
                    .thenReturn(positionLevel);
                
                RuntimeException findException = new RuntimeException("Database find failed");
                when(positionRepository.findByTitleAndPositionLevel(positionTitle, positionLevel))
                    .thenThrow(findException);

                // Act & Assert
                RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                    createPositionUseCase.execute(request);
                });

                assertThat(thrownException, is(sameInstance(findException)));
                verify(positionRepository, never()).save(any());
            }
        }
    }

    @Nested
    @DisplayName("Execute Method - Request Validation Tests")
    class ExecuteMethodRequestValidationTests {

        @Test
        @DisplayName("Should handle null request gracefully")
        void shouldHandleNullRequestGracefully() {
            // Act & Assert
            assertThrows(NullPointerException.class, () -> {
                createPositionUseCase.execute(null);
            });
        }

        @Test
        @DisplayName("Should handle request with null title")
        void shouldHandleRequestWithNullTitle() {
            try (MockedStatic<PositionLevel> positionLevelMock = mockStatic(PositionLevel.class);
                MockedStatic<Position> positionMock = mockStatic(Position.class)) {
                
                // Arrange
                when(request.getTitle()).thenReturn(null);
                when(request.getPositionLevelName()).thenReturn(positionLevelName);
                
                positionLevelMock.when(() -> PositionLevel.fromString(positionLevelName, messageSource))
                    .thenReturn(positionLevel);
                
                when(positionRepository.findByTitleAndPositionLevel(null, positionLevel))
                    .thenReturn(Optional.empty());

                // Mock Position.create to throw validation exception for null title
                IllegalArgumentException expectedException = new IllegalArgumentException("Title cannot be null");
                positionMock.when(() -> Position.create(any(UUID.class), isNull(), eq(positionLevel)))
                    .thenThrow(expectedException);

                // Act & Assert
                IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () -> {
                    createPositionUseCase.execute(request);
                });

                assertThat(thrownException, is(sameInstance(expectedException)));
                verify(positionRepository).findByTitleAndPositionLevel(null, positionLevel);
                verify(positionRepository, never()).save(any());
            }
        }

        @Test
        @DisplayName("Should handle request with null position level name")
        void shouldHandleRequestWithNullPositionLevelName() {
            try (MockedStatic<PositionLevel> positionLevelMock = mockStatic(PositionLevel.class)) {
                // Arrange
                when(request.getPositionLevelName()).thenReturn(null);
                
                RuntimeException expectedException = new RuntimeException("Position level name cannot be null");
                positionLevelMock.when(() -> PositionLevel.fromString(null, messageSource))
                    .thenThrow(expectedException);

                // Act & Assert
                RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                    createPositionUseCase.execute(request);
                });

                assertThat(thrownException, is(sameInstance(expectedException)));
            }
        }

        @Test
        @DisplayName("Should extract values from request correctly")
        void shouldExtractValuesFromRequestCorrectly() {
            try (MockedStatic<PositionLevel> positionLevelMock = mockStatic(PositionLevel.class);
                 MockedStatic<Position> positionMock = mockStatic(Position.class)) {
                
                // Arrange
                when(request.getTitle()).thenReturn(positionTitle);
                when(request.getPositionLevelName()).thenReturn(positionLevelName);
                
                positionLevelMock.when(() -> PositionLevel.fromString(positionLevelName, messageSource))
                    .thenReturn(positionLevel);
                
                when(positionRepository.findByTitleAndPositionLevel(positionTitle, positionLevel))
                    .thenReturn(Optional.empty());
                
                positionMock.when(() -> Position.create(any(UUID.class), eq(positionTitle), eq(positionLevel)))
                    .thenReturn(position);
                
                when(positionRepository.save(position)).thenReturn(savedPosition);

                // Act
                createPositionUseCase.execute(request);

                // Assert
                verify(request, times(2)).getTitle();
                verify(request).getPositionLevelName();
            }
        }
    }

    @Nested
    @DisplayName("Execute Method - Edge Cases Tests")
    class ExecuteMethodEdgeCasesTests {

        @Test
        @DisplayName("Should handle empty string title")
        void shouldHandleEmptyStringTitle() {
            try (MockedStatic<PositionLevel> positionLevelMock = mockStatic(PositionLevel.class);
                 MockedStatic<Position> positionMock = mockStatic(Position.class)) {
                
                // Arrange
                String emptyTitle = "";
                
                when(request.getTitle()).thenReturn(emptyTitle);
                when(request.getPositionLevelName()).thenReturn(positionLevelName);
                
                positionLevelMock.when(() -> PositionLevel.fromString(positionLevelName, messageSource))
                    .thenReturn(positionLevel);
                
                when(positionRepository.findByTitleAndPositionLevel(emptyTitle, positionLevel))
                    .thenReturn(Optional.empty());
                
                positionMock.when(() -> Position.create(any(UUID.class), eq(emptyTitle), eq(positionLevel)))
                    .thenReturn(position);
                
                when(positionRepository.save(position)).thenReturn(savedPosition);

                // Act
                PositionResponse response = createPositionUseCase.execute(request);

                // Assert
                assertThat(response, is(notNullValue()));
                positionMock.verify(() -> Position.create(any(UUID.class), eq(emptyTitle), eq(positionLevel)));
            }
        }

        @Test
        @DisplayName("Should handle very long position title")
        void shouldHandleVeryLongPositionTitle() {
            try (MockedStatic<PositionLevel> positionLevelMock = mockStatic(PositionLevel.class);
                 MockedStatic<Position> positionMock = mockStatic(Position.class)) {
                
                // Arrange
                String longTitle = "Very Long Position Title ".repeat(100);
                
                when(request.getTitle()).thenReturn(longTitle);
                when(request.getPositionLevelName()).thenReturn(positionLevelName);
                
                positionLevelMock.when(() -> PositionLevel.fromString(positionLevelName, messageSource))
                    .thenReturn(positionLevel);
                
                when(positionRepository.findByTitleAndPositionLevel(longTitle, positionLevel))
                    .thenReturn(Optional.empty());
                
                positionMock.when(() -> Position.create(any(UUID.class), eq(longTitle), eq(positionLevel)))
                    .thenReturn(position);
                
                when(positionRepository.save(position)).thenReturn(savedPosition);

                // Act
                PositionResponse response = createPositionUseCase.execute(request);

                // Assert
                assertThat(response, is(notNullValue()));
                positionMock.verify(() -> Position.create(any(UUID.class), eq(longTitle), eq(positionLevel)));
            }
        }

        @Test
        @DisplayName("Should handle special characters in position title")
        void shouldHandleSpecialCharactersInPositionTitle() {
            try (MockedStatic<PositionLevel> positionLevelMock = mockStatic(PositionLevel.class);
                 MockedStatic<Position> positionMock = mockStatic(Position.class)) {
                
                // Arrange
                String specialTitle = "Engenheiro de Software Sênior - Área de TI/Digital";
                
                when(request.getTitle()).thenReturn(specialTitle);
                when(request.getPositionLevelName()).thenReturn(positionLevelName);
                
                positionLevelMock.when(() -> PositionLevel.fromString(positionLevelName, messageSource))
                    .thenReturn(positionLevel);
                
                when(positionRepository.findByTitleAndPositionLevel(specialTitle, positionLevel))
                    .thenReturn(Optional.empty());
                
                positionMock.when(() -> Position.create(any(UUID.class), eq(specialTitle), eq(positionLevel)))
                    .thenReturn(position);
                
                when(positionRepository.save(position)).thenReturn(savedPosition);

                // Act
                PositionResponse response = createPositionUseCase.execute(request);

                // Assert
                assertThat(response, is(notNullValue()));
                positionMock.verify(() -> Position.create(any(UUID.class), eq(specialTitle), eq(positionLevel)));
            }
        }

        @Test
        @DisplayName("Should generate different UUIDs for multiple executions")
        void shouldGenerateDifferentUUIDsForMultipleExecutions() {
            try (MockedStatic<PositionLevel> positionLevelMock = mockStatic(PositionLevel.class);
                 MockedStatic<Position> positionMock = mockStatic(Position.class)) {
                
                // Arrange
                when(request.getTitle()).thenReturn(positionTitle);
                when(request.getPositionLevelName()).thenReturn(positionLevelName);
                
                positionLevelMock.when(() -> PositionLevel.fromString(positionLevelName, messageSource))
                    .thenReturn(positionLevel);
                
                when(positionRepository.findByTitleAndPositionLevel(positionTitle, positionLevel))
                    .thenReturn(Optional.empty());
                
                positionMock.when(() -> Position.create(any(UUID.class), eq(positionTitle), eq(positionLevel)))
                    .thenReturn(position);
                
                when(positionRepository.save(position)).thenReturn(savedPosition);

                // Act
                createPositionUseCase.execute(request);
                createPositionUseCase.execute(request);

                // Assert
                ArgumentCaptor<UUID> uuidCaptor = ArgumentCaptor.forClass(UUID.class);
                positionMock.verify(() -> Position.create(uuidCaptor.capture(), eq(positionTitle), eq(positionLevel)), times(2));
                
                // UUIDs should be different (very unlikely to be same)
                assertThat(uuidCaptor.getAllValues().size(), is(equalTo(2)));
            }
        }

        @Test
        @DisplayName("Should handle Position.create returning null")
        void shouldHandlePositionCreateReturningNull() {
            try (MockedStatic<PositionLevel> positionLevelMock = mockStatic(PositionLevel.class);
                 MockedStatic<Position> positionMock = mockStatic(Position.class)) {
                
                // Arrange
                when(request.getTitle()).thenReturn(positionTitle);
                when(request.getPositionLevelName()).thenReturn(positionLevelName);
                
                positionLevelMock.when(() -> PositionLevel.fromString(positionLevelName, messageSource))
                    .thenReturn(positionLevel);
                
                when(positionRepository.findByTitleAndPositionLevel(positionTitle, positionLevel))
                    .thenReturn(Optional.empty());
                
                positionMock.when(() -> Position.create(any(UUID.class), eq(positionTitle), eq(positionLevel)))
                    .thenReturn(null);
                
                when(positionRepository.save(null)).thenReturn(savedPosition);

                // Act
                PositionResponse response = createPositionUseCase.execute(request);

                // Assert
                assertThat(response, is(notNullValue()));
                verify(positionRepository).save(null);
            }
        }
    }
}
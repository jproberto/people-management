package com.itau.hr.people_management.application.position.dto;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    private UUID positionId;
    private String positionTitle;
    private String levelDisplayName;

    @BeforeEach
    void setUp() {
        positionId = UUID.randomUUID();
        positionTitle = "Senior Software Engineer";
        levelDisplayName = "Senior Level";
    }

    @Nested
    @DisplayName("Constructor with Position - Success Tests")
    class ConstructorWithPositionSuccessTests {

        @Test
        @DisplayName("Should create PositionResponse with complete position data")
        void shouldCreatePositionResponseWithCompletePositionData() {
            // Arrange
            when(position.getId()).thenReturn(positionId);
            when(position.getTitle()).thenReturn(positionTitle);
            when(position.getPositionLevel()).thenReturn(positionLevel);
            when(positionLevel.getDisplayName()).thenReturn(levelDisplayName);

            // Act
            PositionResponse response = new PositionResponse(position);

            // Assert
            assertThat(response.getId(), is(equalTo(positionId)));
            assertThat(response.getTitle(), is(equalTo(positionTitle)));
            assertThat(response.getPositionLevelName(), is(equalTo(levelDisplayName)));
        }

        @Test
        @DisplayName("Should create PositionResponse with minimal position data")
        void shouldCreatePositionResponseWithMinimalPositionData() {
            // Arrange
            when(position.getId()).thenReturn(positionId);
            when(position.getTitle()).thenReturn(positionTitle);
            when(position.getPositionLevel()).thenReturn(null);

            // Act
            PositionResponse response = new PositionResponse(position);

            // Assert
            assertThat(response.getId(), is(equalTo(positionId)));
            assertThat(response.getTitle(), is(equalTo(positionTitle)));
            assertThat(response.getPositionLevelName(), is(nullValue()));
        }

        @Test
        @DisplayName("Should call position getters exactly once")
        void shouldCallPositionGettersExactlyOnce() {
            // Arrange
            when(position.getId()).thenReturn(positionId);
            when(position.getTitle()).thenReturn(positionTitle);
            when(position.getPositionLevel()).thenReturn(positionLevel);
            when(positionLevel.getDisplayName()).thenReturn(levelDisplayName);

            // Act
            new PositionResponse(position);

            // Assert
            verify(position, times(1)).getId();
            verify(position, times(1)).getTitle();
            verify(position, times(2)).getPositionLevel();
            verify(positionLevel, times(1)).getDisplayName();
        }

        @Test
        @DisplayName("Should handle empty string title correctly")
        void shouldHandleEmptyStringTitleCorrectly() {
            // Arrange
            when(position.getId()).thenReturn(positionId);
            when(position.getTitle()).thenReturn("");
            when(position.getPositionLevel()).thenReturn(null);

            // Act
            PositionResponse response = new PositionResponse(position);

            // Assert
            assertThat(response.getTitle(), is(equalTo("")));
        }

        @Test
        @DisplayName("Should handle special characters in position title")
        void shouldHandleSpecialCharactersInPositionTitle() {
            // Arrange
            String specialTitle = "Engenheiro de Software Sênior - Área de TI/Digital";
            
            when(position.getId()).thenReturn(positionId);
            when(position.getTitle()).thenReturn(specialTitle);
            when(position.getPositionLevel()).thenReturn(null);

            // Act
            PositionResponse response = new PositionResponse(position);

            // Assert
            assertThat(response.getTitle(), is(equalTo(specialTitle)));
        }

        @Test
        @DisplayName("Should handle very long position title")
        void shouldHandleVeryLongPositionTitle() {
            // Arrange
            String longTitle = "Very Long Position Title ".repeat(50);
            
            when(position.getId()).thenReturn(positionId);
            when(position.getTitle()).thenReturn(longTitle);
            when(position.getPositionLevel()).thenReturn(null);

            // Act
            PositionResponse response = new PositionResponse(position);

            // Assert
            assertThat(response.getTitle(), is(equalTo(longTitle)));
        }

        @Test
        @DisplayName("Should extract display name from position level when present")
        void shouldExtractDisplayNameFromPositionLevelWhenPresent() {
            // Arrange
            when(position.getId()).thenReturn(positionId);
            when(position.getTitle()).thenReturn(positionTitle);
            when(position.getPositionLevel()).thenReturn(positionLevel);
            when(positionLevel.getDisplayName()).thenReturn(levelDisplayName);

            // Act
            PositionResponse response = new PositionResponse(position);

            // Assert
            assertThat(response.getPositionLevelName(), is(equalTo(levelDisplayName)));
            verify(position, times(2)).getPositionLevel();
            verify(positionLevel).getDisplayName();
        }

        @Test
        @DisplayName("Should handle null display name from position level")
        void shouldHandleNullDisplayNameFromPositionLevel() {
            // Arrange
            when(position.getId()).thenReturn(positionId);
            when(position.getTitle()).thenReturn(positionTitle);
            when(position.getPositionLevel()).thenReturn(positionLevel);
            when(positionLevel.getDisplayName()).thenReturn(null);

            // Act
            PositionResponse response = new PositionResponse(position);

            // Assert
            assertThat(response.getPositionLevelName(), is(nullValue()));
            verify(position, times(2)).getPositionLevel();
            verify(positionLevel).getDisplayName();
        }

        @Test
        @DisplayName("Should handle empty display name from position level")
        void shouldHandleEmptyDisplayNameFromPositionLevel() {
            // Arrange
            when(position.getId()).thenReturn(positionId);
            when(position.getTitle()).thenReturn(positionTitle);
            when(position.getPositionLevel()).thenReturn(positionLevel);
            when(positionLevel.getDisplayName()).thenReturn("");

            // Act
            PositionResponse response = new PositionResponse(position);

            // Assert
            assertThat(response.getPositionLevelName(), is(equalTo("")));
        }

        @Test
        @DisplayName("Should handle special characters in position level display name")
        void shouldHandleSpecialCharactersInPositionLevelDisplayName() {
            // Arrange
            String specialDisplayName = "Nível Sênior - C1/Principal";
            
            when(position.getId()).thenReturn(positionId);
            when(position.getTitle()).thenReturn(positionTitle);
            when(position.getPositionLevel()).thenReturn(positionLevel);
            when(positionLevel.getDisplayName()).thenReturn(specialDisplayName);

            // Act
            PositionResponse response = new PositionResponse(position);

            // Assert
            assertThat(response.getPositionLevelName(), is(equalTo(specialDisplayName)));
        }

        @Test
        @DisplayName("Should handle various position level display names")
        void shouldHandleVariousPositionLevelDisplayNames() {
            // Arrange
            String[] displayNames = {
                "Junior",
                "Mid-level", 
                "Senior",
                "Principal",
                "Staff",
                "Director",
                "VP",
                "C-Level"
            };

            when(position.getId()).thenReturn(positionId);
            when(position.getTitle()).thenReturn(positionTitle);
            when(position.getPositionLevel()).thenReturn(positionLevel);

            for (String displayName : displayNames) {
                // Arrange
                when(positionLevel.getDisplayName()).thenReturn(displayName);

                // Act
                PositionResponse response = new PositionResponse(position);

                // Assert
                assertThat(response.getPositionLevelName(), is(equalTo(displayName)));
            }
        }
    }

    @Nested
    @DisplayName("Constructor with Position - Null Handling Tests")
    class ConstructorWithPositionNullHandlingTests {

        @Test
        @DisplayName("Should throw IllegalArgumentException when position is null")
        void shouldThrowIllegalArgumentExceptionWhenPositionIsNull() {
            // Act & Assert
            IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () -> {
                new PositionResponse(null);
            });

            assertThat(thrownException.getMessage(), is(equalTo("Position cannot be null")));
        }

        @Test
        @DisplayName("Should handle null position level gracefully")
        void shouldHandleNullPositionLevelGracefully() {
            // Arrange
            when(position.getId()).thenReturn(positionId);
            when(position.getTitle()).thenReturn(positionTitle);
            when(position.getPositionLevel()).thenReturn(null);

            // Act
            PositionResponse response = new PositionResponse(position);

            // Assert
            assertThat(response.getPositionLevelName(), is(nullValue()));
            verify(position).getPositionLevel();
            verify(positionLevel, never()).getDisplayName();
        }

        @Test
        @DisplayName("Should handle null title gracefully")
        void shouldHandleNullTitleGracefully() {
            // Arrange
            when(position.getId()).thenReturn(positionId);
            when(position.getTitle()).thenReturn(null);
            when(position.getPositionLevel()).thenReturn(null);

            // Act
            PositionResponse response = new PositionResponse(position);

            // Assert
            assertThat(response.getId(), is(equalTo(positionId)));
            assertThat(response.getTitle(), is(nullValue()));
            assertThat(response.getPositionLevelName(), is(nullValue()));
        }

        @Test
        @DisplayName("Should handle null id gracefully")
        void shouldHandleNullIdGracefully() {
            // Arrange
            when(position.getId()).thenReturn(null);
            when(position.getTitle()).thenReturn(positionTitle);
            when(position.getPositionLevel()).thenReturn(null);

            // Act
            PositionResponse response = new PositionResponse(position);

            // Assert
            assertThat(response.getId(), is(nullValue()));
            assertThat(response.getTitle(), is(equalTo(positionTitle)));
            assertThat(response.getPositionLevelName(), is(nullValue()));
        }

        @Test
        @DisplayName("Should handle all null fields except position object")
        void shouldHandleAllNullFieldsExceptPositionObject() {
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
    }

    @Nested
    @DisplayName("Constructor with Position - Position Level Handling Tests")
    class ConstructorWithPositionPositionLevelHandlingTests {

        @Test
        @DisplayName("Should extract display name when position level is present")
        void shouldExtractDisplayNameWhenPositionLevelIsPresent() {
            // Arrange
            when(position.getId()).thenReturn(positionId);
            when(position.getTitle()).thenReturn(positionTitle);
            when(position.getPositionLevel()).thenReturn(positionLevel);
            when(positionLevel.getDisplayName()).thenReturn(levelDisplayName);

            // Act
            PositionResponse response = new PositionResponse(position);

            // Assert
            assertThat(response.getPositionLevelName(), is(equalTo(levelDisplayName)));
            verify(position, times(2)).getPositionLevel();
            verify(positionLevel).getDisplayName();
        }

        @Test
        @DisplayName("Should not extract display name when position level is null")
        void shouldNotExtractDisplayNameWhenPositionLevelIsNull() {
            // Arrange
            when(position.getId()).thenReturn(positionId);
            when(position.getTitle()).thenReturn(positionTitle);
            when(position.getPositionLevel()).thenReturn(null);

            // Act
            PositionResponse response = new PositionResponse(position);

            // Assert
            assertThat(response.getPositionLevelName(), is(nullValue()));
            verify(position).getPositionLevel();
            verify(positionLevel, never()).getDisplayName();
        }

        @Test
        @DisplayName("Should use getDisplayName method specifically")
        void shouldUseGetDisplayNameMethodSpecifically() {
            // Arrange
            when(position.getId()).thenReturn(positionId);
            when(position.getTitle()).thenReturn(positionTitle);
            when(position.getPositionLevel()).thenReturn(positionLevel);
            when(positionLevel.getDisplayName()).thenReturn(levelDisplayName);

            // Act
            PositionResponse response = new PositionResponse(position);

            // Assert
            assertThat(response.getPositionLevelName(), is(equalTo(levelDisplayName)));
            verify(positionLevel).getDisplayName();
            // Verifica que apenas getDisplayName() é chamado, não outros métodos do enum
            verifyNoMoreInteractions(positionLevel);
        }

        @Test
        @DisplayName("Should handle position level with very long display name")
        void shouldHandlePositionLevelWithVeryLongDisplayName() {
            // Arrange
            String longDisplayName = "Very Long Position Level Display Name ".repeat(20);
            
            when(position.getId()).thenReturn(positionId);
            when(position.getTitle()).thenReturn(positionTitle);
            when(position.getPositionLevel()).thenReturn(positionLevel);
            when(positionLevel.getDisplayName()).thenReturn(longDisplayName);

            // Act
            PositionResponse response = new PositionResponse(position);

            // Assert
            assertThat(response.getPositionLevelName(), is(equalTo(longDisplayName)));
        }

        @Test
        @DisplayName("Should handle position level with numeric display name")
        void shouldHandlePositionLevelWithNumericDisplayName() {
            // Arrange
            String numericDisplayName = "Level 1";
            
            when(position.getId()).thenReturn(positionId);
            when(position.getTitle()).thenReturn(positionTitle);
            when(position.getPositionLevel()).thenReturn(positionLevel);
            when(positionLevel.getDisplayName()).thenReturn(numericDisplayName);

            // Act
            PositionResponse response = new PositionResponse(position);

            // Assert
            assertThat(response.getPositionLevelName(), is(equalTo(numericDisplayName)));
        }

        @Test
        @DisplayName("Should handle position level with alphanumeric display name")
        void shouldHandlePositionLevelWithAlphanumericDisplayName() {
            // Arrange
            String alphanumericDisplayName = "Level C1 - Senior Engineer";
            
            when(position.getId()).thenReturn(positionId);
            when(position.getTitle()).thenReturn(positionTitle);
            when(position.getPositionLevel()).thenReturn(positionLevel);
            when(positionLevel.getDisplayName()).thenReturn(alphanumericDisplayName);

            // Act
            PositionResponse response = new PositionResponse(position);

            // Assert
            assertThat(response.getPositionLevelName(), is(equalTo(alphanumericDisplayName)));
        }
    }

    @Nested
    @DisplayName("Constructor with Position - Edge Cases Tests")
    class ConstructorWithPositionEdgeCasesTests {

        @Test
        @DisplayName("Should handle position with only required fields")
        void shouldHandlePositionWithOnlyRequiredFields() {
            // Arrange
            when(position.getId()).thenReturn(positionId);
            when(position.getTitle()).thenReturn(positionTitle);
            when(position.getPositionLevel()).thenReturn(null);

            // Act
            PositionResponse response = new PositionResponse(position);

            // Assert
            assertThat(response.getId(), is(equalTo(positionId)));
            assertThat(response.getTitle(), is(equalTo(positionTitle)));
            assertThat(response.getPositionLevelName(), is(nullValue()));
        }

        @Test
        @DisplayName("Should handle position with maximum data")
        void shouldHandlePositionWithMaximumData() {
            // Arrange
            when(position.getId()).thenReturn(positionId);
            when(position.getTitle()).thenReturn(positionTitle);
            when(position.getPositionLevel()).thenReturn(positionLevel);
            when(positionLevel.getDisplayName()).thenReturn(levelDisplayName);

            // Act
            PositionResponse response = new PositionResponse(position);

            // Assert
            assertThat(response.getId(), is(equalTo(positionId)));
            assertThat(response.getTitle(), is(equalTo(positionTitle)));
            assertThat(response.getPositionLevelName(), is(equalTo(levelDisplayName)));
        }

        @Test
        @DisplayName("Should be consistent across multiple instantiations with same position")
        void shouldBeConsistentAcrossMultipleInstantiationsWithSamePosition() {
            // Arrange
            when(position.getId()).thenReturn(positionId);
            when(position.getTitle()).thenReturn(positionTitle);
            when(position.getPositionLevel()).thenReturn(positionLevel);
            when(positionLevel.getDisplayName()).thenReturn(levelDisplayName);

            // Act
            PositionResponse response1 = new PositionResponse(position);
            PositionResponse response2 = new PositionResponse(position);

            // Assert
            assertThat(response1.getId(), is(equalTo(response2.getId())));
            assertThat(response1.getTitle(), is(equalTo(response2.getTitle())));
            assertThat(response1.getPositionLevelName(), is(equalTo(response2.getPositionLevelName())));
        }

        @Test
        @DisplayName("Should handle position with whitespace-only title")
        void shouldHandlePositionWithWhitespaceOnlyTitle() {
            // Arrange
            String whitespaceTitle = "   \t\n   ";
            
            when(position.getId()).thenReturn(positionId);
            when(position.getTitle()).thenReturn(whitespaceTitle);
            when(position.getPositionLevel()).thenReturn(null);

            // Act
            PositionResponse response = new PositionResponse(position);

            // Assert
            assertThat(response.getTitle(), is(equalTo(whitespaceTitle)));
        }

        @Test
        @DisplayName("Should handle position with whitespace-only level display name")
        void shouldHandlePositionWithWhitespaceOnlyLevelDisplayName() {
            // Arrange
            String whitespaceDisplayName = "   \t\n   ";
            
            when(position.getId()).thenReturn(positionId);
            when(position.getTitle()).thenReturn(positionTitle);
            when(position.getPositionLevel()).thenReturn(positionLevel);
            when(positionLevel.getDisplayName()).thenReturn(whitespaceDisplayName);

            // Act
            PositionResponse response = new PositionResponse(position);

            // Assert
            assertThat(response.getPositionLevelName(), is(equalTo(whitespaceDisplayName)));
        }

        @Test
        @DisplayName("Should handle extreme UUID values")
        void shouldHandleExtremeUUIDValues() {
            // Arrange
            UUID[] extremeIds = {
                new UUID(0L, 0L),
                new UUID(Long.MAX_VALUE, Long.MAX_VALUE),
                new UUID(Long.MIN_VALUE, Long.MIN_VALUE),
                UUID.randomUUID()
            };

            when(position.getTitle()).thenReturn(positionTitle);
            when(position.getPositionLevel()).thenReturn(null);

            for (UUID extremeId : extremeIds) {
                // Arrange
                when(position.getId()).thenReturn(extremeId);

                // Act
                PositionResponse response = new PositionResponse(position);

                // Assert
                assertThat(response.getId(), is(equalTo(extremeId)));
            }
        }

        @Test
        @DisplayName("Should handle position with single character title")
        void shouldHandlePositionWithSingleCharacterTitle() {
            // Arrange
            String singleCharTitle = "A";
            
            when(position.getId()).thenReturn(positionId);
            when(position.getTitle()).thenReturn(singleCharTitle);
            when(position.getPositionLevel()).thenReturn(null);

            // Act
            PositionResponse response = new PositionResponse(position);

            // Assert
            assertThat(response.getTitle(), is(equalTo(singleCharTitle)));
        }

        @Test
        @DisplayName("Should handle position with single character level display name")
        void shouldHandlePositionWithSingleCharacterLevelDisplayName() {
            // Arrange
            String singleCharDisplayName = "1";
            
            when(position.getId()).thenReturn(positionId);
            when(position.getTitle()).thenReturn(positionTitle);
            when(position.getPositionLevel()).thenReturn(positionLevel);
            when(positionLevel.getDisplayName()).thenReturn(singleCharDisplayName);

            // Act
            PositionResponse response = new PositionResponse(position);

            // Assert
            assertThat(response.getPositionLevelName(), is(equalTo(singleCharDisplayName)));
        }
    }

    @Nested
    @DisplayName("Constructor with Position - Validation Tests")
    class ConstructorWithPositionValidationTests {

        @Test
        @DisplayName("Should create response even with minimal position data")
        void shouldCreateResponseEvenWithMinimalPositionData() {
            // Arrange
            when(position.getId()).thenReturn(positionId);
            when(position.getTitle()).thenReturn(positionTitle);
            when(position.getPositionLevel()).thenReturn(null);

            // Act & Assert - Should not throw any exception
            assertDoesNotThrow(() -> {
                PositionResponse response = new PositionResponse(position);
                assertThat(response, is(notNullValue()));
            });
        }

        @Test
        @DisplayName("Should validate required position parameter")
        void shouldValidateRequiredPositionParameter() {
            // Act & Assert
            IllegalArgumentException thrownException = assertThrows(IllegalArgumentException.class, () -> {
                new PositionResponse(null);
            });

            assertThat(thrownException.getMessage(), is(equalTo("Position cannot be null")));
            assertThat(thrownException.getCause(), is(nullValue()));
        }

        @Test
        @DisplayName("Should not validate position fields themselves")
        void shouldNotValidatePositionFieldsThemselves() {
            // Arrange
            when(position.getId()).thenReturn(null);
            when(position.getTitle()).thenReturn(null);
            when(position.getPositionLevel()).thenReturn(null);

            // Act & Assert - Should accept position with null fields
            assertDoesNotThrow(() -> {
                PositionResponse response = new PositionResponse(position);
                assertThat(response, is(notNullValue()));
                assertThat(response.getId(), is(nullValue()));
                assertThat(response.getTitle(), is(nullValue()));
                assertThat(response.getPositionLevelName(), is(nullValue()));
            });
        }

        @Test
        @DisplayName("Should handle position that throws exception on getters")
        void shouldHandlePositionThatThrowsExceptionOnGetters() {
            // Arrange
            when(position.getId()).thenThrow(new RuntimeException("ID access error"));
            
            // Act & Assert
            RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                new PositionResponse(position);
            });

            assertThat(thrownException.getMessage(), is(equalTo("ID access error")));
        }

        @Test
        @DisplayName("Should handle position level that throws exception on getDisplayName")
        void shouldHandlePositionLevelThatThrowsExceptionOnGetDisplayName() {
            // Arrange
            when(position.getId()).thenReturn(positionId);
            when(position.getTitle()).thenReturn(positionTitle);
            when(position.getPositionLevel()).thenReturn(positionLevel);
            when(positionLevel.getDisplayName()).thenThrow(new RuntimeException("Display name access error"));

            // Act & Assert
            RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
                new PositionResponse(position);
            });

            assertThat(thrownException.getMessage(), is(equalTo("Display name access error")));
        }
    }
}
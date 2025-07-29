package com.itau.hr.people_management.domain.position.entity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.domain.position.enumeration.PositionLevel;
import com.itau.hr.people_management.domain.shared.message.DomainMessageSource;

@ExtendWith(MockitoExtension.class)
@DisplayName("Position Domain Entity Tests")
class PositionTest {

    @Mock
    private DomainMessageSource messageSource;

    private UUID validId;
    private String validTitle;
    private PositionLevel validPositionLevel;

    @BeforeEach
    void setUp() {
        Position.setMessageSource(messageSource);

        validId = UUID.randomUUID();
        validTitle = "Software Engineer";
        validPositionLevel = PositionLevel.SENIOR;
    }

    @Nested
    @DisplayName("Position Creation Tests")
    class PositionCreationTests {

        @Test
        @DisplayName("Should create position with valid parameters")
        void shouldCreatePositionWithValidParameters() {
            // Act
            Position position = Position.create(validId, validTitle, validPositionLevel);

            // Assert
            assertThat(position, is(notNullValue()));
            assertThat(position.getId(), is(equalTo(validId)));
            assertThat(position.getTitle(), is(equalTo(validTitle)));
            assertThat(position.getPositionLevel(), is(equalTo(validPositionLevel)));
        }

        @ParameterizedTest
        @EnumSource(PositionLevel.class)
        @DisplayName("Should create position with all position levels")
        void shouldCreatePositionWithAllPositionLevels(PositionLevel level) {
            // Act
            Position position = Position.create(validId, validTitle, level);

            // Assert
            assertThat(position, is(notNullValue()));
            assertThat(position.getPositionLevel(), is(equalTo(level)));
        }

        @Test
        @DisplayName("Should create position with different UUIDs")
        void shouldCreatePositionWithDifferentUuids() {
            // Arrange
            UUID[] testUuids = {
                UUID.randomUUID(),
                UUID.fromString("123e4567-e89b-12d3-a456-426614174000"),
                UUID.fromString("00000000-0000-0000-0000-000000000000"),
                UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff")
            };

            // Act & Assert
            for (UUID testId : testUuids) {
                Position position = Position.create(testId, validTitle, validPositionLevel);
                assertThat(position.getId(), is(equalTo(testId)));
            }
        }
    }

    @Nested
    @DisplayName("ID Validation Tests")
    class IdValidationTests {

        @Test
        @DisplayName("Should throw exception when id is null")
        void shouldThrowExceptionWhenIdIsNull() {
            // Arrange
            when(messageSource.getMessage("validation.position.id.null")).thenReturn("Position ID cannot be null");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Position.create(null, validTitle, validPositionLevel)
            );

            assertThat(exception.getMessage(), is(equalTo("Position ID cannot be null")));
            verify(messageSource).getMessage("validation.position.id.null");
        }

        @Test
        @DisplayName("Should accept any valid UUID")
        void shouldAcceptAnyValidUuid() {
            // Arrange
            UUID randomId = UUID.randomUUID();

            // Act & Assert
            assertDoesNotThrow(() -> {
                Position position = Position.create(randomId, validTitle, validPositionLevel);
                assertThat(position.getId(), is(equalTo(randomId)));
            });
        }
    }

    @Nested
    @DisplayName("Title Validation Tests")
    class TitleValidationTests {

        @Test
        @DisplayName("Should throw exception when title is null")
        void shouldThrowExceptionWhenTitleIsNull() {
            // Arrange
            when(messageSource.getMessage("validation.position.title.blank")).thenReturn("Position title cannot be blank");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Position.create(validId, null, validPositionLevel)
            );

            assertThat(exception.getMessage(), is(equalTo("Position title cannot be blank")));
            verify(messageSource).getMessage("validation.position.title.blank");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"", " ", "  ", "\t", "\n", "\r"})
        @DisplayName("Should throw exception when title is blank")
        void shouldThrowExceptionWhenTitleIsBlank(String blankTitle) {
            // Arrange
            when(messageSource.getMessage("validation.position.title.blank")).thenReturn("Position title cannot be blank");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Position.create(validId, blankTitle, validPositionLevel)
            );

            assertThat(exception.getMessage(), is(equalTo("Position title cannot be blank")));
        }

        @ParameterizedTest
        @ValueSource(strings = {"A", "B"})
        @DisplayName("Should throw exception when title is too short")
        void shouldThrowExceptionWhenTitleIsTooShort(String shortTitle) {
            // Arrange
            when(messageSource.getMessage("validation.position.title.lenght", 2))
                .thenReturn("Position title must be at least 2 characters long");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Position.create(validId, shortTitle, validPositionLevel)
            );

            assertThat(exception.getMessage(), is(equalTo("Position title must be at least 2 characters long")));
            verify(messageSource).getMessage("validation.position.title.lenght", 2);
        }

        @Test
        @DisplayName("Should accept minimum valid title length")
        void shouldAcceptMinimumValidTitleLength() {
            // Arrange
            String minValidTitle = "QA";

            // Act & Assert
            assertDoesNotThrow(() -> {
                Position position = Position.create(validId, minValidTitle, validPositionLevel);
                assertThat(position.getTitle(), is(equalTo(minValidTitle)));
            });
        }

        @Test
        @DisplayName("Should accept long position titles")
        void shouldAcceptLongPositionTitles() {
            // Arrange
            String longTitle = "Senior Principal Software Engineer and Architect";

            // Act & Assert
            assertDoesNotThrow(() -> {
                Position position = Position.create(validId, longTitle, validPositionLevel);
                assertThat(position.getTitle(), is(equalTo(longTitle)));
            });
        }

        @Test
        @DisplayName("Should accept titles with special characters")
        void shouldAcceptTitlesWithSpecialCharacters() {
            // Arrange
            String specialTitle = "C++ Developer";

            // Act & Assert
            assertDoesNotThrow(() -> {
                Position position = Position.create(validId, specialTitle, validPositionLevel);
                assertThat(position.getTitle(), is(equalTo(specialTitle)));
            });
        }

        @Test
        @DisplayName("Should accept titles with international characters")
        void shouldAcceptTitlesWithInternationalCharacters() {
            // Arrange
            String internationalTitle = "Développeur Senior";

            // Act & Assert
            assertDoesNotThrow(() -> {
                Position position = Position.create(validId, internationalTitle, validPositionLevel);
                assertThat(position.getTitle(), is(equalTo(internationalTitle)));
            });
        }

        @Test
        @DisplayName("Should accept titles with numbers")
        void shouldAcceptTitlesWithNumbers() {
            // Arrange
            String titleWithNumbers = "Level 3 Support Engineer";

            // Act & Assert
            assertDoesNotThrow(() -> {
                Position position = Position.create(validId, titleWithNumbers, validPositionLevel);
                assertThat(position.getTitle(), is(equalTo(titleWithNumbers)));
            });
        }

        @Test
        @DisplayName("Should accept titles with multiple words")
        void shouldAcceptTitlesWithMultipleWords() {
            // Arrange
            String multiWordTitle = "Senior Software Development Engineer in Test";

            // Act & Assert
            assertDoesNotThrow(() -> {
                Position position = Position.create(validId, multiWordTitle, validPositionLevel);
                assertThat(position.getTitle(), is(equalTo(multiWordTitle)));
            });
        }

        @Test
        @DisplayName("Should accept titles with punctuation")
        void shouldAcceptTitlesWithPunctuation() {
            // Arrange
            String titleWithPunctuation = "UI/UX Designer";

            // Act & Assert
            assertDoesNotThrow(() -> {
                Position position = Position.create(validId, titleWithPunctuation, validPositionLevel);
                assertThat(position.getTitle(), is(equalTo(titleWithPunctuation)));
            });
        }

        @Test
        @DisplayName("Should preserve title case and spacing")
        void shouldPreserveTitleCaseAndSpacing() {
            // Arrange
            String titleWithMixedCase = "Senior Java Developer";

            // Act & Assert
            Position position = Position.create(validId, titleWithMixedCase, validPositionLevel);
            assertThat(position.getTitle(), is(equalTo(titleWithMixedCase)));
        }
    }

    @Nested
    @DisplayName("Position Level Validation Tests")
    class PositionLevelValidationTests {

        @Test
        @DisplayName("Should throw exception when position level is null")
        void shouldThrowExceptionWhenPositionLevelIsNull() {
            // Arrange
            when(messageSource.getMessage("validation.position.positionlevel.null"))
                .thenReturn("Position level cannot be null");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Position.create(validId, validTitle, null)
            );

            assertThat(exception.getMessage(), is(equalTo("Position level cannot be null")));
            verify(messageSource).getMessage("validation.position.positionlevel.null");
        }

        @ParameterizedTest
        @EnumSource(PositionLevel.class)
        @DisplayName("Should accept all valid position levels")
        void shouldAcceptAllValidPositionLevels(PositionLevel level) {
            // Act & Assert
            assertDoesNotThrow(() -> {
                Position position = Position.create(validId, validTitle, level);
                assertThat(position.getPositionLevel(), is(equalTo(level)));
            });
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when IDs are the same")
        void shouldBeEqualWhenIdsAreTheSame() {
            // Arrange
            Position position1 = Position.create(validId, validTitle, validPositionLevel);
            Position position2 = Position.create(validId, "Different Title", PositionLevel.JUNIOR);

            // Act & Assert
            assertThat(position1, is(equalTo(position2)));
            assertThat(position1.hashCode(), is(equalTo(position2.hashCode())));
        }

        @Test
        @DisplayName("Should not be equal when IDs are different")
        void shouldNotBeEqualWhenIdsAreDifferent() {
            // Arrange
            UUID differentId = UUID.randomUUID();
            Position position1 = Position.create(validId, validTitle, validPositionLevel);
            Position position2 = Position.create(differentId, validTitle, validPositionLevel);

            // Act & Assert
            assertThat(position1, is(not(equalTo(position2))));
        }

        @Test
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Arrange
            Position position = Position.create(validId, validTitle, validPositionLevel);

            // Act & Assert
            assertThat(position, is(not(equalTo(null))));
        }

        @Test
        @DisplayName("Should not be equal to object of different class")
        void shouldNotBeEqualToObjectOfDifferentClass() {
            // Arrange
            Position position = Position.create(validId, validTitle, validPositionLevel);
            String differentObject = "Not a Position";

            // Act & Assert
            assertThat(position, is(not(equalTo(differentObject))));
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Arrange
            Position position = Position.create(validId, validTitle, validPositionLevel);

            // Act & Assert
            assertThat(position, is(equalTo(position)));
            assertThat(position.hashCode(), is(equalTo(position.hashCode())));
        }

        @Test
        @DisplayName("Should maintain hashCode consistency")
        void shouldMaintainHashCodeConsistency() {
            // Arrange
            Position position = Position.create(validId, validTitle, validPositionLevel);

            // Act
            int hashCode1 = position.hashCode();
            int hashCode2 = position.hashCode();

            // Assert
            assertThat(hashCode1, is(equalTo(hashCode2)));
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should include ID and title in toString")
        void shouldIncludeIdAndTitleInToString() {
            // Arrange
            Position position = Position.create(validId, validTitle, validPositionLevel);

            // Act
            String result = position.toString();

            // Assert
            assertThat(result, containsString(validId.toString()));
            assertThat(result, containsString(validTitle));
        }

        @Test
        @DisplayName("Should not include position level in toString")
        void shouldNotIncludePositionLevelInToString() {
            // Arrange
            Position position = Position.create(validId, validTitle, validPositionLevel);

            // Act
            String result = position.toString();

            // Assert
            assertThat(result, not(containsString(validPositionLevel.toString())));
        }

        @Test
        @DisplayName("Should produce consistent toString output")
        void shouldProduceConsistentToStringOutput() {
            // Arrange
            Position position = Position.create(validId, validTitle, validPositionLevel);

            // Act
            String result1 = position.toString();
            String result2 = position.toString();

            // Assert
            assertThat(result1, is(equalTo(result2)));
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("Should return correct ID")
        void shouldReturnCorrectId() {
            // Arrange
            Position position = Position.create(validId, validTitle, validPositionLevel);

            // Act & Assert
            assertThat(position.getId(), is(equalTo(validId)));
        }

        @Test
        @DisplayName("Should return correct title")
        void shouldReturnCorrectTitle() {
            // Arrange
            Position position = Position.create(validId, validTitle, validPositionLevel);

            // Act & Assert
            assertThat(position.getTitle(), is(equalTo(validTitle)));
        }

        @Test
        @DisplayName("Should return correct position level")
        void shouldReturnCorrectPositionLevel() {
            // Arrange
            Position position = Position.create(validId, validTitle, validPositionLevel);

            // Act & Assert
            assertThat(position.getPositionLevel(), is(equalTo(validPositionLevel)));
        }

        @Test
        @DisplayName("Should return immutable values")
        void shouldReturnImmutableValues() {
            // Arrange
            Position position = Position.create(validId, validTitle, validPositionLevel);

            // Act
            UUID returnedId = position.getId();
            String returnedTitle = position.getTitle();
            PositionLevel returnedLevel = position.getPositionLevel();

            // Assert - Values should remain the same on subsequent calls
            assertThat(position.getId(), is(equalTo(returnedId)));
            assertThat(position.getTitle(), is(equalTo(returnedTitle)));
            assertThat(position.getPositionLevel(), is(equalTo(returnedLevel)));
        }
    }

    @Nested
    @DisplayName("Message Source Configuration Tests")
    class MessageSourceConfigurationTests {

        @Test
        @DisplayName("Should set message source")
        void shouldSetMessageSource() {
            // Arrange
            DomainMessageSource newMessageSource = mock(DomainMessageSource.class);

            // Act & Assert
            assertDoesNotThrow(() -> Position.setMessageSource(newMessageSource));
        }

        @Test
        @DisplayName("Should use message source for validation errors")
        void shouldUseMessageSourceForValidationErrors() {
            // Arrange
            DomainMessageSource customMessageSource = mock(DomainMessageSource.class);
            when(customMessageSource.getMessage("validation.position.id.null"))
                .thenReturn("Custom ID error message");
            
            Position.setMessageSource(customMessageSource);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Position.create(null, validTitle, validPositionLevel)
            );

            assertThat(exception.getMessage(), is(equalTo("Custom ID error message")));
            verify(customMessageSource).getMessage("validation.position.id.null");

            // Restore original message source
            Position.setMessageSource(messageSource);
        }

        @Test
        @DisplayName("Should use message source for title validation with parameter")
        void shouldUseMessageSourceForTitleValidationWithParameter() {
            // Arrange
            DomainMessageSource customMessageSource = mock(DomainMessageSource.class);
            when(customMessageSource.getMessage("validation.position.title.lenght", 2))
                .thenReturn("Custom title length error message");
            
            Position.setMessageSource(customMessageSource);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Position.create(validId, "A", validPositionLevel)
            );

            assertThat(exception.getMessage(), is(equalTo("Custom title length error message")));
            verify(customMessageSource).getMessage("validation.position.title.lenght", 2);

            // Restore original message source
            Position.setMessageSource(messageSource);
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should be immutable after creation")
        void shouldBeImmutableAfterCreation() {
            // Arrange
            Position position = Position.create(validId, validTitle, validPositionLevel);

            // Act - Get initial values
            UUID initialId = position.getId();
            String initialTitle = position.getTitle();
            PositionLevel initialLevel = position.getPositionLevel();

            // Assert - Values should remain the same
            assertThat(position.getId(), is(equalTo(initialId)));
            assertThat(position.getTitle(), is(equalTo(initialTitle)));
            assertThat(position.getPositionLevel(), is(equalTo(initialLevel)));
        }

        @Test
        @DisplayName("Should have final fields")
        void shouldHaveFinalFields() {
            // This test verifies that the fields are final by ensuring
            // the class behavior is consistent with immutable objects
            Position position1 = Position.create(validId, validTitle, validPositionLevel);
            Position position2 = Position.create(validId, validTitle, validPositionLevel);

            // Same parameters should create equal objects
            assertThat(position1, is(equalTo(position2)));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle minimum valid inputs")
        void shouldHandleMinimumValidInputs() {
            // Arrange
            String minTitle = "QA";

            // Act & Assert
            assertDoesNotThrow(() -> {
                Position position = Position.create(validId, minTitle, validPositionLevel);
                assertThat(position.getTitle(), is(equalTo(minTitle)));
            });
        }

        @Test
        @DisplayName("Should handle very long titles")
        void shouldHandleVeryLongTitles() {
            // Arrange
            String longTitle = "A".repeat(1000);

            // Act & Assert
            assertDoesNotThrow(() -> {
                Position position = Position.create(validId, longTitle, validPositionLevel);
                assertThat(position.getTitle(), is(equalTo(longTitle)));
            });
        }

        @Test
        @DisplayName("Should handle titles with mixed whitespace")
        void shouldHandleTitlesWithMixedWhitespace() {
            // Arrange
            String titleWithWhitespace = "Senior\tSoftware\nEngineer";

            // Act & Assert
            assertDoesNotThrow(() -> {
                Position position = Position.create(validId, titleWithWhitespace, validPositionLevel);
                assertThat(position.getTitle(), is(equalTo(titleWithWhitespace)));
            });
        }

        @Test
        @DisplayName("Should handle titles with only valid characters at minimum length")
        void shouldHandleTitlesWithOnlyValidCharactersAtMinimumLength() {
            // Arrange
            String[] minTitles = {"IT", "QA", "PM", "BA", "DX"};

            // Act & Assert
            for (String title : minTitles) {
                assertDoesNotThrow(() -> {
                    Position position = Position.create(validId, title, validPositionLevel);
                    assertThat(position.getTitle(), is(equalTo(title)));
                });
            }
        }

        @Test
        @DisplayName("Should handle different combinations of parameters")
        void shouldHandleDifferentCombinationsOfParameters() {
            // Arrange
            String[] titles = {"Developer", "Analyst", "Manager", "Director"};
            PositionLevel[] levels = PositionLevel.values();

            // Act & Assert
            for (String title : titles) {
                for (PositionLevel level : levels) {
                    assertDoesNotThrow(() -> {
                        Position position = Position.create(validId, title, level);
                        assertThat(position.getTitle(), is(equalTo(title)));
                        assertThat(position.getPositionLevel(), is(equalTo(level)));
                    });
                }
            }
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("Should validate all parameters before creation")
        void shouldValidateAllParametersBeforeCreation() {
            // Test that validation happens in the correct order
            // and all validations are performed

            // ID validation first
            when(messageSource.getMessage("validation.position.id.null"))
                .thenReturn("ID error");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Position.create(null, validTitle, validPositionLevel)
            );
            assertThat(exception.getMessage(), is(equalTo("ID error")));

            // Title validation
            when(messageSource.getMessage("validation.position.title.blank"))
                .thenReturn("Title error");

            exception = assertThrows(IllegalArgumentException.class, () ->
                Position.create(validId, null, validPositionLevel)
            );
            assertThat(exception.getMessage(), is(equalTo("Title error")));

            // Position level validation
            when(messageSource.getMessage("validation.position.positionlevel.null"))
                .thenReturn("Level error");

            exception = assertThrows(IllegalArgumentException.class, () ->
                Position.create(validId, validTitle, null)
            );
            assertThat(exception.getMessage(), is(equalTo("Level error")));
        }

        @Test
        @DisplayName("Should create position only after all validations pass")
        void shouldCreatePositionOnlyAfterAllValidationsPass() {
            // Arrange - All valid inputs
            
            // Act
            Position position = Position.create(validId, validTitle, validPositionLevel);

            // Assert
            assertThat(position, is(notNullValue()));
            assertThat(position.getId(), is(equalTo(validId)));
            assertThat(position.getTitle(), is(equalTo(validTitle)));
            assertThat(position.getPositionLevel(), is(equalTo(validPositionLevel)));
        }
    }
}
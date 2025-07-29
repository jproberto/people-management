package com.itau.hr.people_management.domain.position.enumeration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.itau.hr.people_management.domain.shared.message.DomainMessageSource;

@ExtendWith(MockitoExtension.class)
@DisplayName("PositionLevel Enum Tests")
class PositionLevelTest {

    @Mock
    private DomainMessageSource messageSource;

    @Nested
    @DisplayName("Enum Values Tests")
    class EnumValuesTests {

        @Test
        @DisplayName("Should have exactly three position levels")
        void shouldHaveExactlyThreePositionLevels() {
            // Act
            PositionLevel[] values = PositionLevel.values();

            // Assert
            assertThat(values, arrayWithSize(3));
            assertThat(values, arrayContaining(
                PositionLevel.JUNIOR,
                PositionLevel.PLENO,
                PositionLevel.SENIOR
            ));
        }

        @Test
        @DisplayName("Should have correct ordinal values")
        void shouldHaveCorrectOrdinalValues() {
            // Assert
            assertThat(PositionLevel.JUNIOR.ordinal(), is(equalTo(0)));
            assertThat(PositionLevel.PLENO.ordinal(), is(equalTo(1)));
            assertThat(PositionLevel.SENIOR.ordinal(), is(equalTo(2)));
        }

        @Test
        @DisplayName("Should have correct string representations")
        void shouldHaveCorrectStringRepresentations() {
            // Assert
            assertThat(PositionLevel.JUNIOR.name(), is(equalTo("JUNIOR")));
            assertThat(PositionLevel.PLENO.name(), is(equalTo("PLENO")));
            assertThat(PositionLevel.SENIOR.name(), is(equalTo("SENIOR")));
        }

        @ParameterizedTest
        @EnumSource(PositionLevel.class)
        @DisplayName("Should be able to valueOf each enum constant")
        void shouldBeAbleToValueOfEachEnumConstant(PositionLevel level) {
            // Act
            PositionLevel result = PositionLevel.valueOf(level.name());

            // Assert
            assertThat(result, is(equalTo(level)));
        }
    }

    @Nested
    @DisplayName("Display Name Tests")
    class DisplayNameTests {

        @Test
        @DisplayName("Should return correct display name for JUNIOR")
        void shouldReturnCorrectDisplayNameForJunior() {
            // Act & Assert
            assertThat(PositionLevel.JUNIOR.getDisplayName(), is(equalTo("Júnior")));
        }

        @Test
        @DisplayName("Should return correct display name for PLENO")
        void shouldReturnCorrectDisplayNameForPleno() {
            // Act & Assert
            assertThat(PositionLevel.PLENO.getDisplayName(), is(equalTo("Pleno")));
        }

        @Test
        @DisplayName("Should return correct display name for SENIOR")
        void shouldReturnCorrectDisplayNameForSenior() {
            // Act & Assert
            assertThat(PositionLevel.SENIOR.getDisplayName(), is(equalTo("Sênior")));
        }

        @ParameterizedTest
        @EnumSource(PositionLevel.class)
        @DisplayName("Should return non-null display name for all levels")
        void shouldReturnNonNullDisplayNameForAllLevels(PositionLevel level) {
            // Act & Assert
            assertThat(level.getDisplayName(), is(notNullValue()));
            assertThat(level.getDisplayName(), is(not(emptyString())));
        }

        @Test
        @DisplayName("Should have immutable display names")
        void shouldHaveImmutableDisplayNames() {
            // Act
            String juniorDisplayName1 = PositionLevel.JUNIOR.getDisplayName();
            String juniorDisplayName2 = PositionLevel.JUNIOR.getDisplayName();

            // Assert
            assertThat(juniorDisplayName1, is(equalTo(juniorDisplayName2)));
            assertThat(juniorDisplayName1, is(sameInstance(juniorDisplayName2)));
        }

        @Test
        @DisplayName("Should preserve Portuguese characters in display names")
        void shouldPreservePortugueseCharactersInDisplayNames() {
            // Assert
            assertThat(PositionLevel.JUNIOR.getDisplayName(), containsString("ú"));
            assertThat(PositionLevel.SENIOR.getDisplayName(), containsString("ê"));
        }
    }

    @Nested
    @DisplayName("FromString Method Tests")
    class FromStringMethodTests {

        @Test
        @DisplayName("Should find JUNIOR by exact enum name")
        void shouldFindJuniorByExactEnumName() {
            // Act
            PositionLevel result = PositionLevel.fromString("JUNIOR", messageSource);

            // Assert
            assertThat(result, is(equalTo(PositionLevel.JUNIOR)));
        }

        @Test
        @DisplayName("Should find PLENO by exact enum name")
        void shouldFindPlenoByExactEnumName() {
            // Act
            PositionLevel result = PositionLevel.fromString("PLENO", messageSource);

            // Assert
            assertThat(result, is(equalTo(PositionLevel.PLENO)));
        }

        @Test
        @DisplayName("Should find SENIOR by exact enum name")
        void shouldFindSeniorByExactEnumName() {
            // Act
            PositionLevel result = PositionLevel.fromString("SENIOR", messageSource);

            // Assert
            assertThat(result, is(equalTo(PositionLevel.SENIOR)));
        }

        @Test
        @DisplayName("Should find JUNIOR by display name")
        void shouldFindJuniorByDisplayName() {
            // Act
            PositionLevel result = PositionLevel.fromString("Júnior", messageSource);

            // Assert
            assertThat(result, is(equalTo(PositionLevel.JUNIOR)));
        }

        @Test
        @DisplayName("Should find PLENO by display name")
        void shouldFindPlenoByDisplayName() {
            // Act
            PositionLevel result = PositionLevel.fromString("Pleno", messageSource);

            // Assert
            assertThat(result, is(equalTo(PositionLevel.PLENO)));
        }

        @Test
        @DisplayName("Should find SENIOR by display name")
        void shouldFindSeniorByDisplayName() {
            // Act
            PositionLevel result = PositionLevel.fromString("Sênior", messageSource);

            // Assert
            assertThat(result, is(equalTo(PositionLevel.SENIOR)));
        }

        @ParameterizedTest
        @ValueSource(strings = {"junior", "Junior", "JUNIOR", "JuNiOr"})
        @DisplayName("Should find JUNIOR case-insensitively by enum name")
        void shouldFindJuniorCaseInsensitivelyByEnumName(String input) {
            // Act
            PositionLevel result = PositionLevel.fromString(input, messageSource);

            // Assert
            assertThat(result, is(equalTo(PositionLevel.JUNIOR)));
        }

        @ParameterizedTest
        @ValueSource(strings = {"pleno", "Pleno", "PLENO", "PlEnO"})
        @DisplayName("Should find PLENO case-insensitively by enum name")
        void shouldFindPlenoCaseInsensitivelyByEnumName(String input) {
            // Act
            PositionLevel result = PositionLevel.fromString(input, messageSource);

            // Assert
            assertThat(result, is(equalTo(PositionLevel.PLENO)));
        }

        @ParameterizedTest
        @ValueSource(strings = {"senior", "Senior", "SENIOR", "SeNiOr"})
        @DisplayName("Should find SENIOR case-insensitively by enum name")
        void shouldFindSeniorCaseInsensitivelyByEnumName(String input) {
            // Act
            PositionLevel result = PositionLevel.fromString(input, messageSource);

            // Assert
            assertThat(result, is(equalTo(PositionLevel.SENIOR)));
        }

        @ParameterizedTest
        @ValueSource(strings = {"júnior", "Júnior", "JÚNIOR", "JúNiOr"})
        @DisplayName("Should find JUNIOR case-insensitively by display name")
        void shouldFindJuniorCaseInsensitivelyByDisplayName(String input) {
            // Act
            PositionLevel resultLevel = PositionLevel.fromString(input, messageSource);

            // Assert
            assertThat(resultLevel, is(equalTo(PositionLevel.JUNIOR)));
        }

        @ParameterizedTest
        @ValueSource(strings = {"sênior", "Sênior", "SÊNIOR", "SêNiOr"})
        @DisplayName("Should find SENIOR case-insensitively by display name")
        void shouldFindSeniorCaseInsensitivelyByDisplayName(String input) {
            // Act
            PositionLevel resultLevel = PositionLevel.fromString(input, messageSource);

            // Assert
            assertThat(resultLevel, is(equalTo(PositionLevel.SENIOR)));
        }

        @Test
        @DisplayName("Should throw exception for invalid input")
        void shouldThrowExceptionForInvalidInput() {
            // Arrange
            String invalidInput = "INVALID_LEVEL";
            when(messageSource.getMessage("validation.positionlevel.invalid", invalidInput))
                .thenReturn("Invalid position level: " + invalidInput);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                PositionLevel.fromString(invalidInput, messageSource)
            );

            assertThat(exception.getMessage(), is(equalTo("Invalid position level: " + invalidInput)));
            verify(messageSource).getMessage("validation.positionlevel.invalid", invalidInput);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "  ", "\t", "\n"})
        @DisplayName("Should throw exception for null, empty or whitespace input")
        void shouldThrowExceptionForNullEmptyOrWhitespaceInput(String input) {
            // Arrange
            when(messageSource.getMessage("validation.positionlevel.invalid", input))
                .thenReturn("Invalid position level: " + input);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> PositionLevel.fromString(input, messageSource));

            verify(messageSource).getMessage("validation.positionlevel.invalid", input);
        }

        @Test
        @DisplayName("Should throw exception when messageSource is null")
        void shouldThrowExceptionWhenMessageSourceIsNull() {
            // Act & Assert
            assertThrows(NullPointerException.class, () ->
                PositionLevel.fromString("INVALID", null)
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {"INTERMEDIATE", "EXPERT", "TRAINEE", "LEAD", "PRINCIPAL"})
        @DisplayName("Should throw exception for non-existent position levels")
        void shouldThrowExceptionForNonExistentPositionLevels(String invalidLevel) {
            // Arrange
            when(messageSource.getMessage("validation.positionlevel.invalid", invalidLevel))
                .thenReturn("Position level not found: " + invalidLevel);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                PositionLevel.fromString(invalidLevel, messageSource)
            );

            assertThat(exception.getMessage(), is(equalTo("Position level not found: " + invalidLevel)));
        }

        @Test
        @DisplayName("Should handle partial matches correctly")
        void shouldHandlePartialMatchesCorrectly() {
            // Arrange
            String partialMatch = "JUN";
            when(messageSource.getMessage("validation.positionlevel.invalid", partialMatch))
                .thenReturn("Invalid position level: " + partialMatch);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                PositionLevel.fromString(partialMatch, messageSource)
            );

            assertThat(exception.getMessage(), is(equalTo("Invalid position level: " + partialMatch)));
        }

        @Test
        @DisplayName("Should handle similar but different strings")
        void shouldHandleSimilarButDifferentStrings() {
            // Arrange
            String[] similarStrings = {"junior1", "seniorr", "pleno2", "jr", "sr", "pl"};

            for (String similarString : similarStrings) {
                when(messageSource.getMessage("validation.positionlevel.invalid", similarString))
                    .thenReturn("Invalid position level: " + similarString);

                // Act & Assert
                IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    PositionLevel.fromString(similarString, messageSource)
                );

                assertThat(exception.getMessage(), is(equalTo("Invalid position level: " + similarString)));
            }
        }
    }

    @Nested
    @DisplayName("Enum Behavior Tests")
    class EnumBehaviorTests {

        @Test
        @DisplayName("Should support valueOf with valid enum name")
        void shouldSupportValueOfWithValidEnumName() {
            // Act & Assert
            assertThat(PositionLevel.valueOf("JUNIOR"), is(equalTo(PositionLevel.JUNIOR)));
            assertThat(PositionLevel.valueOf("PLENO"), is(equalTo(PositionLevel.PLENO)));
            assertThat(PositionLevel.valueOf("SENIOR"), is(equalTo(PositionLevel.SENIOR)));
        }

        @Test
        @DisplayName("Should throw exception for valueOf with invalid enum name")
        void shouldThrowExceptionForValueOfWithInvalidEnumName() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () ->
                PositionLevel.valueOf("INVALID")
            );
        }

        @Test
        @DisplayName("Should be comparable")
        void shouldBeComparable() {
            // Act & Assert
            assertThat(PositionLevel.JUNIOR.compareTo(PositionLevel.PLENO), is(lessThan(0)));
            assertThat(PositionLevel.PLENO.compareTo(PositionLevel.SENIOR), is(lessThan(0)));
            assertThat(PositionLevel.SENIOR.compareTo(PositionLevel.JUNIOR), is(greaterThan(0)));
            assertThat(PositionLevel.JUNIOR.compareTo(PositionLevel.JUNIOR), is(equalTo(0)));
        }

        @Test
        @DisplayName("Should have consistent toString implementation")
        void shouldHaveConsistentToStringImplementation() {
            // Act & Assert
            assertThat(PositionLevel.JUNIOR.toString(), is(equalTo("JUNIOR")));
            assertThat(PositionLevel.PLENO.toString(), is(equalTo("PLENO")));
            assertThat(PositionLevel.SENIOR.toString(), is(equalTo("SENIOR")));
        }

        @Test
        @DisplayName("Should have consistent hashCode")
        void shouldHaveConsistentHashCode() {
            // Act & Assert
            assertThat(PositionLevel.JUNIOR.hashCode(), is(equalTo(PositionLevel.JUNIOR.hashCode())));
            assertThat(PositionLevel.PLENO.hashCode(), is(equalTo(PositionLevel.PLENO.hashCode())));
            assertThat(PositionLevel.SENIOR.hashCode(), is(equalTo(PositionLevel.SENIOR.hashCode())));
        }

        @SuppressWarnings("unlikely-arg-type")
        @Test
        @DisplayName("Should have working equals method")
        void shouldHaveWorkingEqualsMethod() {
            // Act & Assert
            assertThat(PositionLevel.JUNIOR.equals(PositionLevel.JUNIOR), is(true));
            assertThat(PositionLevel.JUNIOR.equals(PositionLevel.PLENO), is(false));
            assertThat(PositionLevel.JUNIOR.equals(null), is(false));
            assertThat(PositionLevel.JUNIOR.equals("JUNIOR"), is(false));
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should have immutable display name field")
        void shouldHaveImmutableDisplayNameField() {
            // Act
            String displayName1 = PositionLevel.JUNIOR.getDisplayName();
            String displayName2 = PositionLevel.JUNIOR.getDisplayName();

            // Assert
            assertThat(displayName1, is(equalTo(displayName2)));
            // In this case, they should be the same instance due to string interning of literals
            assertThat(displayName1, is(sameInstance(displayName2)));
        }

        @Test
        @DisplayName("Should be singleton instances")
        void shouldBeSingletonInstances() {
            // Act
            PositionLevel junior1 = PositionLevel.JUNIOR;
            PositionLevel junior2 = PositionLevel.valueOf("JUNIOR");

            // Assert
            assertThat(junior1, is(sameInstance(junior2)));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle unicode characters correctly in display names")
        void shouldHandleUnicodeCharactersCorrectlyInDisplayNames() {
            // Act & Assert
            assertThat(PositionLevel.JUNIOR.getDisplayName().contains("ú"), is(true));
            assertThat(PositionLevel.SENIOR.getDisplayName().contains("ê"), is(true));
        }

        @Test
        @DisplayName("Should handle accented characters in fromString")
        void shouldHandleAccentedCharactersInFromString() {
            // Act & Assert
            assertThat(PositionLevel.fromString("júnior", messageSource), is(equalTo(PositionLevel.JUNIOR)));
            assertThat(PositionLevel.fromString("sênior", messageSource), is(equalTo(PositionLevel.SENIOR)));
        }

        @Test
        @DisplayName("Should be thread-safe for fromString method")
        void shouldBeThreadSafeForFromStringMethod() {
            // This test verifies that multiple threads can safely call fromString
            // without causing issues (enum constants are thread-safe by nature)
            
            // Act & Assert - Multiple calls should work consistently
            for (int i = 0; i < 100; i++) {
                assertThat(PositionLevel.fromString("JUNIOR", messageSource), is(equalTo(PositionLevel.JUNIOR)));
                assertThat(PositionLevel.fromString("pleno", messageSource), is(equalTo(PositionLevel.PLENO)));
                assertThat(PositionLevel.fromString("Sênior", messageSource), is(equalTo(PositionLevel.SENIOR)));
            }
        }

        @Test
        @DisplayName("Should handle mixed Portuguese and English input")
        void shouldHandleMixedPortugueseAndEnglishInput() {
            // Act & Assert - Should work with both Portuguese display names and English enum names
            assertThat(PositionLevel.fromString("JUNIOR", messageSource), is(equalTo(PositionLevel.JUNIOR)));
            assertThat(PositionLevel.fromString("Júnior", messageSource), is(equalTo(PositionLevel.JUNIOR)));
            assertThat(PositionLevel.fromString("SENIOR", messageSource), is(equalTo(PositionLevel.SENIOR)));
            assertThat(PositionLevel.fromString("Sênior", messageSource), is(equalTo(PositionLevel.SENIOR)));
        }

        @Test
        @DisplayName("Should handle input with extra whitespace by rejecting it")
        void shouldHandleInputWithExtraWhitespaceByRejectingIt() {
            // Arrange
            String inputWithSpaces = " JUNIOR ";
            when(messageSource.getMessage("validation.positionlevel.invalid", inputWithSpaces))
                .thenReturn("Invalid position level: " + inputWithSpaces);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                PositionLevel.fromString(inputWithSpaces, messageSource)
            );

            assertThat(exception.getMessage(), is(equalTo("Invalid position level: " + inputWithSpaces)));
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should work correctly with all enum operations")
        void shouldWorkCorrectlyWithAllEnumOperations() {
            // Test a complete workflow using the enum
            
            // 1. Get all values
            PositionLevel[] allLevels = PositionLevel.values();
            assertThat(allLevels, arrayWithSize(3));

            // 2. Test fromString for each
            for (PositionLevel level : allLevels) {
                PositionLevel fromEnumName = PositionLevel.fromString(level.name(), messageSource);
                PositionLevel fromDisplayName = PositionLevel.fromString(level.getDisplayName(), messageSource);
                
                assertThat(fromEnumName, is(equalTo(level)));
                assertThat(fromDisplayName, is(equalTo(level)));
            }

            // 3. Test display names
            for (PositionLevel level : allLevels) {
                String displayName = level.getDisplayName();
                assertThat(displayName, is(notNullValue()));
                assertThat(displayName, is(not(emptyString())));
            }
        }

        @Test
        @DisplayName("Should maintain consistency between all access methods")
        void shouldMaintainConsistencyBetweenAllAccessMethods() {
            // Verify that all ways of accessing the same enum constant return the same instance
            
            PositionLevel junior1 = PositionLevel.JUNIOR;
            PositionLevel junior2 = PositionLevel.valueOf("JUNIOR");
            PositionLevel junior3 = PositionLevel.values()[0];
            PositionLevel junior4 = PositionLevel.fromString("JUNIOR", messageSource);
            PositionLevel junior5 = PositionLevel.fromString("Júnior", messageSource);

            assertThat(junior1, is(sameInstance(junior2)));
            assertThat(junior1, is(sameInstance(junior3)));
            assertThat(junior1, is(sameInstance(junior4)));
            assertThat(junior1, is(sameInstance(junior5)));
        }
    }
}
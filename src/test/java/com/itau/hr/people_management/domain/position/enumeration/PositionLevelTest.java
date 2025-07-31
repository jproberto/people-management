package com.itau.hr.people_management.domain.position.enumeration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
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

    @Test
    @DisplayName("Should have correct display names")
    void shouldHaveCorrectDisplayNames() {
        // Act & Assert
        assertThat(PositionLevel.JUNIOR.getDisplayName(), is("Júnior"));
        assertThat(PositionLevel.PLENO.getDisplayName(), is("Pleno"));
        assertThat(PositionLevel.SENIOR.getDisplayName(), is("Sênior"));
    }

    @Test
    @DisplayName("Should find position level by exact enum name")
    void shouldFindPositionLevelByExactEnumName() {
        // Act & Assert
        assertThat(PositionLevel.fromString("JUNIOR", messageSource), is(PositionLevel.JUNIOR));
        assertThat(PositionLevel.fromString("PLENO", messageSource), is(PositionLevel.PLENO));
        assertThat(PositionLevel.fromString("SENIOR", messageSource), is(PositionLevel.SENIOR));
    }

    @Test
    @DisplayName("Should find position level by display name")
    void shouldFindPositionLevelByDisplayName() {
        // Act & Assert
        assertThat(PositionLevel.fromString("Júnior", messageSource), is(PositionLevel.JUNIOR));
        assertThat(PositionLevel.fromString("Pleno", messageSource), is(PositionLevel.PLENO));
        assertThat(PositionLevel.fromString("Sênior", messageSource), is(PositionLevel.SENIOR));
    }

    @ParameterizedTest
    @ValueSource(strings = {"junior", "JUNIOR", "JuNiOr", "júnior", "JÚNIOR", "JúNiOr"})
    @DisplayName("Should find JUNIOR case-insensitively")
    void shouldFindJuniorCaseInsensitively(String input) {
        // Act & Assert
        assertThat(PositionLevel.fromString(input, messageSource), is(PositionLevel.JUNIOR));
    }

    @ParameterizedTest
    @ValueSource(strings = {"pleno", "PLENO", "PlEnO"})
    @DisplayName("Should find PLENO case-insensitively")
    void shouldFindPlenoCaseInsensitively(String input) {
        // Act & Assert
        assertThat(PositionLevel.fromString(input, messageSource), is(PositionLevel.PLENO));
    }

    @ParameterizedTest
    @ValueSource(strings = {"senior", "SENIOR", "SeNiOr", "sênior", "SÊNIOR", "SêNiOr"})
    @DisplayName("Should find SENIOR case-insensitively")
    void shouldFindSeniorCaseInsensitively(String input) {
        // Act & Assert
        assertThat(PositionLevel.fromString(input, messageSource), is(PositionLevel.SENIOR));
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

        assertThat(exception.getMessage(), is("Invalid position level: " + invalidInput));
        verify(messageSource).getMessage("validation.positionlevel.invalid", invalidInput);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n", " JUNIOR "})
    @DisplayName("Should throw exception for null, empty or invalid input")
    void shouldThrowExceptionForNullEmptyOrInvalidInput(String input) {
        // Arrange
        when(messageSource.getMessage("validation.positionlevel.invalid", input))
            .thenReturn("Invalid position level: " + input);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
            PositionLevel.fromString(input, messageSource)
        );
    }

    @Test
    @DisplayName("Should throw exception when messageSource is null")
    void shouldThrowExceptionWhenMessageSourceIsNull() {
        // Act & Assert
        assertThrows(NullPointerException.class, () ->
            PositionLevel.fromString("INVALIDO", null)
        );
    }
}
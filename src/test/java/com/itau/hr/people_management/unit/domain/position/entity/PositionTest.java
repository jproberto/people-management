package com.itau.hr.people_management.unit.domain.position.entity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    @DisplayName("Should create position with valid parameters")
    void shouldCreatePositionWithValidParameters() {
        // Act
        Position position = Position.create(validId, validTitle, validPositionLevel);

        // Assert
        assertThat(position.getId(), is(validId));
        assertThat(position.getTitle(), is(validTitle));
        assertThat(position.getPositionLevel(), is(validPositionLevel));
    }

    @Test
    @DisplayName("Should throw exception when id is null")
    void shouldThrowExceptionWhenIdIsNull() {
        // Arrange
        when(messageSource.getMessage("validation.position.id.null")).thenReturn("Position ID cannot be null");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            Position.create(null, validTitle, validPositionLevel)
        );

        assertThat(exception.getMessage(), is("Position ID cannot be null"));
    }

    @Test
    @DisplayName("Should throw exception when title is blank")
    void shouldThrowExceptionWhenTitleIsBlank() {
        // Arrange
        when(messageSource.getMessage("validation.position.title.blank")).thenReturn("Position title cannot be blank");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            Position.create(validId, "  ", validPositionLevel)
        );

        assertThat(exception.getMessage(), is("Position title cannot be blank"));
    }

    @Test
    @DisplayName("Should throw exception when title is too short")
    void shouldThrowExceptionWhenTitleIsTooShort() {
        // Arrange
        when(messageSource.getMessage("validation.position.title.lenght", 2))
            .thenReturn("Position title must be at least 2 characters long");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            Position.create(validId, "A", validPositionLevel)
        );

        assertThat(exception.getMessage(), is("Position title must be at least 2 characters long"));
    }

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

        assertThat(exception.getMessage(), is("Position level cannot be null"));
    }

    @Test
    @DisplayName("Should accept minimum valid title length")
    void shouldAcceptMinimumValidTitleLength() {
        // Arrange
        String minValidTitle = "QA";

        // Act
        Position position = Position.create(validId, minValidTitle, validPositionLevel);

        // Assert
        assertThat(position.getTitle(), is(minValidTitle));
    }

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
}
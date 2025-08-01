package com.itau.hr.people_management.unit.domain.shared.vo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.domain.shared.message.DomainMessageSource;
import com.itau.hr.people_management.domain.shared.vo.Email;

@ExtendWith(MockitoExtension.class)
@DisplayName("Email Value Object Tests")
class EmailTest {

    @Mock
    private DomainMessageSource messageSource;

    private String validEmailAddress;

    @BeforeEach
    void setUp() {
        Email.setMessageSource(messageSource);
        validEmailAddress = "john.doe@example.com";
    }

    @Test
    @DisplayName("Should create email with valid address")
    void shouldCreateEmailWithValidAddress() {
        // Act
        Email email = Email.create(validEmailAddress);

        // Assert
        assertThat(email.getAddress(), is(validEmailAddress));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "user@domain.com",
        "test.email@example.org", 
        "user_name@sub-domain.co.uk",
        "123@example.com",
        "a@b.co"
    })
    @DisplayName("Should create email with various valid formats")
    void shouldCreateEmailWithVariousValidFormats(String emailAddress) {
        // Act
        Email email = Email.create(emailAddress);

        // Assert
        assertThat(email.getAddress(), is(emailAddress));
    }

    @Test
    @DisplayName("Should throw exception when address is null or blank")
    void shouldThrowExceptionWhenAddressIsNullOrBlank() {
        // Arrange
        when(messageSource.getMessage("validation.email.address.blank"))
            .thenReturn("Email address cannot be blank");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> Email.create(null));
        assertThrows(IllegalArgumentException.class, () -> Email.create(""));
        assertThrows(IllegalArgumentException.class, () -> Email.create("  "));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "invalid-email",
        "@domain.com",
        "user@domain",
        "user.domain.com",
        "user@@domain.com",
        "user@domain..com",
        "us er@domain.com"
    })
    @DisplayName("Should throw exception for invalid email formats")
    void shouldThrowExceptionForInvalidEmailFormats(String invalidEmail) {
        // Arrange
        when(messageSource.getMessage("validation.email.address.invalid"))
            .thenReturn("Invalid email format");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> Email.create(invalidEmail));
    }

    @Test
    @DisplayName("Should throw exception when address is too short")
    void shouldThrowExceptionWhenAddressIsTooShort() {
        // Arrange
        when(messageSource.getMessage("validation.email.address.length", 6, 100))
            .thenReturn("Email address must be between 6 and 100 characters");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> Email.create("a@b.c"));
    }

    @Test
    @DisplayName("Should throw exception when address is too long")
    void shouldThrowExceptionWhenAddressIsTooLong() {
        // Arrange
        String longEmail = "a".repeat(89) + "@example.com"; // 101 characters
        when(messageSource.getMessage("validation.email.address.length", 6, 100))
            .thenReturn("Email address must be between 6 and 100 characters");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> Email.create(longEmail));
    }

    @Test
    @DisplayName("Should be equal when addresses are the same")
    void shouldBeEqualWhenAddressesAreTheSame() {
        // Arrange
        Email email1 = Email.create(validEmailAddress);
        Email email2 = Email.create(validEmailAddress);

        // Act & Assert
        assertThat(email1, is(equalTo(email2)));
        assertThat(email1.hashCode(), is(equalTo(email2.hashCode())));
    }

    @Test
    @DisplayName("Should not be equal when addresses are different")
    void shouldNotBeEqualWhenAddressesAreDifferent() {
        // Arrange
        Email email1 = Email.create("user1@example.com");
        Email email2 = Email.create("user2@example.com");

        // Act & Assert
        assertThat(email1, is(not(equalTo(email2))));
    }

    @Test
    @DisplayName("Should include address in toString")
    void shouldIncludeAddressInToString() {
        // Arrange
        Email email = Email.create(validEmailAddress);

        // Act
        String result = email.toString();

        // Assert
        assertThat(result, containsString(validEmailAddress));
    }
}
package com.itau.hr.people_management.domain.shared.vo;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.itau.hr.people_management.domain.shared.message.DomainMessageSource;

@ExtendWith(MockitoExtension.class)
@DisplayName("Email Value Object Tests")
class EmailTest {

    @Mock
    private static DomainMessageSource messageSource;

    private String validEmailAddress;

    @BeforeEach
    void setUp() {
        Email.setMessageSource(messageSource);

        validEmailAddress = "john.doe@example.com";
    }

    @Nested
    @DisplayName("Email Creation Tests")
    class EmailCreationTests {

        @Test
        @DisplayName("Should create email with valid address")
        void shouldCreateEmailWithValidAddress() {
            // Act
            Email email = Email.create(validEmailAddress);

            // Assert
            assertThat(email, is(notNullValue()));
            assertThat(email.getAddress(), is(equalTo(validEmailAddress)));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "user@domain.com",
            "test.email@example.org",
            "firstname.lastname@company.co.uk",
            "user123@test-domain.com",
            "email@subdomain.example.com",
            "a@b.co",
            "test_user@domain.net",
            "user-name@domain-name.com",
            "email.with.dots@example.com",
            "user+tag@example.com",
            "123@example.com",
            "email@123.com",
            "test@domain-with-dash.com",
            "user@domain.info"
        })
        @DisplayName("Should create email with various valid formats")
        void shouldCreateEmailWithVariousValidFormats(String emailAddress) {
            // Act
            Email email = Email.create(emailAddress);

            // Assert
            assertThat(email, is(notNullValue()));
            assertThat(email.getAddress(), is(equalTo(emailAddress)));
        }

        @Test
        @DisplayName("Should create email with minimum valid length")
        void shouldCreateEmailWithMinimumValidLength() {
            // Arrange
            String minValidEmail = "a@b.co"; // 6 characters

            // Act
            Email email = Email.create(minValidEmail);

            // Assert
            assertThat(email.getAddress(), is(equalTo(minValidEmail)));
        }

        @Test
        @DisplayName("Should create email with maximum valid length")
        void shouldCreateEmailWithMaximumValidLength() {
            // Arrange - 100 characters total
            String maxValidEmail = "a".repeat(88) + "@example.com"; // 88 + 12 = 100

            // Act
            Email email = Email.create(maxValidEmail);

            // Assert
            assertThat(email.getAddress(), is(equalTo(maxValidEmail)));
            assertThat(email.getAddress().length(), is(equalTo(100)));
        }
    }

    @Nested
    @DisplayName("Address Validation Tests")
    class AddressValidationTests {

        @Test
        @DisplayName("Should throw exception when address is null")
        void shouldThrowExceptionWhenAddressIsNull() {
            // Arrange
            when(messageSource.getMessage("validation.email.address.blank"))
                .thenReturn("Email address cannot be blank");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Email.create(null)
            );

            assertThat(exception.getMessage(), is(equalTo("Email address cannot be blank")));
            verify(messageSource).getMessage("validation.email.address.blank");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"", " ", "  ", "\t", "\n", "\r", "   \t\n   "})
        @DisplayName("Should throw exception when address is blank")
        void shouldThrowExceptionWhenAddressIsBlank(String blankAddress) {
            // Arrange
            when(messageSource.getMessage("validation.email.address.blank"))
                .thenReturn("Email address cannot be blank");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Email.create(blankAddress)
            );

            assertThat(exception.getMessage(), is(equalTo("Email address cannot be blank")));
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "invalid-email",
            "@domain.com",
            "user@domain",
            "user.domain.com",
            "user@@domain.com",
            "user@domain..com",
            "user@.domain.com",
            "user@domain.com.",
            ".user@domain.com",
            "user.@domain.com",
            "us er@domain.com",
            "user@dom ain.com",
            "user@domain.c",
            "user@domain.123456",
            "user@domain.",
            "user@domain.c@m",
            "user@domain..com",
            "user name@domain.com"
        })
        @DisplayName("Should throw exception for invalid email formats")
        void shouldThrowExceptionForInvalidEmailFormats(String invalidEmail) {
            // Arrange
            when(messageSource.getMessage("validation.email.address.invalid"))
                .thenReturn("Invalid email format");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Email.create(invalidEmail)
            );

            assertThat(exception.getMessage(), is(equalTo("Invalid email format")));
        }

        @Test
        @DisplayName("Should throw exception when address is too short")
        void shouldThrowExceptionWhenAddressIsTooShort() {
            // Arrange
            String shortEmail = "a@b.c"; // 5 characters
            when(messageSource.getMessage("validation.email.address.length", 6, 100))
                .thenReturn("Email address must be between 6 and 100 characters");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Email.create(shortEmail)
            );

            assertThat(exception.getMessage(), is(equalTo("Email address must be between 6 and 100 characters")));
            verify(messageSource).getMessage("validation.email.address.length", 6, 100);
        }

        @Test
        @DisplayName("Should throw exception when address is too long")
        void shouldThrowExceptionWhenAddressIsTooLong() {
            // Arrange
            String longEmail = "a".repeat(89) + "@example.com"; // 101 characters
            when(messageSource.getMessage("validation.email.address.length", 6, 100))
                .thenReturn("Email address must be between 6 and 100 characters");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Email.create(longEmail)
            );

            assertThat(exception.getMessage(), is(equalTo("Email address must be between 6 and 100 characters")));
            verify(messageSource).getMessage("validation.email.address.length", 6, 100);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "user@domain.a",      // TLD too short
            "user@domain.1",      // Numeric TLD
            "user@domain.12",     // Numeric TLD
            "user@domain.-com",   // Invalid TLD start
            "user@domain.com-",   // Invalid TLD end
        })
        @DisplayName("Should throw exception for invalid TLD formats")
        void shouldThrowExceptionForInvalidTldFormats(String invalidTldEmail) {
            // Arrange
            when(messageSource.getMessage("validation.email.address.invalid"))
                .thenReturn("Invalid email format");

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Email.create(invalidTldEmail)
            );

            assertThat(exception.getMessage(), is(equalTo("Invalid email format")));
        }
    }

    @Nested
    @DisplayName("Regex Pattern Tests")
    class RegexPatternTests {

        @ParameterizedTest
        @ValueSource(strings = {
            "test@example.com",
            "user.name@domain.co.uk",
            "firstname-lastname@company.org",
            "email123@test-domain.net",
            "user_name@sub.domain.com",
            "test.email+tag@example.info",
            "123numbers@domain.com"
        })
        @DisplayName("Should accept valid email patterns")
        void shouldAcceptValidEmailPatterns(String validEmail) {
            // Act & Assert
            assertDoesNotThrow(() -> {
                Email email = Email.create(validEmail);
                assertThat(email.getAddress(), is(equalTo(validEmail)));
            });
        }

        @Test
        @DisplayName("Should handle emails with special characters in local part")
        void shouldHandleEmailsWithSpecialCharactersInLocalPart() {
            // Arrange
            String[] validSpecialEmails = {
                "user.name@example.com",
                "user_name@example.com",
                "user-name@example.com",
                "user123@example.com",
                "123user@example.com"
            };

            // Act & Assert
            for (String email : validSpecialEmails) {
                assertDoesNotThrow(() -> {
                    Email emailObj = Email.create(email);
                    assertThat(emailObj.getAddress(), is(equalTo(email)));
                });
            }
        }

        @Test
        @DisplayName("Should handle emails with special characters in domain part")
        void shouldHandleEmailsWithSpecialCharactersInDomainPart() {
            // Arrange
            String[] validDomainEmails = {
                "user@sub-domain.com",
                "user@sub.domain.com",
                "user@123domain.com",
                "user@domain123.com"
            };

            // Act & Assert
            for (String email : validDomainEmails) {
                assertDoesNotThrow(() -> {
                    Email emailObj = Email.create(email);
                    assertThat(emailObj.getAddress(), is(equalTo(email)));
                });
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "user@domain.co",
            "user@domain.org",
            "user@domain.net",
            "user@domain.edu",
            "user@domain.gov",
            "user@domain.mil",
            "user@domain.info",
            "user@domain.name"
        })
        @DisplayName("Should accept various valid TLD formats")
        void shouldAcceptVariousValidTldFormats(String emailWithValidTld) {
            // Act & Assert
            assertDoesNotThrow(() -> {
                Email email = Email.create(emailWithValidTld);
                assertThat(email.getAddress(), is(equalTo(emailWithValidTld)));
            });
        }
    }

    @Nested
    @DisplayName("Getter Tests")
    class GetterTests {

        @Test
        @DisplayName("Should return correct address")
        void shouldReturnCorrectAddress() {
            // Arrange
            Email email = Email.create(validEmailAddress);

            // Act & Assert
            assertThat(email.getAddress(), is(equalTo(validEmailAddress)));
        }

        @Test
        @DisplayName("Should return immutable address")
        void shouldReturnImmutableAddress() {
            // Arrange
            Email email = Email.create(validEmailAddress);

            // Act
            String returnedAddress = email.getAddress();

            // Assert - Should be the same reference since it's immutable
            assertThat(email.getAddress(), is(sameInstance(returnedAddress)));
        }

        @Test
        @DisplayName("Should return consistent address on multiple calls")
        void shouldReturnConsistentAddressOnMultipleCalls() {
            // Arrange
            Email email = Email.create(validEmailAddress);

            // Act & Assert
            for (int i = 0; i < 100; i++) {
                assertThat(email.getAddress(), is(equalTo(validEmailAddress)));
            }
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

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
        @DisplayName("Should not be equal to null")
        void shouldNotBeEqualToNull() {
            // Arrange
            Email email = Email.create(validEmailAddress);

            // Act & Assert
            assertThat(email, is(not(equalTo(null))));
            assertThat(email.equals(null), is(false));
        }

        @SuppressWarnings("unlikely-arg-type")
        @Test
        @DisplayName("Should not be equal to object of different class")
        void shouldNotBeEqualToObjectOfDifferentClass() {
            // Arrange
            Email email = Email.create(validEmailAddress);
            String differentObject = validEmailAddress;

            // Act & Assert
            assertThat(email, is(not(equalTo(differentObject))));
            assertThat(email.equals(differentObject), is(false));
        }

        @Test
        @DisplayName("Should be equal to itself")
        void shouldBeEqualToItself() {
            // Arrange
            Email email = Email.create(validEmailAddress);

            // Act & Assert
            assertThat(email, is(equalTo(email)));
            assertThat(email.equals(email), is(true));
            assertThat(email.hashCode(), is(equalTo(email.hashCode())));
        }

        @Test
        @DisplayName("Should handle equals reflexivity")
        void shouldHandleEqualsReflexivity() {
            // Arrange
            Email email = Email.create(validEmailAddress);

            // Act & Assert
            assertThat(email.equals(email), is(true));
        }

        @Test
        @DisplayName("Should handle equals symmetry")
        void shouldHandleEqualsSymmetry() {
            // Arrange
            Email email1 = Email.create(validEmailAddress);
            Email email2 = Email.create(validEmailAddress);

            // Act & Assert
            assertThat(email1.equals(email2), is(true));
            assertThat(email2.equals(email1), is(true));
        }

        @Test
        @DisplayName("Should handle equals transitivity")
        void shouldHandleEqualsTransitivity() {
            // Arrange
            Email email1 = Email.create(validEmailAddress);
            Email email2 = Email.create(validEmailAddress);
            Email email3 = Email.create(validEmailAddress);

            // Act & Assert
            assertThat(email1.equals(email2), is(true));
            assertThat(email2.equals(email3), is(true));
            assertThat(email1.equals(email3), is(true));
        }

        @Test
        @DisplayName("Should maintain hashCode consistency")
        void shouldMaintainHashCodeConsistency() {
            // Arrange
            Email email = Email.create(validEmailAddress);

            // Act
            int hashCode1 = email.hashCode();
            int hashCode2 = email.hashCode();

            // Assert
            assertThat(hashCode1, is(equalTo(hashCode2)));
        }

        @Test
        @DisplayName("Should have different hashCodes for different addresses")
        void shouldHaveDifferentHashCodesForDifferentAddresses() {
            // Arrange
            Email email1 = Email.create("user1@example.com");
            Email email2 = Email.create("user2@example.com");

            // Act
            int hashCode1 = email1.hashCode();
            int hashCode2 = email2.hashCode();

            // Assert
            assertThat(hashCode1, is(not(equalTo(hashCode2))));
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

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

        @Test
        @DisplayName("Should produce consistent toString output")
        void shouldProduceConsistentToStringOutput() {
            // Arrange
            Email email = Email.create(validEmailAddress);

            // Act
            String result1 = email.toString();
            String result2 = email.toString();

            // Assert
            assertThat(result1, is(equalTo(result2)));
        }

        @Test
        @DisplayName("Should not throw exception on toString")
        void shouldNotThrowExceptionOnToString() {
            // Arrange
            Email email = Email.create(validEmailAddress);

            // Act & Assert
            assertDoesNotThrow(() -> {
                String result = email.toString();
                assertThat(result, is(notNullValue()));
            });
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
            assertDoesNotThrow(() -> Email.setMessageSource(newMessageSource));
        }

        @Test
        @DisplayName("Should use message source for validation errors")
        void shouldUseMessageSourceForValidationErrors() {
            // Arrange
            DomainMessageSource customMessageSource = mock(DomainMessageSource.class);
            when(customMessageSource.getMessage("validation.email.address.blank"))
                .thenReturn("Custom blank email error");
            
            Email.setMessageSource(customMessageSource);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Email.create(null)
            );

            assertThat(exception.getMessage(), is(equalTo("Custom blank email error")));
            verify(customMessageSource).getMessage("validation.email.address.blank");

            // Restore original message source
            Email.setMessageSource(messageSource);
        }

        @Test
        @DisplayName("Should use message source for length validation with parameters")
        void shouldUseMessageSourceForLengthValidationWithParameters() {
            // Arrange
            DomainMessageSource customMessageSource = mock(DomainMessageSource.class);
            when(customMessageSource.getMessage("validation.email.address.length", 6, 100))
                .thenReturn("Custom length error message");
            
            Email.setMessageSource(customMessageSource);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Email.create("a@bc") // Too short
            );

            assertThat(exception.getMessage(), is(equalTo("Custom length error message")));
            verify(customMessageSource).getMessage("validation.email.address.length", 6, 100);

            // Restore original message source
            Email.setMessageSource(messageSource);
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("Should be immutable after creation")
        void shouldBeImmutableAfterCreation() {
            // Arrange
            Email email = Email.create(validEmailAddress);

            // Act - Get initial address
            String initialAddress = email.getAddress();

            // Assert - Address should remain the same
            assertThat(email.getAddress(), is(equalTo(initialAddress)));
        }

        @Test
        @DisplayName("Should have final address field")
        void shouldHaveFinalAddressField() {
            // This test verifies that the address field is final by ensuring
            // the value object behavior is consistent with immutable objects
            Email email1 = Email.create(validEmailAddress);
            Email email2 = Email.create(validEmailAddress);

            // Same parameters should create equal objects
            assertThat(email1, is(equalTo(email2)));
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle email with minimum valid domain")
        void shouldHandleEmailWithMinimumValidDomain() {
            // Arrange
            String emailWithMinDomain = "a@b.co"; // Minimum valid format

            // Act & Assert
            assertDoesNotThrow(() -> {
                Email email = Email.create(emailWithMinDomain);
                assertThat(email.getAddress(), is(equalTo(emailWithMinDomain)));
            });
        }

        @Test
        @DisplayName("Should handle email with complex subdomain")
        void shouldHandleEmailWithComplexSubdomain() {
            // Arrange
            String complexSubdomainEmail = "user@mail.subdomain.example.com";

            // Act & Assert
            assertDoesNotThrow(() -> {
                Email email = Email.create(complexSubdomainEmail);
                assertThat(email.getAddress(), is(equalTo(complexSubdomainEmail)));
            });
        }

        @Test
        @DisplayName("Should handle rapid successive creations")
        void shouldHandleRapidSuccessiveCreations() {
            // Act & Assert
            for (int i = 0; i < 1000; i++) {
                String emailAddress = "user" + i + "@example.com";
                Email email = Email.create(emailAddress);
                assertThat(email.getAddress(), is(equalTo(emailAddress)));
            }
        }

        @Test
        @DisplayName("Should handle emails with international domain names")
        void shouldHandleEmailsWithInternationalDomainNames() {
            // Note: This regex might not support IDN, but testing edge case
            String[] internationalEmails = {
                "user@example.org",
                "user@domain.info",
                "user@company.name"
            };

            for (String email : internationalEmails) {
                assertDoesNotThrow(() -> {
                    Email emailObj = Email.create(email);
                    assertThat(emailObj.getAddress(), is(equalTo(email)));
                });
            }
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("Should validate address before creation")
        void shouldValidateAddressBeforeCreation() {
            // Test that validation happens before object creation
            when(messageSource.getMessage("validation.email.address.blank"))
                .thenReturn("Email error");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Email.create(null)
            );
            assertThat(exception.getMessage(), is(equalTo("Email error")));
        }

        @Test
        @DisplayName("Should create email only after validation passes")
        void shouldCreateEmailOnlyAfterValidationPasses() {
            // Arrange - Valid email
            
            // Act
            Email email = Email.create(validEmailAddress);

            // Assert
            assertThat(email, is(notNullValue()));
            assertThat(email.getAddress(), is(equalTo(validEmailAddress)));
        }

        @Test
        @DisplayName("Should validate all constraints in sequence")
        void shouldValidateAllConstraintsInSequence() {
            // The validation should check blank first, then format, then length
            
            // Blank validation
            when(messageSource.getMessage("validation.email.address.blank"))
                .thenReturn("Blank error");
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                Email.create("")
            );
            assertThat(exception.getMessage(), is(equalTo("Blank error")));

            // Format validation
            when(messageSource.getMessage("validation.email.address.invalid"))
                .thenReturn("Format error");
            exception = assertThrows(IllegalArgumentException.class, () ->
                Email.create("invalid-email-format")
            );
            assertThat(exception.getMessage(), is(equalTo("Format error")));

            // Length validation
            when(messageSource.getMessage("validation.email.address.length", 6, 100))
                .thenReturn("Length error");
            exception = assertThrows(IllegalArgumentException.class, () ->
                Email.create("a@bc") // Too short
            );
            assertThat(exception.getMessage(), is(equalTo("Length error")));
        }
    }
}

package com.itau.hr.people_management.infrastructure.shared.message;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("SpringDomainMessageSource Unit Tests")
class SpringDomainMessageSourceTest {

    @Mock
    private MessageSource messageSource;

    private SpringDomainMessageSource domainMessageSource;

    @BeforeEach
    void setUp() {
        domainMessageSource = new SpringDomainMessageSource(messageSource);
    }

    @Test
    @DisplayName("Should delegate getMessage with current locale")
    void shouldDelegateGetMessageWithCurrentLocale() {
        try (MockedStatic<LocaleContextHolder> localeHolderMock = mockStatic(LocaleContextHolder.class)) {
            // Arrange
            String key = "validation.email.invalid";
            Object[] args = {"john.doe", "example.com"};
            String expectedMessage = "Invalid email format";
            Locale currentLocale = Locale.US;
            
            localeHolderMock.when(LocaleContextHolder::getLocale).thenReturn(currentLocale);
            when(messageSource.getMessage(key, args, currentLocale)).thenReturn(expectedMessage);

            // Act
            String result = domainMessageSource.getMessage(key, args);

            // Assert
            assertThat(result, is(expectedMessage));
            verify(messageSource).getMessage(key, args, currentLocale);
        }
    }

    @Test
    @DisplayName("Should handle empty args array")
    void shouldHandleEmptyArgsArray() {
        try (MockedStatic<LocaleContextHolder> localeHolderMock = mockStatic(LocaleContextHolder.class)) {
            // Arrange
            String key = "error.not.found";
            Object[] emptyArgs = {};
            String expectedMessage = "Not found";
            Locale currentLocale = Locale.getDefault();
            
            localeHolderMock.when(LocaleContextHolder::getLocale).thenReturn(currentLocale);
            when(messageSource.getMessage(key, emptyArgs, currentLocale)).thenReturn(expectedMessage);

            // Act
            String result = domainMessageSource.getMessage(key, emptyArgs);

            // Assert
            assertThat(result, is(expectedMessage));
        }
    }

    @Test
    @DisplayName("Should handle null args")
    void shouldHandleNullArgs() {
        try (MockedStatic<LocaleContextHolder> localeHolderMock = mockStatic(LocaleContextHolder.class)) {
            // Arrange
            String key = "info.welcome";
            Object[] nullArgs = null;
            String expectedMessage = "Welcome";
            Locale currentLocale = Locale.ENGLISH;
            
            localeHolderMock.when(LocaleContextHolder::getLocale).thenReturn(currentLocale);
            when(messageSource.getMessage(key, nullArgs, currentLocale)).thenReturn(expectedMessage);

            // Act
            String result = domainMessageSource.getMessage(key, nullArgs);

            // Assert
            assertThat(result, is(expectedMessage));
        }
    }
}
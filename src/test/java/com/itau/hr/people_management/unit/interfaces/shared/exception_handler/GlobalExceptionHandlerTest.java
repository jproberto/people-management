package com.itau.hr.people_management.unit.interfaces.shared.exception_handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.itau.hr.people_management.domain.shared.exception.BusinessException;
import com.itau.hr.people_management.domain.shared.exception.ConflictException;
import com.itau.hr.people_management.domain.shared.exception.NotFoundException;
import com.itau.hr.people_management.domain.shared.message.DomainMessageSource;
import com.itau.hr.people_management.interfaces.shared.dto.ApiErrorResponse;
import com.itau.hr.people_management.interfaces.shared.exception_handler.GlobalExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    @Mock private DomainMessageSource messageSource;
    @Mock private HttpServletRequest request;
    @Mock private MethodArgumentNotValidException validationException;
    @Mock private BindingResult bindingResult;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        GlobalExceptionHandler.setMessageSource(messageSource);
        when(request.getRequestURI()).thenReturn("/api/v1/test");
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle BusinessException and return 400 BAD_REQUEST")
    void shouldHandleBusinessExceptionAndReturn400BadRequest() {
        // Arrange
        BusinessException exception = new BusinessException("business.error.test", "arg1");
        when(messageSource.getMessage("business.error.test", "arg1")).thenReturn("Business error message");

        // Act
        ResponseEntity<ApiErrorResponse> result = handler.handleBusinessException(exception, request);

        // Assert
        assertThat(result.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(result.getBody().getError(), is("Business Rule Violation"));
        assertThat(result.getBody().getMessages(), contains("Business error message"));
        assertThat(result.getBody().getPath(), is("/api/v1/test"));
        verify(messageSource).getMessage("business.error.test", "arg1");
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle NotFoundException and return 404 NOT_FOUND")
    void shouldHandleNotFoundExceptionAndReturn404NotFound() {
        // Arrange
        NotFoundException exception = new NotFoundException("resource.not.found", "123");
        when(messageSource.getMessage("resource.not.found", "123")).thenReturn("Resource not found");

        // Act
        ResponseEntity<ApiErrorResponse> result = handler.handleNotFoundException(exception, request);

        // Assert
        assertThat(result.getStatusCode(), is(HttpStatus.NOT_FOUND));
        assertThat(result.getBody().getError(), is("Resource Not Found"));
        assertThat(result.getBody().getMessages(), contains("Resource not found"));
        verify(messageSource).getMessage("resource.not.found", "123");
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle ConflictException and return 409 CONFLICT")
    void shouldHandleConflictExceptionAndReturn409Conflict() {
        // Arrange
        ConflictException exception = new ConflictException("resource.conflict", "duplicate");
        when(messageSource.getMessage("resource.conflict", "duplicate")).thenReturn("Resource conflict");

        // Act
        ResponseEntity<ApiErrorResponse> result = handler.handleConflictException(exception, request);

        // Assert
        assertThat(result.getStatusCode(), is(HttpStatus.CONFLICT));
        assertThat(result.getBody().getError(), is("Resource Conflict"));
        assertThat(result.getBody().getMessages(), contains("Resource conflict"));
        verify(messageSource).getMessage("resource.conflict", "duplicate");
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle MethodArgumentNotValidException and return 400 with field errors")
    void shouldHandleMethodArgumentNotValidExceptionAndReturn400WithFieldErrors() {
        // Arrange
        FieldError fieldError1 = new FieldError("employee", "name", "Name is required");
        FieldError fieldError2 = new FieldError("employee", "email", "Invalid email format");
        when(validationException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // Act
        ResponseEntity<ApiErrorResponse> result = handler.handleValidationExceptions(validationException, request);

        // Assert
        assertThat(result.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(result.getBody().getError(), is("Validation Error"));
        assertThat(result.getBody().getMessages(), hasSize(2));
        assertThat(result.getBody().getMessages(), contains("name: Name is required", "email: Invalid email format"));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle NoResourceFoundException and return 404")
    void shouldHandleNoResourceFoundExceptionAndReturn404() {
        // Arrange
        NoResourceFoundException exception = mock(NoResourceFoundException.class);
        when(exception.getMessage()).thenReturn("Resource not found");

        // Act
        ResponseEntity<ApiErrorResponse> result = handler.handleNoResourceFoundException(exception, request);

        // Assert
        assertThat(result.getStatusCode(), is(HttpStatus.NOT_FOUND));
        assertThat(result.getBody().getError(), is("Resource Not Found"));
        assertThat(result.getBody().getMessages(), contains("Resource not found"));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle HttpRequestMethodNotSupportedException and return 405")
    void shouldHandleHttpRequestMethodNotSupportedExceptionAndReturn405() {
        // Arrange
        HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("POST");

        // Act
        ResponseEntity<ApiErrorResponse> result = handler.handleHttpRequestMethodNotSupportedException(exception, request);

        // Assert
        assertThat(result.getStatusCode(), is(HttpStatus.METHOD_NOT_ALLOWED));
        assertThat(result.getBody().getError(), is("Method Not Allowed"));
        assertThat(result.getBody().getMessages(), hasItem(containsString("POST")));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle IllegalArgumentException and return 400")
    void shouldHandleIllegalArgumentExceptionAndReturn400() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument provided");

        // Act
        ResponseEntity<ApiErrorResponse> result = handler.handleIllegalArgumentException(exception, request);

        // Assert
        assertThat(result.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(result.getBody().getError(), is("Invalid Argument"));
        assertThat(result.getBody().getMessages(), contains("Invalid argument provided"));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Should handle generic Exception and return 500 INTERNAL_SERVER_ERROR")
    void shouldHandleGenericExceptionAndReturn500InternalServerError() {
        // Arrange
        RuntimeException exception = new RuntimeException("Unexpected error");

        // Act
        ResponseEntity<ApiErrorResponse> result = handler.handleGenericException(exception, request);

        // Assert
        assertThat(result.getStatusCode(), is(HttpStatus.INTERNAL_SERVER_ERROR));
        assertThat(result.getBody().getError(), is("Internal Server Error"));
        assertThat(result.getBody().getMessages(), hasSize(2));
        assertThat(result.getBody().getMessages(), hasItem("An unexpected error occurred. Please try again later."));
        assertThat(result.getBody().getMessages(), hasItem("Unexpected error"));
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("Should build error response with timestamp and correct structure")
    void shouldBuildErrorResponseWithTimestampAndCorrectStructure() {
        // Arrange
        BusinessException exception = new BusinessException("test.error");
        when(messageSource.getMessage("test.error")).thenReturn("Test error");
        Instant beforeCall = Instant.now();

        // Act
        ResponseEntity<ApiErrorResponse> result = handler.handleBusinessException(exception, request);

        // Assert
        ApiErrorResponse response = result.getBody();
        assertThat(response.getTimestamp(), is(greaterThanOrEqualTo(beforeCall)));
        assertThat(response.getStatus(), is(400));
        assertThat(response.getError(), is(notNullValue()));
        assertThat(response.getMessages(), is(notNullValue()));
        assertThat(response.getPath(), is("/api/v1/test"));
    }
}
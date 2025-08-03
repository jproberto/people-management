package com.itau.hr.people_management.interfaces.shared.exception_handler;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.itau.hr.people_management.domain.shared.exception.BusinessException;
import com.itau.hr.people_management.domain.shared.exception.ConflictException;
import com.itau.hr.people_management.domain.shared.exception.NotFoundException;
import com.itau.hr.people_management.domain.shared.message.DomainMessageSource;
import com.itau.hr.people_management.interfaces.shared.dto.ApiErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static DomainMessageSource messageSource;
    
    public static void setMessageSource(DomainMessageSource messageSource) {
        GlobalExceptionHandler.messageSource = messageSource;
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        String errorMessage = messageSource.getMessage(ex.getMessageKey(), ex.getArgs());
        return buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Business Rule Violation",
            Collections.singletonList(errorMessage),
            request
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFoundException(NotFoundException ex, HttpServletRequest request) {
        String errorMessage = messageSource.getMessage(ex.getMessageKey(), ex.getArgs());
        return buildErrorResponse(
            HttpStatus.NOT_FOUND,
            "Resource Not Found",
            Collections.singletonList(errorMessage),
            request
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflictException(ConflictException ex, HttpServletRequest request) {
        String errorMessage = messageSource.getMessage(ex.getMessageKey(), ex.getArgs());
        return buildErrorResponse(
            HttpStatus.CONFLICT,
            "Resource Conflict",
            Collections.singletonList(errorMessage),
            request
        );
    }
        
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errors = extractValidationErrors(ex);
        return buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Validation Error",
            errors,
            request
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(
            HttpStatus.NOT_FOUND,
            "Resource Not Found",
            Collections.singletonList(ex.getMessage()),
            request
        );
    }
    
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        return buildErrorResponse(
            HttpStatus.METHOD_NOT_ALLOWED,
            "Method Not Allowed",
            Collections.singletonList(ex.getMessage()),
            request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        List<String> messages = Arrays.asList("An unexpected error occurred. Please try again later.", ex.getMessage());
        return buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            messages,
            request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Invalid JSON format",
            Collections.singletonList(ex.getMessage()),
            request
        );
    }


    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String errorMessage = ex.getMessage(); 

        ApiErrorResponse error = new ApiErrorResponse(
            Instant.now(),
            status.value(),
            "Invalid Argument",
            Collections.singletonList(errorMessage),
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {
         return buildErrorResponse(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "Invalid Media Type",
            Collections.singletonList(ex.getMessage()),
            request
        );
    }

    private ResponseEntity<ApiErrorResponse> buildErrorResponse(HttpStatus status, String title, List<String> errors, HttpServletRequest request) {
        ApiErrorResponse error = new ApiErrorResponse(
            Instant.now(),
            status.value(),
            title,
            errors,
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, status);
    }

    private List<String> extractValidationErrors(MethodArgumentNotValidException ex) {
        return ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return fieldError.getField() + ": " + error.getDefaultMessage();
                    }
                    return error.getObjectName() + ": " + error.getDefaultMessage();
                })
                .toList();
    }
}
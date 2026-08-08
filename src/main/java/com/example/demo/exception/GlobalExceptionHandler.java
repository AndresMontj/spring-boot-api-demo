package com.example.demo.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handle validation failures
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, List<String>> errors = new HashMap<>();
        List<String> errorMessages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        errors.put("validationErrors", errorMessages);

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                "Validation Failed",
                ex.getMessage(),
                errors,
                request.getDescription(false));

        return handleExceptionInternal(ex, apiError, headers, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Handle BindException
     */
    protected ResponseEntity<Object> handleBindException(
            BindException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {

        Map<String, List<String>> errors = new HashMap<>();
        List<String> errorMessages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        errors.put("validationErrors", errorMessages);

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                "Validation Failed",
                ex.getMessage(),
                errors,
                request.getDescription(false));

        return handleExceptionInternal(ex, apiError, headers, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Handle custom exceptions
     */
    @ExceptionHandler({RuntimeException.class, IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Object> handleCustomExceptions(
            RuntimeException ex, WebRequest request) {

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                "Business Logic Error",
                ex.getMessage(),
                null,
                request.getDescription(false));

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle all other exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(
            Exception ex, WebRequest request) {

        log.error("Unhandled exception: {}", ex.getMessage(), ex);

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                "Internal Server Error",
                ex.getMessage(),
                null,
                request.getDescription(false));

        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle not found exceptions
     */
    @ExceptionHandler({java.util.NoSuchElementException.class})
    public ResponseEntity<Object> handleNotFound(
            RuntimeException ex, WebRequest request) {

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                "Resource Not Found",
                ex.getMessage(),
                null,
                request.getDescription(false));

        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    /**
     * API Error response structure
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiError {
        private LocalDateTime timestamp;
        private String status;
        private String message;
        @Nullable
        private Map<String, List<String>> errors;
        private String path;
    }
}
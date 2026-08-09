package com.example.demo.exception;

import com.example.demo.dto.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Centralized exception handling that always renders errors as a single,
 * consistent {@link ErrorResponse} JSON shape:
 * {@code { status, errorCode, message, timestamp, details } }
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handle @Valid @RequestBody validation failures.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.toList());

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed", details);

        return handleExceptionInternal(ex, errorResponse, headers, status, request);
    }

    /**
     * Handle constraint violations on path variables / request params (e.g. @Validated).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> details = ex.getConstraintViolations()
                .stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.toList());

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed", details);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle exceptions that already carry their own HTTP status (e.g. UserNotFoundException).
     * Registered explicitly so it takes precedence over the generic RuntimeException handling
     * below and the correct status (e.g. 404) is preserved instead of collapsing to 400.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String message = ex.getReason() != null ? ex.getReason() : ex.getMessage();

        ErrorResponse errorResponse = buildErrorResponse(status, status.name(), message, null);

        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Handle authentication failures (bad credentials, unknown user, etc.).
     * Uses a generic message regardless of cause to avoid leaking whether a
     * given username exists (user-enumeration protection).
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex) {
        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Invalid username or password", null);

        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle "not found" style lookups that don't carry their own HTTP status.
     */
    @ExceptionHandler(java.util.NoSuchElementException.class)
    public ResponseEntity<Object> handleNotFound(java.util.NoSuchElementException ex) {
        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), null);

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle common invalid-input business exceptions that don't carry their own HTTP status.
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Object> handleBadRequestExceptions(RuntimeException ex) {
        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), null);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Fallback for any other unhandled exception.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Unhandled exception at [{}]: {}", request.getDescription(false), ex.getMessage(), ex);

        ErrorResponse errorResponse = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred", null);

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Ensures every exception delegated to the parent {@link ResponseEntityExceptionHandler}
     * (malformed JSON, unsupported media type, missing params, method not supported, etc.)
     * is rendered with the same {@link ErrorResponse} JSON shape instead of the framework's
     * default ProblemDetail body.
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {

        Object responseBody = body;
        if (!(responseBody instanceof ErrorResponse)) {
            HttpStatus status = HttpStatus.valueOf(statusCode.value());
            responseBody = buildErrorResponse(status, status.name(), ex.getMessage(), null);
        }
        return super.handleExceptionInternal(ex, responseBody, headers, statusCode, request);
    }

    private ErrorResponse buildErrorResponse(HttpStatus status, String errorCode, String message, List<String> details) {
        return ErrorResponse.builder()
                .status(status.value())
                .errorCode(errorCode)
                .message(message)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();
    }
}

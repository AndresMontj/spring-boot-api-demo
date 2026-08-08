package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Exception thrown when a user is not found.
 */
public class UserNotFoundException extends ResponseStatusException {

    public UserNotFoundException(Long id) {
        super(HttpStatus.NOT_FOUND, "User not found with id: " + id);
    }

    public UserNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }

    @Override
    public String getMessage() {
        return super.getReason();
    }
}
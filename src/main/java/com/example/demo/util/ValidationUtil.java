package com.example.demo.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for validation operations
 */
public final class ValidationUtil {

    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private static final Validator validator = factory.getValidator();

    private ValidationUtil() {
        // Prevent instantiation
    }

    /**
     * Validate an object and return constraint violations
     *
     * @param object Object to validate
     * @param <T>    Type of object
     * @return Set of constraint violation messages
     */
    public static <T> Set<String> validate(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }

    /**
     * Check if object is valid
     *
     * @param object Object to validate
     * @param <T>    Type of object
     * @return true if valid, false otherwise
     */
    public static <T> boolean isValid(T object) {
        return validate(object).isEmpty();
    }
}
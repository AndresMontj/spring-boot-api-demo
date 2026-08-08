package com.example.demo.util;

public final class AppConstants {

    // Application Constants
    public static final String APP_NAME = "Spring Boot API Demo";
    public static final String APP_VERSION = "1.0.0";

    // Security Constants
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";

    // API Constants
    public static final String API_BASE_URL = "/api";
    public static final String API_USERS_URL = API_BASE_URL + "/users";
    public static final String API_AUTH_URL = API_BASE_URL + "/auth";
    public static final String API_CIRCUIT_BREAKER_URL = API_BASE_URL + "/circuit-breaker";
    public static final String API_OAUTH2_URL = API_BASE_URL + "/oauth2";

    // Database Constants
    public static final String TABLE_USERS = "users";

    // Error Messages
    public static final String ERROR_USER_NOT_FOUND = "User not found with id: ";
    public static final String ERROR_INVALID_TOKEN = "Invalid JWT token";
    public static final String ERROR_EXPIRED_TOKEN = "Expired JWT token";
    public static final String ERROR_UNSUPPORTED_TOKEN = "Unsupported JWT token";
    public static final String ERROR_MALFORMED_TOKEN = "Malformed JWT token";

    // Success Messages
    public static final String SUCCESS_USER_CREATED = "User created successfully";
    public static final String SUCCESS_USER_UPDATED = "User updated successfully";
    public static final String SUCCESS_USER_DELETED = "User deleted successfully";

    // Pagination Constants
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    private AppConstants() {
        // Prevent instantiation
    }
}
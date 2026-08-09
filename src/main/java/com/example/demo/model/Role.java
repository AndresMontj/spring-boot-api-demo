package com.example.demo.model;

/**
 * Application roles. Stored on {@link User} and mapped to Spring Security
 * authorities as {@code ROLE_<name>} (see UserDetailsServiceImpl).
 */
public enum Role {
    ADMIN,
    USER
}

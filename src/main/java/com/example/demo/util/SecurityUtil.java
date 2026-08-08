package com.example.demo.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

/**
 * Utility class for security operations
 */
@Component
@Slf4j
public class SecurityUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    /**
     * Get current authenticated user's username
     *
     * @return username or null if not authenticated
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }

    /**
     * Get current authenticated user's authorities
     *
     * @return authorities or empty list if not authenticated
     */
    public Object getCurrentAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities();
        }
        return null;
    }

    /**
     * Check if current user has a specific role
     *
     * @param role Role to check
     * @return true if user has role, false otherwise
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + role));
        }
        return false;
    }

    /**
     * Check if current user has any of the specified roles
     *
     * @param roles Roles to check
     * @return true if user has any of the roles, false otherwise
     */
    public boolean hasAnyRole(String... roles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> {
                        for (String role : roles) {
                            if (grantedAuthority.getAuthority().equals("ROLE_" + role)) {
                                return true;
                            }
                        }
                        return false;
                    });
        }
        return false;
    }

    /**
     * Validate JWT token and return claims if valid
     *
     * @param token JWT token
     * @return Claims if valid, null otherwise
     */
    public Claims validateTokenAndGetClaims(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims;
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Check if JWT token is expired
     *
     * @param token JWT token
     * @return true if expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Get expiration date from JWT token
     *
     * @param token JWT token
     * @return expiration date
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration();
    }

    /**
     * Get username from JWT token
     *
     * @param token JWT token
     * @return username
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * Generate signing key for JWT
     *
     * @return signing key
     */
    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
        return io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);
    }
}
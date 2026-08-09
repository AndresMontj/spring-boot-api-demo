package com.example.demo.config;

import com.example.demo.dto.ErrorResponse;
import com.example.demo.security.JwtAuthenticationFilter;
import com.example.demo.security.JwtUtils;
import com.example.demo.security.UserDetailsServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtils jwtUtils;

    // Dedicated instance (rather than the shared app ObjectMapper bean) since this only ever
    // needs to render a single, simple ErrorResponse body for unauthenticated requests.
    // findAndRegisterModules() picks up JSR-310 support, and disabling WRITE_DATES_AS_TIMESTAMPS
    // matches Spring Boot's default Jackson behavior so timestamps render as ISO-8601 strings
    // consistently with every other ErrorResponse produced by GlobalExceptionHandler.
    private final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception
                        // Both handlers write the ErrorResponse body directly (instead of calling
                        // response.sendError(), which would trigger a container-level forward to
                        // /error - a request the JwtAuthenticationFilter does not re-run for, so it
                        // would come back in as anonymous and get reported as 401 instead of 403).
                        .authenticationEntryPoint((request, response, authException) -> writeJsonError(
                                response, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                                "Authentication is required to access this resource"))
                        .accessDeniedHandler((request, response, accessDeniedException) -> writeJsonError(
                                response, HttpStatus.FORBIDDEN, "FORBIDDEN",
                                "You do not have permission to access this resource")))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/h2-console/**",
                                "/auth/**",
                                "/actuator/health",
                                "/actuator/info",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        // Remaining actuator endpoints (metrics, prometheus, circuitbreakers, etc.)
                        // require an authenticated admin
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                );

        // Add JWT token filter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        // Enable H2 console
        http.headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions
                        .sameOrigin()
                )
        );

        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(userDetailsService, jwtUtils);
    }

    private void writeJsonError(HttpServletResponse response, HttpStatus status, String errorCode, String message)
            throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status.value())
                .errorCode(errorCode)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}

package com.example.demo.controller;

import com.example.demo.dto.auth.JwtRequest;
import com.example.demo.dto.auth.JwtResponse;
import com.example.demo.security.JwtUtils;
import com.example.demo.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {

        log.debug("Authentication attempt for username: {}", authenticationRequest.getUsername());
        log.debug("Request body: username='{}', password='{}'",
                  authenticationRequest.getUsername(),
                  authenticationRequest.getPassword());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authenticationRequest.getUsername(),
                            authenticationRequest.getPassword()
                    )
            );
            log.debug("Authentication successful for username: {}", authenticationRequest.getUsername());
        } catch (BadCredentialsException e) {
            log.debug("Bad credentials for username: {}", authenticationRequest.getUsername());
            throw new Exception("Incorrect username or password", e);
        }

        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());

        log.debug("Loaded user details for username: {}", userDetails.getUsername());

        final String jwt = jwtUtils.generateTokenFromUsername(userDetails.getUsername());

        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getUsername(), "USER"));
    }

    // For demonstration purposes - token refresh endpoint
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody JwtRequest request) {
        // In a real application, you would validate the refresh token and issue a new access token
        // For simplicity, we're just re-authenticating here
        log.debug("Token refresh attempt for username: {}", request.getUsername());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            log.debug("Bad credentials for token refresh: {}", request.getUsername());
            return ResponseEntity.badRequest().body("Invalid credentials");
        }

        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(request.getUsername());

        final String jwt = jwtUtils.generateTokenFromUsername(userDetails.getUsername());

        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getUsername(), "USER"));
    }
}
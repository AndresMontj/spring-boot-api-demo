package com.example.demo.controller;

import com.example.demo.service.ExternalService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/circuit-breaker")
@RequiredArgsConstructor
@Tag(name = "Circuit Breaker", description = "APIs demonstrating circuit breaker pattern")
public class CircuitBreakerController {

    private final ExternalService externalService;

    @Operation(summary = "Call external service with circuit breaker", description = "Demonstrates circuit breaker pattern with Resilience4j")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully called external service",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/call")
    public CompletableFuture<ResponseEntity<String>> callExternalService() {
        return externalService.callExternalService()
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(500).body("Error: " + ex.getMessage()));
    }

    // Additional resilience patterns
    @Retry(name = "externalService", fallbackMethod = "retryFallback")
    @GetMapping("/retry")
    public CompletableFuture<ResponseEntity<String>> callWithRetry() {
        return externalService.callExternalService()
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(500).body("Error: " + ex.getMessage()));
    }

    private CompletableFuture<ResponseEntity<String>> retryFallback(Throwable throwable) {
        return CompletableFuture.completedFuture(ResponseEntity.ok("Retry fallback response"));
    }

    @RateLimiter(name = "externalService")
    @GetMapping("/rate-limited")
    public CompletableFuture<ResponseEntity<String>> callWithRateLimit() {
        return externalService.callExternalService()
                .thenApply(ResponseEntity::ok)
                .exceptionally(ex -> ResponseEntity.status(500).body("Error: " + ex.getMessage()));
    }
}
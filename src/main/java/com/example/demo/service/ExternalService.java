package com.example.demo.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class ExternalService {

    @CircuitBreaker(name = "externalService", fallbackMethod = "fallback")
    @TimeLimiter(name = "externalService")
    public CompletableFuture<String> callExternalService() {
        // Simulate external service call that might fail or timeout
        return CompletableFuture.supplyAsync(() -> {
            // Simulate random failures for demonstration
            if (Math.random() < 0.3) { // 30% chance of failure
                throw new RuntimeException("External service failed");
            }
            // Simulate processing time
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while processing");
            }
            return "External service response";
        });
    }

    // Fallback method
    public CompletableFuture<String> fallback(Throwable throwable) {
        return CompletableFuture.completedFuture("Fallback response: External service is unavailable");
    }
}
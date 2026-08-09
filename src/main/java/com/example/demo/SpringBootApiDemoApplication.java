package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Objects;
import java.util.Optional;

@SpringBootApplication
@EnableJpaAuditing
public class SpringBootApiDemoApplication {

    static void main(String[] args) {
        SpringApplication.run(SpringBootApiDemoApplication.class, args);
    }

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (Objects.isNull(authentication) || !authentication.isAuthenticated()) {
                return Optional.ofNullable("SYSTEM");
            }
            if (authentication.getPrincipal() instanceof UserDetails userDetails) {
                return Optional.ofNullable(userDetails.getUsername());
            }
            return Optional.ofNullable(authentication.getName());
        };
    }
}
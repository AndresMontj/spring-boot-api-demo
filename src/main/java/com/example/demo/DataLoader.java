package com.example.demo;

import com.example.demo.dto.UserDto;
import com.example.demo.model.Role;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    private final UserRepository userRepository;
    private final UserService userService;

    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            // Create initial users if none exist
            if (userRepository.count() == 0) {
                UserDto adminUser = UserDto.builder()
                        .username("admin")
                        .email("admin@example.com")
                        .password("admin123")
                        .firstName("Admin")
                        .lastName("User")
                        .role(Role.ADMIN)
                        .build();

                UserDto regularUser = UserDto.builder()
                        .username("user")
                        .email("user@example.com")
                        .password("user123")
                        .firstName("Regular")
                        .lastName("User")
                        .role(Role.USER)
                        .build();

                userService.createUser(adminUser);
                userService.createUser(regularUser);

                log.info("Initialized database with default admin and user accounts");
            }
        };
    }
}

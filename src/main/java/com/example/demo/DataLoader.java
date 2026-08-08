package com.example.demo;

import com.example.demo.dto.UserDto;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataLoader {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
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
                        .role("ADMIN")
                        .build();

                UserDto regularUser = UserDto.builder()
                        .username("user")
                        .email("user@example.com")
                        .password("user123")
                        .firstName("Regular")
                        .lastName("User")
                        .role("USER")
                        .build();

                UserDto savedAdmin = userService.createUser(adminUser);
                UserDto savedUser = userService.createUser(regularUser);

                System.out.println("Initialized database with admin and user accounts");
                System.out.println("Admin password encoded: " + savedAdmin.getPassword());
                System.out.println("User password encoded: " + savedUser.getPassword());
            }
        };
    }
}
package com.example.demo.repository;

import com.example.demo.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    void findByUsername_ShouldReturnUser() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole("USER");

        userRepository.save(user);

        // Act
        Optional<User> found = userRepository.findByUsername("testuser");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @Transactional
    void findByEmail_ShouldReturnUser() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole("USER");

        userRepository.save(user);

        // Act
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @Transactional
    void save_ShouldPersistUser() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole("USER");

        // Act
        User savedUser = userRepository.save(user);

        // Assert
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @Transactional
    void deleteById_ShouldRemoveUser() {
        // Arrange
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole("USER");

        User saved = userRepository.save(user);
        Long userId = saved.getId();

        // Act
        userRepository.deleteById(userId);

        // Assert
        Optional<User> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isEmpty();
    }
}
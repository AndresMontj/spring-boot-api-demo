package com.example.demo.service.impl;

import com.example.demo.dto.UserDto;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDto userDto;
    private User user;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .role("USER")
                .build();

        user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("encodedPassword")
                .firstName("Test")
                .lastName("User")
                .role("USER")
                .build();
    }

    @Test
    void getAllUsers_ShouldReturnListOfUserDtos() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Arrays.asList(user));
        when(userRepository.findAll()).thenReturn(List.of(user));

        // Act
        List<UserDto> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        assertEquals("test@example.com", result.get(0).getEmail());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getUserById_ExistingUser_ShouldReturnUserDto() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        UserDto result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserById_NonExistingUser_ShouldThrowUserNotFoundException() {
        // Arrange
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.getUserById(2L);
        });
        assertEquals("User not found with id: 2", exception.getMessage());
        verify(userRepository, times(1)).findById(2L);
    }

    @Test
    void createUser_ShouldReturnUserDto() {
        // Arrange
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        UserDto result = userService.createUser(userDto);

        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("encodedPassword", result.getPassword()); // Password is encoded in DTO
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_ExistingUser_ShouldReturnUpdatedUserDto() {
        // Arrange
        UserDto updatedDto = UserDto.builder()
                .id(1L)
                .username("updateduser")
                .email("updated@example.com")
                .password("newpassword123")
                .firstName("Updated")
                .lastName("User")
                .role("ADMIN")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpassword123")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setUsername("updateduser");
            savedUser.setEmail("updated@example.com");
            savedUser.setPassword("newEncodedPassword");
            savedUser.setFirstName("Updated");
            savedUser.setLastName("User");
            savedUser.setRole("ADMIN");
            return savedUser;
        });

        // Act
        UserDto result = userService.updateUser(1L, updatedDto);

        // Assert
        assertNotNull(result);
        assertEquals("updateduser", result.getUsername());
        assertEquals("updated@example.com", result.getEmail());
        assertEquals("newEncodedPassword", result.getPassword());
        verify(userRepository, times(1)).findById(1L);
        verify(passwordEncoder, times(1)).encode("newpassword123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_NonExistingUser_ShouldThrowUserNotFoundException() {
        // Arrange
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.updateUser(2L, userDto);
        });
        assertEquals("User not found with id: 2", exception.getMessage());
        verify(userRepository, times(1)).findById(2L);
    }

    @Test
    void deleteUser_ExistingUser_ShouldNotThrowException() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        // Act
        assertDoesNotThrow(() -> userService.deleteUser(1L));

        // Assert
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_NonExistingUser_ShouldThrowUserNotFoundException() {
        // Arrange
        when(userRepository.existsById(2L)).thenReturn(false);

        // Act & Assert
        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            userService.deleteUser(2L);
        });
        assertEquals("User not found with id: 2", exception.getMessage());
        verify(userRepository, times(1)).existsById(2L);
        verify(userRepository, never()).deleteById(anyLong());
    }
}
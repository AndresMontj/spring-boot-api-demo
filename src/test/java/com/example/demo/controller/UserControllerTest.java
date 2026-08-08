package com.example.demo.controller;

import com.example.demo.dto.UserDto;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .defaultRequest(get("/").contextPath("/api"))
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .role("USER")
                .build();
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() throws Exception {
        // Arrange
        when(userService.getAllUsers()).thenReturn(Arrays.asList(userDto));

        // Act & Assert
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is("testuser")))
                .andExpect(jsonPath("$[0].email", is("test@example.com")));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void getUserById_ExistingUser_ShouldReturnUser() throws Exception {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(userDto);

        // Act & Assert
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")));

        verify(userService, times(1)).getUserById(1L);
    }

    @Test
    void getUserById_NonExistingUser_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(userService.getUserById(2L)).thenThrow(new UserNotFoundException(2L));

        // Act & Assert
        mockMvc.perform(get("/api/users/2"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(2L);
    }

    @Test
    void createUser_ValidUserDto_ShouldReturnCreatedUser() throws Exception {
        // Arrange
        when(userService.createUser(any(UserDto.class))).thenReturn(userDto);

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.email", is("test@example.com")));

        verify(userService, times(1)).createUser(any(UserDto.class));
    }

    @Test
    void createUser_InvalidUserDto_ShouldReturnBadRequest() throws Exception {
        // Arrange
        UserDto invalidDto = UserDto.builder()
                .username("ab") // Too short (min 3)
                .email("invalid-email")
                .password("123") // Too short (min 8)
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(UserDto.class));
    }

    @Test
    void updateUser_ExistingUser_ShouldReturnUpdatedUser() throws Exception {
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

        when(userService.updateUser(eq(1L), any(UserDto.class))).thenReturn(updatedDto);

        // Act & Assert
        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username", is("updateduser")))
                .andExpect(jsonPath("$.email", is("updated@example.com")))
                .andExpect(jsonPath("$.role", is("ADMIN")));

        verify(userService, times(1)).updateUser(eq(1L), any(UserDto.class));
    }

    @Test
    void updateUser_NonExistingUser_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(userService.updateUser(eq(2L), any(UserDto.class))).thenThrow(new UserNotFoundException(2L));

        // Act & Assert
        mockMvc.perform(put("/api/users/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).updateUser(eq(2L), any(UserDto.class));
    }

    @Test
    void deleteUser_ExistingUser_ShouldReturnNoContent() throws Exception {
        // Arrange
        doNothing().when(userService).deleteUser(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void deleteUser_NonExistingUser_ShouldReturnNotFound() throws Exception {
        // Arrange
        doThrow(new UserNotFoundException(2L)).when(userService).deleteUser(2L);

        // Act & Assert
        mockMvc.perform(delete("/api/users/2"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).deleteUser(2L);
    }
}
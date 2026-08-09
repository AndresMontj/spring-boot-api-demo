package com.example.demo.controller;

import com.example.demo.dto.UserDto;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.model.Role;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
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

        // Configure Pageable argument resolver for Spring Data web support
        PageableHandlerMethodArgumentResolver pageableResolver = new PageableHandlerMethodArgumentResolver();
        pageableResolver.setFallbackPageable(PageRequest.of(0, 10));

        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(pageableResolver)
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .firstName("Test")
                .lastName("User")
                .role(Role.USER)
                .build();
    }

    /**
     * UserDto.password is @JsonProperty(WRITE_ONLY) so it's never serialized when the DTO is
     * used as a response body - but that also means objectMapper.writeValueAsString(dto) can't
     * be used to build a *request* payload that needs to carry a password. This helper builds
     * the outgoing JSON directly from a Map (not subject to UserDto's Jackson annotations) so
     * tests can still send a password to the server, just like a real HTTP client would.
     */
    private String toRequestJson(UserDto dto) throws Exception {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("id", dto.getId());
        fields.put("username", dto.getUsername());
        fields.put("email", dto.getEmail());
        fields.put("password", dto.getPassword());
        fields.put("firstName", dto.getFirstName());
        fields.put("lastName", dto.getLastName());
        fields.put("role", dto.getRole());
        return objectMapper.writeValueAsString(fields);
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() throws Exception {
        // Arrange
        Page<UserDto> page = new PageImpl<>(Arrays.asList(userDto), PageRequest.of(0, 10), 1);
        when(userService.getAllUsers(any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/v1/users").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].username", is("testuser")))
                .andExpect(jsonPath("$.content[0].email", is("test@example.com")));

        verify(userService, times(1)).getAllUsers(any(Pageable.class));
    }

    @Test
    void getUserById_ExistingUser_ShouldReturnUser() throws Exception {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(userDto);

        // Act & Assert
        mockMvc.perform(get("/v1/users/1"))
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
        mockMvc.perform(get("/v1/users/2"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getUserById(2L);
    }

    @Test
    void createUser_ValidUserDto_ShouldReturnCreatedUser() throws Exception {
        // Arrange
        when(userService.createUser(any(UserDto.class))).thenReturn(userDto);

        // Act & Assert
        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toRequestJson(userDto)))
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
        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).createUser(any(UserDto.class));
    }

    @Test
    void createUser_UnauthenticatedCallerRequestsAdminRole_ShouldBeDowngradedToUser() throws Exception {
        // Arrange: no SecurityContext authentication is set up (simulates a non-admin caller)
        UserDto adminRequest = UserDto.builder()
                .username("wannabeadmin")
                .email("wannabe@example.com")
                .password("password123")
                .role(Role.ADMIN)
                .build();
        when(userService.createUser(any(UserDto.class))).thenReturn(userDto);

        // Act
        mockMvc.perform(post("/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toRequestJson(adminRequest)))
                .andExpect(status().isCreated());

        // Assert: the role actually forwarded to the service must have been downgraded to USER
        ArgumentCaptor<UserDto> captor = ArgumentCaptor.forClass(UserDto.class);
        verify(userService, times(1)).createUser(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(Role.USER);
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
                .role(Role.ADMIN)
                .build();

        when(userService.updateUser(eq(1L), any(UserDto.class))).thenReturn(updatedDto);

        // Act & Assert
        mockMvc.perform(put("/v1/users/1")
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
        mockMvc.perform(put("/v1/users/2")
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
        mockMvc.perform(delete("/v1/users/1"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void deleteUser_NonExistingUser_ShouldReturnNotFound() throws Exception {
        // Arrange
        doThrow(new UserNotFoundException(2L)).when(userService).deleteUser(2L);

        // Act & Assert
        mockMvc.perform(delete("/v1/users/2"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).deleteUser(2L);
    }
}
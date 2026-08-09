package com.example.demo.controller;

import com.example.demo.dto.OnCreate;
import com.example.demo.dto.UserDto;
import com.example.demo.model.Role;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get all users", description = "Retrieve a list of all users with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<Page<UserDto>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @Operation(summary = "Get user by ID", description = "Retrieve a user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved user",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class),
                            examples = @ExampleObject(
                                    name = "User response",
                                    value = """
                                            {
                                                "id": 1,
                                                "username": "john_doe",
                                                "email": "john@example.com",
                                                "firstName": "John",
                                                "lastName": "Doe",
                                                "role": "USER"
                                            }
                                            """))),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "Create a new user", description = "Create a new user with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully created user",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class),
                            examples = @ExampleObject(
                                    name = "Created user",
                                    value = """
                                            {
                                                "id": 1,
                                                "username": "jane_smith",
                                                "email": "jane@example.com",
                                                "firstName": "Jane",
                                                "lastName": "Smith",
                                                "role": "USER"
                                            }
                                            """))),
            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Validation error",
                                    value = """
                                            {
                                                "status": 400,
                                                "errorCode": "VALIDATION_ERROR",
                                                "message": "Validation failed",
                                                "timestamp": "2026-08-08T10:30:00",
                                                "details": [
                                                    "email: Email should be valid",
                                                    "password: Password must be at least 8 characters long"
                                                ]
                                            }
                                            """))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Validated({Default.class, OnCreate.class}) @RequestBody UserDto userDto) {
        enforceRoleAssignmentPermission(userDto);
        return ResponseEntity.status(201).body(userService.createUser(userDto));
    }

    @Operation(summary = "Update an existing user", description = "Update an existing user with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated user",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class),
                            examples = @ExampleObject(
                                    name = "Updated user",
                                    value = """
                                            {
                                                "id": 1,
                                                "username": "jane_smith_updated",
                                                "email": "jane.updated@example.com",
                                                "firstName": "Jane",
                                                "lastName": "Smith Updated",
                                                "role": "ADMIN"
                                            }
                                            """))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {
        enforceRoleAssignmentPermission(userDto);
        return ResponseEntity.ok(userService.updateUser(id, userDto));
    }

    @Operation(summary = "Delete a user", description = "Delete a user by their ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Successfully deleted user"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Prevents privilege escalation: only a caller already holding ROLE_ADMIN
     * may create or promote a user to ADMIN. Everyone else is forced to USER,
     * regardless of what the request body asked for.
     */
    private void enforceRoleAssignmentPermission(UserDto userDto) {
        if (userDto.getRole() == Role.ADMIN && !currentUserIsAdmin()) {
            userDto.setRole(Role.USER);
        }
    }

    private boolean currentUserIsAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}

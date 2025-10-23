package net.sphuta.tms.freelancer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.constants.TmsMessages;
import net.sphuta.tms.freelancer.dto.TmsUserDto;
import net.sphuta.tms.freelancer.response.TmsApiResponse;
import net.sphuta.tms.freelancer.service.TmsUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for User APIs.
 * <p>
 * Provides endpoints to manage users including creation, retrieval, update, partial update, and deletion.
 * All responses are standardized using SphutaTmsApiResponse and include success flag, status code, status text, message, data, and timestamp.
 * Logging is enabled for each request to aid in debugging and tracing.
 */
@RestController
@RequestMapping("/api/v1")
@Slf4j
public class TmsUserController {

    @Autowired
    private TmsUserService tmsUserService;

    /**
     * Create a new user in the system.
     *
     * @param request UserRequest containing fields like email, fullName, passwordHash, phone, status, emailVerified, timezone, locale
     * @return SphutaTmsApiResponse containing the created UserResponse
     */
    @PostMapping("/users")
    @Operation(summary = "Create a new user",
            description = "Adds a new user to the system. Returns the created user with HTTP 201 status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully with all details"),
            @ApiResponse(responseCode = "400", description = "Validation failed or email already exists")
    })
    public TmsApiResponse<TmsUserDto> createUser(@Valid @RequestBody TmsUserDto request) {
        log.info("POST /api/users called with email: {}", request.email());
        var createdUser = tmsUserService.createUser(request);
        return TmsApiResponse.created(TmsMessages.USER_CREATED_SUCCESS, createdUser);
    }

    /**
     * Retrieve a user by their unique ID.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID",
            description = "Fetches user details by user ID. Returns 404 if user does not exist.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public TmsApiResponse<TmsUserDto> getUserById(@PathVariable Integer id) {
        log.info("GET /api/users/{} called", id);
        var user = tmsUserService.getUserById(id);
        return TmsApiResponse.success(TmsMessages.USER_RETRIEVED_SUCCESS, user);
    }

    /**
     * Retrieve all users in the system.
     */
    @GetMapping("/users")
    @Operation(summary = "Get all users",
            description = "Fetches a list of all users in the system. Returns empty list if no users exist.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All users retrieved successfully")
    })
    public TmsApiResponse<List<TmsUserDto>> getAllUsers() {
        log.info("GET /api/users called");
        var users = tmsUserService.getAllUsers();
        return TmsApiResponse.success(TmsMessages.USER_RETRIEVED_ALL_SUCCESS, users);
    }

    /**
     * Full update of an existing user.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a user (full update)",
            description = "Updates all fields of an existing user. Returns 404 if user not found.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public TmsApiResponse<TmsUserDto> updateUser(@PathVariable Integer id,
                                                 @Valid @RequestBody TmsUserDto request) {
        log.info("PUT /api/users/{} called", id);
        var updatedUser = tmsUserService.updateUser(id, request);
        return TmsApiResponse.success(TmsMessages.USER_UPDATED_SUCCESS, updatedUser);
    }

    /**
     * Delete a user by ID.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user by ID",
            description = "Removes a user from the system. Returns 404 if user not found.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public TmsApiResponse<Void> deleteUser(@PathVariable Integer id) {
        log.info("DELETE /api/users/{} called", id);
        tmsUserService.deleteUser(id);
        log.debug("User with ID {} deleted successfully", id);
        return TmsApiResponse.success(TmsMessages.USER_DELETED_SUCCESS, null);
    }
}

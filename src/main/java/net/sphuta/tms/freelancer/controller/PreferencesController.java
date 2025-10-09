package net.sphuta.tms.freelancer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.PreferencesDto;
import net.sphuta.tms.freelancer.response.TmsApiResponse;
import net.sphuta.tms.freelancer.service.SettingsPreferencesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static net.sphuta.tms.freelancer.constants.TmsMessages.*;

/**
 * <h2>PreferencesController</h2>
 *
 * REST Controller for managing {@link PreferencesDto}.
 * <p>
 * This controller exposes CRUD APIs for user preferences such as:
 * <ul>
 *   <li>Date format (YYYY-MM-DD, DD-MM-YYYY, etc.)</li>
 *   <li>Week start day (MON, SUN)</li>
 *   <li>Rounding rules (NONE, NEAREST_6, NEAREST_15, NEAREST_30)</li>
 * </ul>
 *
 * Each API is documented with Swagger annotations and follows standard
 * response patterns via {@link TmsApiResponse}.
 * </p>
 *
 * @author
 * @version 1.0
 * @since 2025
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/settings/preferences")
@Tag(name = "Preferences API", description = "Manage user preferences (date format, week start, rounding)")
public class PreferencesController {

    /**
     * Service dependency for handling business logic related to preferences.
     */
    @Autowired
    private SettingsPreferencesService settingsPreferencesService;

    // -------------------- GET ALL --------------------

    /**
     * Fetches all stored user preferences.
     *
     * @return {@link TmsApiResponse} containing a list of {@link PreferencesDto}
     */
    @GetMapping
    @Operation(summary = "Get Preferences", description = "Fetch preferences for all users")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Preferences fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public TmsApiResponse<List<PreferencesDto>> getAllPreferences() {
        log.debug("Fetching all preferences records");
        return TmsApiResponse.success(
                PREFERENCES_FETCH_ALL_SUCCESS,
                settingsPreferencesService.getAllPreferences()
        );
    }

    // -------------------- GET BY ID --------------------

    /**
     * Fetches preferences for a specific user by {@code userId}.
     *
     * @param userId unique identifier of the user
     * @return {@link TmsApiResponse} containing {@link PreferencesDto}
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get Preferences by ID", description = "Fetch preferences for a specific user by userId")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Preferences fetched successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Preferences not found for the given user"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid userId"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public TmsApiResponse<PreferencesDto> getPreferencesById(@PathVariable int userId) {
        log.debug("Fetching preferences by ID for user: {}", userId);
        return TmsApiResponse.success(
                PREFERENCES_FETCH_SUCCESS,
                settingsPreferencesService.getPreferences(userId)
        );
    }

    // -------------------- POST --------------------

    /**
     * Creates new preferences for a user.
     *
     * @param request {@link PreferencesDto} object containing new preferences data
     * @return {@link TmsApiResponse} containing created {@link PreferencesDto}
     */
    @PostMapping
    @Operation(summary = "Create Preferences", description = "Create preferences for a new user")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Preferences created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Preferences already exist for the user"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public TmsApiResponse<PreferencesDto> createPreferences(
            @Valid @RequestBody PreferencesDto request) {
        log.info("Creating preferences for user: {}", request.userId());
        return TmsApiResponse.success(
                PREFERENCES_CREATE_SUCCESS,
                settingsPreferencesService.createPreferences(request)
        );
    }

    // -------------------- PUT --------------------

    /**
     * Updates all preferences for a given user.
     *
     * @param userId  unique identifier of the user
     * @param request {@link PreferencesDto} object containing updated preferences
     * @return {@link TmsApiResponse} containing updated {@link PreferencesDto}
     */
    @PutMapping("/{userId}")
    @Operation(summary = "Update Preferences", description = "Update all fields of user preferences")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Preferences updated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request body"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Preferences not found for the given user"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public TmsApiResponse<PreferencesDto> updatePreferences(
            @PathVariable int userId,
            @Valid @RequestBody PreferencesDto request) {
        log.info("Updating all preferences for user: {}", userId);
        return TmsApiResponse.success(
                PREFERENCES_UPDATE_SUCCESS,
                settingsPreferencesService.updatePreferences(userId, request)
        );
    }

    // -------------------- DELETE --------------------

    /**
     * Deletes preferences for a specific user.
     *
     * @param userId unique identifier of the user
     * @return {@link TmsApiResponse} with a success message
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete Preferences", description = "Delete preferences for a user")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Preferences deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Preferences not found for the given user"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public TmsApiResponse<Void> deletePreferences(@PathVariable int userId) {
        log.warn("Deleting preferences for user: {}", userId);
        settingsPreferencesService.deletePreferences(userId);
        return TmsApiResponse.success(PREFERENCES_DELETE_SUCCESS, null);
    }
}

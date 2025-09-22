package net.sphuta.tms.freelancer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.constants.ApiMessageConstants;
import net.sphuta.tms.freelancer.dto.SettingsInvoicingDTO;
import net.sphuta.tms.freelancer.exception.NotFoundException;
import net.sphuta.tms.freelancer.response.ApiResponse;
import net.sphuta.tms.freelancer.service.SettingsInvoicingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * {@code InvoicingSettingsController} is a REST controller that exposes endpoints
 * for managing user-specific invoicing settings.
 *
 * <p>Features provided:
 * <ul>
 *   <li>Fetch all invoicing settings</li>
 *   <li>Fetch invoicing settings for a specific user</li>
 *   <li>Create new invoicing settings</li>
 *   <li>Update existing invoicing settings (full or partial)</li>
 *   <li>Delete invoicing settings by user ID</li>
 * </ul>
 *
 * <p>This controller uses:
 * <ul>
 *   <li>{@link SettingsInvoicingService} for business logic</li>
 *   <li>{@link ApiResponse} as a standard response wrapper</li>
 *   <li>Swagger annotations for API documentation</li>
 *   <li>SLF4J logging for request tracing</li>
 * </ul>
 *
 * <p>Base endpoint: <b>/api/v1/settings/invoicing</b></p>
 */
@RestController
@RequestMapping("/api/v1/settings/invoicing")
@Slf4j
@Tag(name = "Invoicing Settings", description = "API for managing invoicing settings")
public class SettingsInvoicingController {

    /**
     * Service layer dependency injected by Spring.
     * Handles all business logic and persistence for invoicing settings.
     */
    @Autowired
    private SettingsInvoicingService service;

    /**
     * Retrieve all invoicing settings.
     *
     * @return ResponseEntity containing {@link ApiResponse} with a list of {@link SettingsInvoicingDTO}
     */
    @GetMapping
    @Operation(summary = "Get all invoicing settings",
            description = "Returns a list of all invoicing settings configured in the system")
    public ResponseEntity<ApiResponse<List<SettingsInvoicingDTO>>> getAllSettings() {
        log.info("GET request: Fetch all invoicing settings");
        List<SettingsInvoicingDTO> settingsList = service.getAllSettings();
        return ResponseEntity.ok(ApiResponse.success(ApiMessageConstants.MSG_FETCH_ALL_SETTINGS, settingsList));
    }

    /**
     * Retrieve invoicing settings for a specific user.
     *
     * @param userId the unique user identifier
     * @return ResponseEntity containing {@link ApiResponse} with {@link SettingsInvoicingDTO}
     * @throws NotFoundException if no settings exist for the given userId
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get invoicing settings by user ID",
            description = "Returns the invoicing settings for the specified user ID")
    public ResponseEntity<ApiResponse<SettingsInvoicingDTO>> getSettingsByUserId(
            @Parameter(description = "Unique identifier of the user") @PathVariable Integer userId) {
        log.info("GET request: Fetch settings for userId={}", userId);
        return service.getSettingsByUserId(userId)
                .map(dto -> ResponseEntity.ok(ApiResponse.success(ApiMessageConstants.MSG_FETCH_SINGLE_SETTING, dto)))
                .orElseThrow(() -> new NotFoundException(
                        ApiMessageConstants.MSG_SETTINGS_NOT_FOUND + " for userId: " + userId));
    }

    /**
     * Create invoicing settings for a user.
     *
     * @param dto the invoicing settings to create
     * @return ResponseEntity containing {@link ApiResponse} with the created {@link SettingsInvoicingDTO}
     */
    @PostMapping
    @Operation(summary = "Create new invoicing settings",
            description = "Creates invoicing settings for a specific user")
    public ResponseEntity<ApiResponse<SettingsInvoicingDTO>> createSettings(
            @Parameter(description = "Invoicing settings data to create") @Valid @RequestBody SettingsInvoicingDTO dto) {
        log.info("POST request: Create invoicing settings for userId={}", dto.userId());
        SettingsInvoicingDTO created = service.createSettings(dto);
        return ResponseEntity.status(201)
                .body(ApiResponse.success(ApiMessageConstants.MSG_SETTINGS_CREATED, created));
    }

    /**
     * Fully update invoicing settings for a user (PUT).
     *
     * @param userId the user ID
     * @param dto    updated invoicing settings
     * @return ResponseEntity containing {@link ApiResponse} with the updated {@link SettingsInvoicingDTO}
     */
    @PutMapping("/{userId}")
    @Operation(summary = "Update invoicing settings completely",
            description = "Replaces existing invoicing settings for a user with new values")
    public ResponseEntity<ApiResponse<SettingsInvoicingDTO>> updateSettings(
            @Parameter(description = "Unique identifier of the user") @PathVariable Integer userId,
            @Parameter(description = "Updated invoicing settings data") @Valid @RequestBody SettingsInvoicingDTO dto) {
        log.info("PUT request: Update settings for userId={}", userId);
        SettingsInvoicingDTO updated = service.updateSettings(userId, dto);
        return ResponseEntity.ok(ApiResponse.success(ApiMessageConstants.MSG_SETTINGS_UPDATED, updated));
    }


    /**
     * Delete invoicing settings for a user.
     *
     * @param userId the user ID
     * @return ResponseEntity containing {@link ApiResponse} with no data (null)
     */
    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete invoicing settings by user ID",
            description = "Deletes invoicing settings for the specified user")
    public ResponseEntity<ApiResponse<Void>> deleteSettings(
            @Parameter(description = "Unique identifier of the user") @PathVariable Integer userId) {
        log.info("DELETE request: Delete settings for userId={}", userId);
        service.deleteSettings(userId);
        return ResponseEntity.ok(ApiResponse.success(ApiMessageConstants.MSG_SETTINGS_DELETED, null));
    }
}

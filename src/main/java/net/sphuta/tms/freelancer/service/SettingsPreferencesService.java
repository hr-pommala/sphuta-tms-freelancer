package net.sphuta.tms.freelancer.service;


import net.sphuta.tms.freelancer.dto.PreferencesDto;

import java.util.List;

/**
 * Service interface for managing user preferences in the system.
 * <p>
 * This interface defines the contract for all CRUD (Create, Read, Update, Delete)
 * operations related to user preferences. It ensures a clean separation of
 * business logic from persistence logic (handled at the repository level).
 * <p>
 * Typical usage includes:
 * <ul>
 *     <li>Creating default preferences for a new user.</li>
 *     <li>Fetching preferences for a specific user.</li>
 *     <li>Allowing administrators to view all preferences.</li>
 *     <li>Updating or partially updating user-specific preferences.</li>
 *     <li>Deleting preferences when a user is removed.</li>
 * </ul>
 */
public interface SettingsPreferencesService {

    /**
     * Create new preferences for a user.
     * <p>
     * This method is typically called when a new user is registered
     * to initialize their preferences.
     *
     * @param request the {@link PreferencesDto} DTO containing user preference details.
     * @return the created {@link PreferencesDto} containing persisted preference data.
     */
    PreferencesDto createPreferences(PreferencesDto request);

    /**
     * Retrieve preferences for a specific user by their unique identifier.
     *
     * @param userId unique identifier of the user.
     * @return the {@link PreferencesDto} containing the user's preferences.
     */
    PreferencesDto getPreferences(int userId);

    /**
     * Retrieve all user preferences in the system.
     * <p>
     * This method is intended for administrative usage and should be
     * protected by proper authorization rules.
     *
     * @return a {@link List} of {@link PreferencesDto} representing all users' preferences.
     */
    List<PreferencesDto> getAllPreferences();

    /**
     * Update (replace) the preferences of a user.
     * <p>
     * This method follows HTTP PUT semantics and replaces the entire
     * preferences object for the given user with the provided request data.
     *
     * @param userId  unique identifier of the user whose preferences need to be updated.
     * @param request the {@link PreferencesDto} DTO containing updated preferences.
     * @return the updated {@link PreferencesDto}.
     */
    PreferencesDto updatePreferences(int userId, PreferencesDto request);

    /**
     * Delete preferences for a specific user.
     * <p>
     * Once deleted, the user will no longer have stored preferences
     * unless new preferences are created again.
     *
     * @param userId unique identifier of the user whose preferences should be deleted.
     */
    void deletePreferences(int userId);
}

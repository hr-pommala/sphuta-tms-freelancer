package net.sphuta.tms.freelancer.repository;

import net.sphuta.tms.freelancer.entity.SettingsPreferences;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for SettingsPreferences entity.
 * Provides CRUD operations on the settings_preferences table.
 */
@Repository
public interface SettingsPreferencesRepository extends JpaRepository<SettingsPreferences, Integer> {

    /**
     * Check if a preferences record exists for a given userId.
     *
     * @param userId user identifier
     * @return true if record exists, false otherwise
     */
    boolean existsByUserId(int userId);
}

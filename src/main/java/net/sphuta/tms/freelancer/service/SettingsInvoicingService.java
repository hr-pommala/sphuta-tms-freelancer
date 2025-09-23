package net.sphuta.tms.freelancer.service;


import net.sphuta.tms.freelancer.dto.SettingsInvoicingDTO;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing invoicing settings.
 * Defines all business operations for CRUD and partial updates.
 */
public interface SettingsInvoicingService {

    /**
     * Fetch all invoicing settings.
     *
     * @return List of InvoicingSettingsDTO
     */
    List<SettingsInvoicingDTO> getAllSettings();

    /**
     * Fetch invoicing settings by user ID.
     *
     * @param userId unique identifier of the user
     * @return Optional containing InvoicingSettingsDTO if found
     */
    Optional<SettingsInvoicingDTO> getSettingsByUserId(int userId);

    /**
     * Create new invoicing settings.
     *
     * @param settingsInvoicingDTO DTO containing settings data
     * @return InvoicingSettingsDTO of the created record
     */
    SettingsInvoicingDTO createSettings(SettingsInvoicingDTO settingsInvoicingDTO);

    /**
     * Update existing invoicing settings completely (PUT).
     *
     * @param userId unique identifier of the user
     * @param settingsInvoicingDTO DTO containing updated settings
     * @return Updated InvoicingSettingsDTO
     */
    SettingsInvoicingDTO updateSettings(int userId, SettingsInvoicingDTO settingsInvoicingDTO);

    /**
     * Delete invoicing settings by user ID.
     *
     * @param userId unique identifier of the user
     */
    void deleteSettings(int userId);
}

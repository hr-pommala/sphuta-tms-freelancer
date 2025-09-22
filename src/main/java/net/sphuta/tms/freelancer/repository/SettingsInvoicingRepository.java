package net.sphuta.tms.freelancer.repository;


import net.sphuta.tms.freelancer.entity.SettingsInvoicing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for InvoicingSettings entity.
 * Provides CRUD operations and custom database queries if needed.
 */
@Repository
public interface SettingsInvoicingRepository extends JpaRepository<SettingsInvoicing, Integer> {

    /**
     * Find invoicing settings by currency code.
     *
     * @param currency the currency code to search for
     * @return Optional containing InvoicingSettings if found
     */
    Optional<SettingsInvoicing> findByCurrency(String currency);

    /**
     * Check if settings exist for a given template ID.
     *
     * @param templateId the template ID to search for
     * @return true if settings exist
     */
    boolean existsByTemplateId(String templateId);

    /**
     * Find invoicing settings by user ID.
     *
     * @param userId the user ID to search for
     * @return Optional containing InvoicingSettings if found
     */
    Optional<SettingsInvoicing> findByUserId(int userId);
}

package net.sphuta.tms.freelancer.repository;

import net.sphuta.tms.freelancer.entity.InvoiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ==========================================================
 * TmsInvoiceRepository
 * ==========================================================
 *
 * Spring Data JPA repository for {@link InvoiceEntity}.
 *
 * Responsibilities:
 * - Provides CRUD operations for invoices via {@link JpaRepository}.
 * - Exposes custom query methods based on Spring Data naming conventions.
 *
 * Typical usage:
 * - Verify if a client has invoices before deleting the client.
 * - Perform invoice lookups in service/business logic layers.
 */
public interface TmsInvoiceRepository extends JpaRepository<InvoiceEntity, Integer> {

    /**
     * Checks whether at least one {@link InvoiceEntity} exists for the given client.
     *
     * Purpose:
     * - Ensures data integrity by preventing client deletion if invoices are associated.
     *
     * @param clientId the ID of the client
     * @return true if invoices exist for this client; false otherwise
     */
    boolean existsByClientId(Integer clientId);
}

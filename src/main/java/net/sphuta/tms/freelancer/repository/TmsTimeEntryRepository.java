package net.sphuta.tms.freelancer.repository;

import net.sphuta.tms.freelancer.entity.TimeEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

/**
 * ==========================================================
 * TmsTimeEntryRepository
 * ==========================================================
 *
 * Spring Data JPA repository for {@link TimeEntryEntity}.
 *
 * Responsibilities:
 * - Provides CRUD operations for time entries via {@link JpaRepository}.
 * - Declares custom queries for business-specific use cases.
 *
 * Typical usage:
 * - Fetching uninvoiced, approved time entries for invoice generation.
 * - Checking whether a client has related time entries before deletion.
 */
public interface TmsTimeEntryRepository extends JpaRepository<TimeEntryEntity, Integer> {

    /**
     * Finds all approved and uninvoiced time entries for a client within a given date range.
     *
     * Purpose:
     * - Used to generate invoices (only APPROVED entries not yet linked to an invoice).
     *
     * @param clientId the ID of the client
     * @param from     start date (inclusive)
     * @param to       end date (inclusive)
     * @return list of matching uninvoiced time entries
     */
    @Query("""
           select t from TimeEntryEntity t
            where t.clientId = :clientId
              and t.status = 'APPROVED'
              and t.invoiceId is null
              and t.entryDate between :from and :to
           """)
    List<TimeEntryEntity> findUninvoiced(Integer clientId, LocalDate from, LocalDate to);

    /**
     * Checks whether the given client has at least one time entry.
     *
     * Purpose:
     * - Used as a safeguard before deleting a client to ensure data integrity.
     *
     * @param clientId the client ID to check
     * @return true if at least one time entry exists for the client; false otherwise
     */
    boolean existsByClientId(Integer clientId);
}

// TimeEntryRepository.java
package net.sphuta.tms.freelancer.repository;

import net.sphuta.tms.freelancer.entity.TimeEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for managing {@link TimeEntry} entities.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Provides CRUD operations via {@link JpaRepository}.</li>
 *   <li>Exposes custom query methods for business-specific requirements.</li>
 *   <li>Delegates implementation to Spring Data JPA at runtime.</li>
 * </ul>
 *
 * <p>Usage:</p>
 * - Injected in services to handle persistence of {@link TimeEntry}.
 * - Supports custom JPQL queries for invoice processing workflows.
 */
public interface TimeEntryRepository extends JpaRepository<TimeEntry, UUID> {

    /**
     * Find all approved, uninvoiced time entries for a client within a date range.
     *
     * <p>Criteria:</p>
     * <ul>
     *   <li>Status must be {@code APPROVED}.</li>
     *   <li>Invoice ID must be {@code null} (not yet linked to an invoice).</li>
     *   <li>Entry date must fall between {@code from} and {@code to} (inclusive).</li>
     * </ul>
     *
     * @param clientId unique identifier of the client
     * @param from start date of the search range
     * @param to end date of the search range
     * @return list of matching {@link TimeEntry} records
     */
    @Query("""
           select t from TimeEntry t
            where t.clientId = :clientId
              and t.status = 'APPROVED'
              and t.invoiceId is null
              and t.entryDate between :from and :to
           """)
    List<TimeEntry> findUninvoiced(UUID clientId, LocalDate from, LocalDate to);

    /**
     * Check if any time entries exist for a given client.
     *
     * @param clientId unique identifier of the client
     * @return {@code true} if at least one record exists, otherwise {@code false}
     */
    boolean existsByClientId(UUID clientId);
}

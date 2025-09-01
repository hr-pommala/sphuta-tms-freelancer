package net.sphuta.tms.freelancer.repository;

import net.sphuta.tms.freelancer.entity.ClientEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * ==========================================================
 * TmsClientRepository
 * ==========================================================
 *
 * Spring Data JPA repository for {@link ClientEntity}.
 *
 * Responsibilities:
 * - Provides CRUD operations via {@link JpaRepository}.
 * - Exposes a JPQL-based search across common client fields.
 *
 * Notes:
 * - This interface is intentionally thin; business logic (e.g., active-only filter)
 *   should live in the service layer. A default method is included to keep the
 *   current call sites stable while delegating to the core JPQL search.
 */
public interface TmsClientRepository extends JpaRepository<ClientEntity, Integer> {

    /**
     * Full-text style search across companyName, firstName, lastName, and email.
     *
     * Behavior:
     * - If {@code :q} is null or blank, returns all rows (no filter).
     * - Case-insensitive LIKE matching using {@code lower(...)}.
     *
     * @param q        free-text term (nullable/blank for no filter)
     * @param pageable paging and sorting information
     * @return a page of matching {@link ClientEntity} rows
     */
    @Query("""
    select c
    from ClientEntity c
    where (:q is null or trim(:q) = '' or
           lower(c.companyName) like concat('%', lower(:q), '%') or
           lower(c.firstName)   like concat('%', lower(:q), '%') or
           lower(c.lastName)    like concat('%', lower(:q), '%') or
           lower(c.email)       like concat('%', lower(:q), '%'))
    """)
    Page<ClientEntity> search(@Param("q") String q, Pageable pageable);

    /**
     * Compatibility overload used by callers that pass an "activeOnly" flag.
     * Currently delegates to {@link #search(String, Pageable)} without applying
     * an active filter (kept for backward compatibility with existing code paths).
     *
     * @param activeOnly ignored in current implementation (service may filter)
     * @param q          free-text term
     * @param pageable   paging and sorting info
     * @return same result as {@link #search(String, Pageable)}
     */
    default Page<ClientEntity> search(boolean activeOnly, String q, Pageable pageable) {
        // Intentionally ignoring activeOnly here; service can compose/specify active filtering.
        return search(q, pageable);
    }
}

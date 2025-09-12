package net.sphuta.tms.freelancer.repository;

import net.sphuta.tms.freelancer.entity.ClientEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * ===========================================================
 * TmsClientRepository
 * ===========================================================
 *
 * <p>Repository interface for performing persistence and search operations
 * on {@link ClientEntity} objects.</p>
 *
 * <p><b>Features:</b></p>
 * - Extends {@link JpaRepository} → inherits CRUD operations and pagination. <br>
 * - Adds custom JPQL queries for flexible client search (by active status, name, or email). <br>
 * - Provides both <i>filtered</i> and <i>unfiltered</i> search methods. <br>
 *
 * <p><b>Spring Data JPA benefits:</b></p>
 * - No need to write boilerplate DAO code. <br>
 * - Supports pagination and sorting via {@link Pageable}. <br>
 * - Query methods can be derived from method names or explicitly annotated with {@link Query}. <br>
 */
public interface TmsClientRepository extends JpaRepository<ClientEntity, Integer> {

    // ------------------------------------------------------------------------
    // CUSTOM SEARCH METHODS
    // ------------------------------------------------------------------------

    /**
     * Full-text style search across multiple client attributes.
     *
     * <p>Filters on:</p>
     * - {@code companyName} <br>
     * - {@code firstName} <br>
     * - {@code lastName} <br>
     * - {@code email} <br>
     *
     * <p>Notes:</p>
     * - Ignores filter if {@code q} is null or blank. <br>
     * - Only returns active clients. <br>
     *
     * @param q        free-text search term (nullable/blank → no filter applied)
     * @param pageable pagination and sorting configuration
     * @return a {@link Page} of matching {@link ClientEntity} objects
     */
    @Query("""
        select c
        from ClientEntity c
        where c.isActive = true
          and (
              :q is null or trim(:q) = '' or
              lower(c.companyName) like concat('%', lower(:q), '%') or
              lower(c.firstName)   like concat('%', lower(:q), '%') or
              lower(c.lastName)    like concat('%', lower(:q), '%') or
              lower(c.email)       like concat('%', lower(:q), '%')
          )
        """)
    Page<ClientEntity> search(@Param("q") String q, Pageable pageable);

    /**
     * Finds all clients that are marked as active.
     *
     * @param pageable pagination and sorting configuration
     * @return a {@link Page} of active {@link ClientEntity} objects
     */
    Page<ClientEntity> findByIsActiveTrue(Pageable pageable);

    // ------------------------------------------------------------------------
    // ALTERNATIVE SEARCH VARIANTS
    // ------------------------------------------------------------------------

    /**
     * Searches clients by an {@code active} flag and a free-text query
     * across companyName, firstName, lastName, and email.
     *
     * @param active  whether to filter only active/inactive clients
     * @param search  free-text term (case-insensitive)
     * @param pageable pagination and sorting configuration
     * @return a {@link Page} of filtered clients
     */
    @Query("SELECT c FROM ClientEntity c " +
            "WHERE c.isActive = :active " +
            "AND (LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "  OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "  OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "  OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<ClientEntity> search(@Param("active") boolean active,
                              @Param("search") String search,
                              Pageable pageable);

    /**
     * Searches clients across name/email fields without considering active status.
     *
     * @param search   free-text term (case-insensitive)
     * @param pageable pagination and sorting configuration
     * @return a {@link Page} of all matching clients
     */
    @Query("SELECT c FROM ClientEntity c " +
            "WHERE (LOWER(c.companyName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "   OR LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "   OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
            "   OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<ClientEntity> searchAll(@Param("search") String search, Pageable pageable);

    /**
     * Case-insensitive existence check for company + email (used on create).
     */
    boolean existsByCompanyNameIgnoreCaseAndEmailIgnoreCase(String companyName, String email);

    /**
     * Case-insensitive existence check for company + email excluding an id (used on update).
     */
    boolean existsByCompanyNameIgnoreCaseAndEmailIgnoreCaseAndIdNot(String companyName, String email, int id);

    /**
     * Optional lookup for a client by company + email (case-insensitive).
     */
    Optional<ClientEntity> findByCompanyNameIgnoreCaseAndEmailIgnoreCase(String companyName, String email);

    // ---------------------------
    // Per-user uniqueness checks
    // ---------------------------
    boolean existsByUserIdAndEmailIgnoreCase(int userId, String email);

    boolean existsByUserIdAndEmailIgnoreCaseAndIdNot(int userId, String email,int id);

    Optional<ClientEntity> findByUserIdAndCompanyNameIgnoreCaseAndEmailIgnoreCase(int userId, String companyName, String email);
}
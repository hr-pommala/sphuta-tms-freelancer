package net.sphuta.tms.freelancer.repository;

import net.sphuta.tms.freelancer.entity.ClientEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository interface for {@link ClientEntity} entities.
 */
public interface TmsClientRepository extends JpaRepository<ClientEntity, Integer> {

    /**
     * Full-text style search across companyName, firstName, lastName, and email.
     *
     * @param q        free-text term (nullable/blank for no filter)
     * @param pageable paging and sorting information
     * @return a page of matching clients
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
     * Finds all active clients (no search filter).
     */
    Page<ClientEntity> findByIsActiveTrue(Pageable pageable);

    /**
     * Compatibility overload that allows an activeOnly flag (ignored here).
     */
    default Page<ClientEntity> search(boolean activeOnly, String q, Pageable pageable) {
        return search(q, pageable);
    }
}

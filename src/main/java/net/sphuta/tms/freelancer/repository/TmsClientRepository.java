package net.sphuta.tms.freelancer.repository;

import net.sphuta.tms.freelancer.entity.ClientEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for {@link ClientEntity} entities.
 *
 * <p>Extends Spring Data JPA's {@link JpaRepository} to provide:</p>
 * <ul>
 *   <li>Basic CRUD operations.</li>
 *   <li>Pagination and sorting.</li>
 *   <li>Custom finder methods declared below.</li>
 * </ul>
 *
 * <p>Spring automatically implements these methods based on the
 * method naming convention.</p>
 */
public interface TmsClientRepository extends JpaRepository<ClientEntity, Integer> {

    /**
     * Finds all active clients whose name contains the given search string (case-insensitive).
     *
     * @param q        substring to search for in client names
     * @param pageable pagination and sorting information
     * @return a page of active clients filtered by name
     */
    Page<ClientEntity> findByActiveTrueAndNameContainingIgnoreCase(String q, Pageable pageable);

    /**
     * Finds all active clients (no filtering).
     *
     * @param pageable pagination and sorting information
     * @return a page of active clients
     */
    Page<ClientEntity> findByActiveTrue(Pageable pageable);
}

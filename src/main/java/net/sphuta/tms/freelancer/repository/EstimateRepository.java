// EstimateRepository.java
package net.sphuta.tms.freelancer.repository;

import net.sphuta.tms.freelancer.entity.Estimate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository interface for managing {@link Estimate} entities.
 *
 * Purpose:
 * - Extends Spring Data JPA {@link JpaRepository} to provide CRUD operations.
 * - Adds custom finder methods for domain-specific queries.
 *
 * Benefits:
 * - Automatically implemented by Spring Data JPA at runtime.
 * - No need to write boilerplate SQL or JPQL for common queries.
 */
public interface EstimateRepository extends JpaRepository<Estimate, UUID> {

    /**
     * Checks if an {@link Estimate} exists for the given client ID.
     *
     * @param clientId unique identifier of the client
     * @return true if at least one estimate exists for the client, false otherwise
     */
    boolean existsByClientId(UUID clientId);
}

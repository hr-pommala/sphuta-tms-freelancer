package net.sphuta.tms.freelancer.repository;

import net.sphuta.tms.freelancer.entity.EstimateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ==========================================================
 * TmsEstimateRepository
 * ==========================================================
 *
 * Spring Data JPA repository for {@link EstimateEntity}.
 *
 * Responsibilities:
 * - Provides standard CRUD operations via {@link JpaRepository}.
 * - Adds convenience query methods based on Spring Data naming conventions.
 *
 * Usage:
 * - Called by services to check referential constraints (e.g., before deleting a client).
 */
public interface TmsEstimateRepository extends JpaRepository<EstimateEntity, Integer> {

    /**
     * Checks whether at least one {@link EstimateEntity} exists for the given client.
     *
     * Purpose:
     * - Used before deleting a client to ensure there are no related estimates.
     *
     * @param clientId the ID of the client to check
     * @return true if one or more estimates exist for the client; false otherwise
     */
    boolean existsByClientId(Integer clientId);
}

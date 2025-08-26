package net.sphuta.tms.repository;

import net.sphuta.tms.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository interface for {@link Project} entities.
 *
 * <p>Extends Spring Data JPA's {@link JpaRepository} to provide:</p>
 * <ul>
 *   <li>Basic CRUD operations.</li>
 *   <li>Pagination and sorting support.</li>
 *   <li>Derived query methods for uniqueness checks and filtered project listings.</li>
 * </ul>
 *
 * <p>Spring automatically generates implementations for the finder methods
 * based on the method naming conventions.</p>
 */
public interface ProjectRepository extends JpaRepository<Project, UUID> {

    /**
     * Checks if a project with the given name already exists
     * for a specified client (case-insensitive).
     *
     * @param clientId the client UUID
     * @param name     the project name to check
     * @return true if a project with the same (clientId, name) exists
     */
    boolean existsByClient_IdAndNameIgnoreCase(UUID clientId, String name);

    /**
     * Checks if a project with the given code already exists
     * for a specified client (case-insensitive).
     *
     * @param clientId the client UUID
     * @param code     the project code to check
     * @return true if a project with the same (clientId, code) exists
     */
    boolean existsByClient_IdAndCodeIgnoreCase(UUID clientId, String code);

    /**
     * Finds projects filtered by active flag, client, and partial name search.
     *
     * @param active   true = active projects, false = archived
     * @param clientId the client UUID to filter on
     * @param name     substring to search for in project names (case-insensitive)
     * @param pageable pagination and sorting information
     * @return a page of projects that match the filters
     */
    Page<Project> findAllByActiveAndClient_IdAndNameContainingIgnoreCase(
            boolean active, UUID clientId, String name, Pageable pageable);

    /**
     * Finds projects filtered by active flag and partial name search,
     * without filtering by client.
     *
     * @param active   true = active projects, false = archived
     * @param name     substring to search for in project names (case-insensitive)
     * @param pageable pagination and sorting information
     * @return a page of projects that match the filters
     */
    Page<Project> findAllByActiveAndNameContainingIgnoreCase(
            boolean active, String name, Pageable pageable);
}

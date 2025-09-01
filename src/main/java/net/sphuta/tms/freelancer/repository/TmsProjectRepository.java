package net.sphuta.tms.freelancer.repository;

import net.sphuta.tms.freelancer.entity.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for {@link ProjectEntity} entities.
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
public interface TmsProjectRepository extends JpaRepository<ProjectEntity, Integer> {

    /**
     * Checks if a project with the given name already exists
     * for a specified client (case-insensitive).
     *
     * @param clientId the client ID
     * @param name     the project name to check
     * @return true if a project with the same (clientId, name) exists
     */
    boolean existsByClientEntity_IdAndNameIgnoreCase(Integer clientId, String name);

    /**
     * Checks if a project with the given code already exists
     * for a specified client (case-insensitive).
     *
     * @param clientId the client ID
     * @param code     the project code to check
     * @return true if a project with the same (clientId, code) exists
     */
    boolean existsByClientEntity_IdAndCodeIgnoreCase(Integer clientId, String code);

    /**
     * Finds projects filtered by active flag only.
     *
     * @param active   true = active projects, false = archived
     * @param pageable pagination and sorting information
     * @return a page of projects
     */
    Page<ProjectEntity> findByActive(boolean active, Pageable pageable);

    /**
     * Finds projects filtered by active flag and client.
     *
     * @param active   true = active projects, false = archived
     * @param clientId the client ID to filter on
     * @param pageable pagination and sorting information
     * @return a page of projects
     */
    Page<ProjectEntity> findByActiveAndClientEntity_Id(
            boolean active, Integer clientId, Pageable pageable);

    /**
     * Finds projects filtered by active flag, client, and search on name/code/description.
     *
     * @param active      true = active projects, false = archived
     * @param clientId    the client ID to filter on
     * @param name        substring to search for in project names (case-insensitive)
     * @param code        substring to search for in project codes (case-insensitive)
     * @param description substring to search for in project descriptions (case-insensitive)
     * @param pageable    pagination and sorting information
     * @return a page of projects that match the filters
     */
    Page<ProjectEntity> findByActiveAndClientEntity_IdAndNameContainingIgnoreCaseOrCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            boolean active, Integer clientId, String name, String code, String description, Pageable pageable);

    /**
     * Finds projects filtered by active flag and search on name/code/description (no client filter).
     *
     * @param active      true = active projects, false = archived
     * @param name        substring to search for in project names (case-insensitive)
     * @param code        substring to search for in project codes (case-insensitive)
     * @param description substring to search for in project descriptions (case-insensitive)
     * @param pageable    pagination and sorting information
     * @return a page of projects that match the filters
     */
    Page<ProjectEntity> findByActiveAndNameContainingIgnoreCaseOrCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            boolean active, String name, String code, String description, Pageable pageable);

    /**
     * Finds projects filtered by active flag, client, and partial name search.
     *
     * @param active   true = active projects, false = archived
     * @param clientId the client ID to filter on
     * @param name     substring to search for in project names (case-insensitive)
     * @param pageable pagination and sorting information
     * @return a page of projects that match the filters
     */
    Page<ProjectEntity> findAllByActiveAndClientEntity_IdAndNameContainingIgnoreCase(
            boolean active, Integer clientId, String name, Pageable pageable);

    /**
     * Finds projects filtered by active flag and partial name search,
     * without filtering by client.
     *
     * @param active   true = active projects, false = archived
     * @param name     substring to search for in project names (case-insensitive)
     * @param pageable pagination and sorting information
     * @return a page of projects that match the filters
     */
    Page<ProjectEntity> findAllByActiveAndNameContainingIgnoreCase(
            boolean active, String name, Pageable pageable);
}

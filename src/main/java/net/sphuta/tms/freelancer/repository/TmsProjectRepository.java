package net.sphuta.tms.freelancer.repository;

import net.sphuta.tms.freelancer.entity.ProjectEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository interface for {@link ProjectEntity} entities.
 *
 * Provides CRUD, pagination, and search operations.
 */
public interface TmsProjectRepository extends JpaRepository<ProjectEntity, Integer> {

    /**
     * Checks if a project with the given name already exists for a client.
     */
    boolean existsByClientEntity_IdAndNameIgnoreCase(Integer clientId, String name);

    /**
     * Checks if a project with the given code already exists for a client.
     */
    boolean existsByClientEntity_IdAndCodeIgnoreCase(Integer clientId, String code);

    /**
     * Finds all projects by active flag.
     */
    Page<ProjectEntity> findByActive(boolean active, Pageable pageable);

    /**
     * Finds all projects by active flag and client.
     */
    Page<ProjectEntity> findByActiveAndClientEntity_Id(boolean active, Integer clientId, Pageable pageable);

    /**
     * Finds projects by active flag, client, and a search term across name, code, and description.
     */
    @Query("""
           SELECT p FROM ProjectEntity p
           WHERE p.active = :active
             AND p.clientEntity.id = :clientId
             AND (
                   LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))
                 )
           """)
    Page<ProjectEntity> searchByActiveAndClientAndTerm(
            @Param("active") boolean active,
            @Param("clientId") Integer clientId,
            @Param("search") String search,
            Pageable pageable);

    /**
     * Finds projects by active flag and a search term across name, code, and description.
     */
    @Query("""
           SELECT p FROM ProjectEntity p
           WHERE p.active = :active
             AND (
                   LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(p.code) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))
                 )
           """)
    Page<ProjectEntity> searchByActiveAndTerm(
            @Param("active") boolean active,
            @Param("search") String search,
            Pageable pageable);

    /**
     * Finds projects by active flag, client, and partial name match.
     */
    Page<ProjectEntity> findByActiveAndClientEntity_IdAndNameContainingIgnoreCase(
            boolean active, Integer clientId, String name, Pageable pageable);

    /**
     * Finds projects by active flag and partial name match (no client filter).
     */
    Page<ProjectEntity> findByActiveAndNameContainingIgnoreCase(
            boolean active, String name, Pageable pageable);
}

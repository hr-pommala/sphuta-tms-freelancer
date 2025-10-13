package net.sphuta.tms.freelancer.service;

import net.sphuta.tms.freelancer.dto.TmsClientDto;
import net.sphuta.tms.freelancer.dto.TmsProjectDto;
import org.springframework.data.domain.Page;

/**
 * Service interface defining business operations for Clients and Projects
 * in the Sphuta TMS (Freelancer edition).
 *
 * <p>This interface is implemented by the service layer to encapsulate
 * business logic and hide persistence details from controllers.</p>
 *
 * <p>Responsibilities include:</p>
 * <ul>
 *   <li>Fetching clients for dropdowns.</li>
 *   <li>CRUD operations for projects.</li>
 *   <li>Filtering and listing projects (active, archived, search-based).</li>
 *   <li>Managing project lifecycle (archive/unarchive).</li>
 * </ul>
 */
public interface TmsProjectService {

    /**
     * Retrieves a paginated list of clients for use in the Owner dropdown.
     *
     *  - search: optional name filter (case-insensitive)
     *  - page:   0-based page index
     *  - size:   number of items per page
     * @return page of clients mapped to {@link TmsClientDto}
     */
    Page<TmsClientDto> listClients(TmsClientDto filters);

    /**
     * Creates a new project.
     *
     * @param in DTO containing the create payload
     * @return created project mapped to {@link TmsProjectDto}
     */
    TmsProjectDto createProject(TmsProjectDto in);

    /**
     * Updates an existing project (PUT semantics — full replace).
     *
     * @param id project ID (int)
     * @param in DTO containing update payload (all fields expected for a full replace)
     * @return updated project mapped to {@link TmsProjectDto}
     */
    TmsProjectDto updateProject(int id, TmsProjectDto in);

    /**
     * Archives or unarchives a project.
     *
     * @param id        project ID (int)
     * @param unarchive if true, unarchive (set active=true); if false, archive (set active=false)
     * @return updated project mapped to {@link TmsProjectDto}
     */
    TmsProjectDto archiveProject(int id, boolean unarchive);

    /**
     * Deletes a project (hard delete).
     *
     * @param id project ID (int)
     */
    void deleteProject(int id);

    /**
     * Lists projects with optional filters.
     @param filters filter DTO containing:
      *                <ul>
      *                  <li>{@link TmsProjectDto active} – true for active projects, false for archived</li>
      *                  <li>{@link TmsProjectDto search} – optional case-insensitive substring search on project name</li>
      *                  <li>{@link TmsProjectDto page page} – 0-based page index</li>
      *                  <li>{@link TmsProjectDto size size} – number of items per page</li>
      *                </ul>
      * @return page of projects mapped to {@link TmsProjectDto}
     */
    Page<TmsProjectDto> listProjects(TmsProjectDto filters);

}

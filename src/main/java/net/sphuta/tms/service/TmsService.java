package net.sphuta.tms.service;

import net.sphuta.tms.dto.*;
import org.springframework.data.domain.Page;

import java.util.UUID;

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
public interface TmsService {

    /**
     * Retrieves a paginated list of clients for use in the Owner dropdown.
     *
     * @param search optional name filter (case-insensitive)
     * @param page   0-based page index
     * @param size   number of items per page
     * @return page of clients mapped to {@link ClientDto}
     */
    Page<ClientDto> listClients(String search, int page, int size);

    /**
     * Creates a new project.
     *
     * @param in DTO containing the create payload
     * @return created project mapped to {@link ProjectDtos.View}
     */
    ProjectDtos.View createProject(ProjectDtos.Create in);

    /**
     * Updates an existing project.
     *
     * @param id          project UUID
     * @param in          DTO containing update payload
     * @param fullReplace true for PUT (full replacement), false for PATCH (partial update)
     * @return updated project mapped to {@link ProjectDtos.View}
     */
    ProjectDtos.View updateProject(UUID id, ProjectDtos.Update in, boolean fullReplace);

    /**
     * Archives or unarchives a project.
     *
     * @param id        project UUID
     * @param unarchive if true, unarchive (set active=true); if false, archive (set active=false)
     * @return updated project mapped to {@link ProjectDtos.View}
     */
    ProjectDtos.View archiveProject(UUID id, boolean unarchive);

    /**
     * Deletes a project (hard delete).
     *
     * @param id project UUID
     */
    void deleteProject(UUID id);

    /**
     * Lists all active projects (active=true).
     *
     * @param page 0-based page index
     * @param size number of items per page
     * @return page of active projects
     */
    Page<ProjectDtos.View> listActiveProjects(int page, int size);

    /**
     * Lists all archived projects (active=false).
     *
     * @param page 0-based page index
     * @param size number of items per page
     * @return page of archived projects
     */
    Page<ProjectDtos.View> listArchivedProjects(int page, int size);

    /**
     * Lists active projects filtered by owner (clientId) and search term.
     *
     * @param clientId client UUID (string form)
     * @param search   substring to search for in project names
     * @param page     0-based page index
     * @param size     number of items per page
     * @return page of filtered active projects
     */
    Page<ProjectDtos.View> listActiveProjectsByOwnerAndSearch(String clientId, String search, int page, int size);

}

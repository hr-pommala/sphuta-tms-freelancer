package net.sphuta.tms.freelancer.constants;

/**
 * ==========================================================
 * # ApiMessageConstants
 * ==========================================================
 *
 * Centralized constants for **user-facing API messages** used
 * throughout controllers and services in the Freelancer module.
 *
 * <p>Key Goals:</p>
 * <ul>
 *   <li>Ensure **consistency** across all API endpoints</li>
 *   <li>Enable easier **future localization / i18n**</li>
 *   <li>Keep controllers clean by avoiding inline string literals</li>
 *   <li>Provide a single source of truth for message values</li>
 * </ul>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>
 * return TmsApiResponse.success(HttpStatus.OK,
 *         ApiMessageConstants.PROJECT_CREATED, createdProject);
 * </pre>
 *
 * <p>This approach allows changing messages in one place
 * without touching business logic or controllers.</p>
 *
 * <hr>
 * <b>Design Notes:</b>
 * <ul>
 *   <li>Declared as {@code final} to prevent inheritance.</li>
 *   <li>Private constructor to prevent instantiation.</li>
 *   <li>All fields are {@code public static final} constants.</li>
 * </ul>
 */
public final class ApiMessageConstants {

    /**
     * Private constructor to prevent instantiation of this utility class.
     * <p>Since this class only holds constants, it should never be created.</p>
     */
    private ApiMessageConstants() {
        // utility class
    }

    /* ==========================================================
     * Clients (Owner Dropdown)
     * ========================================================== */

    /** Success message when active clients are fetched for dropdowns. */
    public static final String CLIENTS_FETCHED = "Clients fetched successfully";


    /* ==========================================================
     * Projects — Read/List Operations
     * ========================================================== */

    /** Message returned when active projects are listed. */
    public static final String PROJECTS_ACTIVE_FETCHED = "Active projects fetched";

    /** Message returned when archived projects are listed. */
    public static final String PROJECTS_ARCHIVED_FETCHED = "Archived projects fetched";

    /** General message for successful project listing with filters. */
    public static final String PROJECTS_FETCHED_SUCCESS = "Projects fetched successfully";


    /* ==========================================================
     * Projects — Mutations (Create/Update/Delete/Archive)
     * ========================================================== */

    /** Message used when a new project is successfully created. */
    public static final String PROJECT_CREATED = "Project created successfully";

    /** Message used when a project is updated. */
    public static final String PROJECT_UPDATED = "Project updated successfully";

    /** Message used when a project is archived (active → false). */
    public static final String PROJECT_ARCHIVED = "Project archived successfully";

    /** Message used when a project is unarchived (active → true). */
    public static final String PROJECT_UNARCHIVED = "Project unarchived successfully";

    /** Message used when a project is permanently deleted. */
    public static final String PROJECT_DELETED = "Project deleted successfully";
}

package net.sphuta.tms.freelancer.constants;

/**
 * TmsMessages
 * ------------------------------------------------------
 * This utility class holds all the constant application-wide
 * success messages used across the TMS Freelancer module.
 *
 * <p>Purpose:
 * - Centralize message definitions to avoid duplication
 * - Ensure consistent wording across controllers and services
 * - Improve maintainability by keeping all messages in one place
 *
 * <p>Usage:
 * These constants are typically used in API responses to
 * communicate success messages back to the client.
 *
 * <p>Note:
 * The class is declared final and has a private constructor
 * to prevent instantiation and inheritance.
 */
public final class TmsMessages {

    private TmsMessages() {}


    // ===================== ClientController =====================
    public static final String CLIENT_BASE_PATH = "/api/v1/clients";

    /** Message displayed when a single timesheet is successfully fetched */
    public static final String TIMESHEET_FETCHED = "Timesheet fetched successfully";
    // Success messages
    public static final String MSG_CLIENTS_FETCHED = "Clients fetched";
    public static final String MSG_CLIENT_FETCHED = "Client fetched";
    public static final String MSG_CLIENT_CREATED = "Client created";
    public static final String MSG_CLIENT_REPLACED = "Client replaced";
    public static final String MSG_CLIENT_DELETED = "Client deleted";
    public static final String MSG_CLIENT_ARCHIVED = "Client archived";
    public static final String MSG_CLIENT_UNARCHIVED = "Client unarchived";

    //--------------------- Timesheets and TimeEntries ------------------
    public static final String TIMESHEET_SUBMITTED = "Timesheet submitted successfully";
    public static final String TIMESHEET_DELETED = "Timesheet deleted successfully";
    public static final String ENTRIES_FETCHED = "Entries fetched successfully";
    public static final String TIME_ENTRY_DELETED = "Time entry deleted successfully";
    public static final String TIME_ENTRY_CREATED = "Time entry created successfully";
    public static final String TIMESHEETS_FETCHED = "Timesheets fetched successfully";
    public static final String TIMESHEET_CREATED = "Timesheet created successfully";
    public static final String BULK_UPSERT_COMPLETED = "Bulk upsert completed";

    public static final String TIMESHEET_NOT_FOUND    = "Timesheet not found";
    public static final String TIMESHEET_LOCKED       = "Timesheet is LOCKED and cannot be modified";
    public static final String HOURS_INVALID          = "hours must be > 0";
    public static final String TIME_ENTRY_NOT_FOUND   = "Time entry not found";
    public static final String TIME_ENTRY_INVOICED    = "Time entry already invoiced; cannot delete";

    public static final String TIMESHEET_CONFLICT     = "Timesheet for project & period already exists";
;    public static final String USER_NOT_FOUND         = "User not found";


    // ---------------- Task messages ----------------

    public static final String ENTITIES_CREATED = "Task created successfully";
    public static final String ENTITIES_UPDATED = "Task updated successfully";
    public static final String ENTITY_FETCHED   = "Task fetched successfully";
    public static final String ENTITIES_FETCHED  = "Tasks fetched successfully";
    public static final String ENTITY_DELETED   = "Task deleted successfully";
    public static final String TASK_NOT_FOUND = "Task not found: ";
    public static final String PROJECT_NOT_FOUND = "Project not found: ";

    //------------------ Project Messages ---------------------

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

     /* =================================================
       Default configuration values of Settings invoicing
       ==================================================== */

    /** Default currency code used for invoicing (ISO 4217 format). */
    public static final String DEFAULT_CURRENCY = "USD";

    /** Default format string for invoice numbering (year + sequence). */
    public static final String DEFAULT_INVOICE_FORMAT = "INV-${yyyy}${seq:5}";

    /** Default number of days allowed for payment terms. */
    public static final int DEFAULT_PAYMENT_TERMS = 14;

    /** Default template identifier for invoice rendering. */
    public static final String DEFAULT_TEMPLATE_ID = "tmpl_default";


    /* ===============================================
       API response messages of Settings invoicing
       =============================================*/

    /** Message returned when invoicing settings are not found. */
    public static final String MSG_SETTINGS_NOT_FOUND = "Invoicing settings not found";

    /** Message returned when invoicing settings are successfully created. */
    public static final String MSG_SETTINGS_CREATED = "Settings created successfully";

    /** Message returned when invoicing settings are successfully updated. */
    public static final String MSG_SETTINGS_UPDATED = "Settings updated successfully";

    /** Message returned when invoicing settings are successfully deleted. */
    public static final String MSG_SETTINGS_DELETED = "Settings deleted successfully";

    /** Message returned when all invoicing settings are fetched. */
    public static final String MSG_FETCH_ALL_SETTINGS = "Fetched all settings";

    /** Message returned when a single invoicing setting is fetched successfully. */
    public static final String MSG_FETCH_SINGLE_SETTING = "Fetched settings successfully";

    public static final String SETTINGS_NOT_FOUND = "Settings not found for userId: ";



    // -------------------- PREFERENCES MESSAGES --------------------

    public static final String PREFERENCES_FETCH_ALL_SUCCESS = "All preferences fetched successfully";
    public static final String PREFERENCES_FETCH_SUCCESS = "Preferences fetched successfully";
    public static final String PREFERENCES_CREATE_SUCCESS = "Preferences created successfully";
    public static final String PREFERENCES_UPDATE_SUCCESS = "Preferences updated successfully";
    public static final String PREFERENCES_DELETE_SUCCESS = "Preferences deleted successfully";
    public static final String PREFERENCES_NOT_FOUND = "Preferences not found for the given user";


    //----------------------- Notification Messages --------------------

    public static final String NOTIFICATION_FETCH_SUCCESS = "Notifications fetched successfully";
    public static final String NOTIFICATION_CREATE_SUCCESS = "Notification created successfully";
    public static final String NOTIFICATION_MARK_READ_SUCCESS = "Notification marked as read";
    public static final String NOTIFICATION_MARK_ALL_READ_SUCCESS = "All notifications marked as read";

    //------------------------- Auth Messages ----------------------------

    public static final String USER_REGISTERED_SUCCESS = "User registered successfully";
    public static final String LOGIN_SUCCESS = "Login successful";
    public static final String PASSWORD_UPDATED = "Password updated";
    public static final String LOGGED_OUT = "Logged out";
    public static final String PASSWORD_MISMATCH     = "Passwords do not match";
    public static final String EMAIL_ALREADY_EXISTS = "Email already registered";
    public static final String EMAIL_NOT_FOUND      = "Email doesn't exist";
    public static final String INVALID_CREDENTIALS = "Invalid credentials";
    public static final String RESET_LINK_SENT      = "Reset link shared to registered email if exists";

    //------------------------ User Messages ----------------------------

    public static final String USER_NOT_FOUND_BY_ID = "User not existed with this id: %d";
    public static final String USER_CREATED_SUCCESS = "User created successfully";
    public static final String USER_RETRIEVED_SUCCESS = "User retrieved successfully";
    public static final String USER_RETRIEVED_ALL_SUCCESS = "All users retrieved successfully";
    public static final String USER_UPDATED_SUCCESS = "User updated successfully";
    public static final String USER_DELETED_SUCCESS = "User deleted successfully";

}

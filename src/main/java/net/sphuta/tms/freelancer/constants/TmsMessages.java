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


    // ===================== TimeEntryController =====================
    public static final String TIME_ENTRY_BASE_PATH = "/api/v1/time-entries";
    public static final String TIME_ENTRY_UNINVOICED_PATH = "/uninvoiced";
    /**
     * Private constructor to prevent instantiation.
     * This ensures that the class is only used as a static utility.
     */
    private TmsMessages() {}

    public static final String LOG_TIME_ENTRY_FETCH_REQUEST =
            "HTTP GET /time-entries/uninvoiced clientId={}, from={}, to={}";
    public static final String LOG_TIME_ENTRY_SUSPICIOUS_RANGE =
            "Suspicious date range: from {} > to {} for client {}";
    public static final String LOG_TIME_ENTRY_FETCH_SUCCESS_WITH_COUNT =
            "Uninvoiced entries fetched for client {} ({} ms, count={})";
    public static final String LOG_TIME_ENTRY_FETCH_SUCCESS_NO_COUNT =
            "Uninvoiced entries fetched for client {} ({} ms)";
    /** Message displayed when all timesheets are successfully fetched */
    public static final String TIMESHEETS_FETCHED = "Timesheets fetched successfully";

    /** Message displayed when a timesheet is successfully created */
    public static final String TIMESHEET_CREATED = "Timesheet created successfully";
    // ===================== ClientController =====================
    public static final String CLIENT_BASE_PATH = "/api/v1/clients";

    /** Message displayed when a single timesheet is successfully fetched */
    public static final String TIMESHEET_FETCHED = "Timesheet fetched successfully";
    // Success messages
    public static final String MSG_CLIENTS_FETCHED = "Clients fetched";
    public static final String MSG_CLIENT_FETCHED = "Client fetched";
    public static final String MSG_CLIENT_CREATED = "Client created";
    public static final String MSG_CLIENT_REPLACED = "Client replaced";
    public static final String MSG_CLIENT_PATCHED = "Client patched";
    public static final String MSG_CLIENT_DELETED = "Client deleted";
    public static final String MSG_CLIENT_ARCHIVED = "Client archived";
    public static final String MSG_CLIENT_UNARCHIVED = "Client unarchived";

    // Error messages
    public static final String ERR_CLIENT_NOT_FOUND = "Client not found";
    /** Message displayed after bulk insert or update operation is completed */
    public static final String BULK_UPSERT_COMPLETED = "Bulk upsert completed";

    // Log templates
    public static final String LOG_CLIENT_LIST_REQUEST =
            "HTTP GET /clients active={}, search='{}', page={}, size={}";
    public static final String LOG_CLIENT_GET_BY_ID = "HTTP GET /clients/{} (fetch by id)";
    public static final String LOG_CLIENT_CREATE_REQUEST =
            "HTTP POST /clients email={}, companyName={}";
    public static final String LOG_CLIENT_UPDATE_REQUEST = "HTTP PUT /clients/{} (replace)";
    public static final String LOG_CLIENT_PATCH_REQUEST = "HTTP PATCH /clients/{} (partial update)";
    public static final String LOG_CLIENT_DELETE_REQUEST = "HTTP DELETE /clients/{}";
    public static final String LOG_CLIENT_ARCHIVE_REQUEST = "HTTP POST /clients/{}/archive";
    public static final String LOG_CLIENT_UNARCHIVE_REQUEST = "HTTP POST /clients/{}/unarchive";
    public static final String LOG_CLIENT_EXPORT_REQUEST =
            "HTTP GET /clients/export active='{}', search='{}'";
    /** Message displayed when a timesheet is successfully submitted */
    public static final String TIMESHEET_SUBMITTED = "Timesheet submitted successfully";

    /** Message displayed when a timesheet is successfully locked */
    public static final String TIMESHEET_LOCKED = "Timesheet locked successfully";

    /** Message displayed when a timesheet is successfully deleted */
    public static final String TIMESHEET_DELETED = "Timesheet deleted successfully";

    /** Message displayed when entries for a timesheet are successfully fetched */
    public static final String ENTRIES_FETCHED = "Entries fetched successfully";

    // ===================== EstimateController =====================
    public static final String ESTIMATE_BASE_PATH = "/api/v1/estimates";

    /** Message displayed when a time entry is successfully created */
    public static final String TIME_ENTRY_CREATED = "Time entry created successfully";
    public static final String LOG_ESTIMATE_CREATE_REQUEST = "HTTP POST /estimates for client {}";
    public static final String LOG_ESTIMATE_NO_ITEMS_WARN = "Estimate create request has no items for client {}";
    public static final String LOG_ESTIMATE_ITEMS_TRACE = "Estimate create request contains {} item(s)";
    public static final String LOG_ESTIMATE_CREATED_SUCCESS =
            "Estimate {} created for client {} ({} ms)";

    /** Message displayed when a time entry is successfully deleted */
    public static final String TIME_ENTRY_DELETED = "Time entry deleted successfully";
    // ===================== InvoiceController =====================
    public static final String INVOICE_BASE_PATH = "/api/v1/invoices";
    public static final String INVOICE_SEND_PATH = "/{invoiceId}/send";

    public static final String LOG_INVOICE_CREATE_REQUEST = "HTTP POST /invoices for client {}";
    public static final String LOG_INVOICE_NO_TIME_ENTRIES_WARN =
            "Invoice create request received with no timeEntryIds for client {}";
    public static final String LOG_INVOICE_TIME_ENTRIES_TRACE =
            "Invoice create request contains {} timeEntryIds";
    public static final String LOG_INVOICE_CREATED_SUCCESS =
            "Invoice {} created for client {} ({} ms)";

    public static final String LOG_INVOICE_SEND_REQUEST =
            "HTTP POST /invoices/{}/send";
    public static final String LOG_INVOICE_SENT_SUCCESS =
            "Invoice {} sent (status={}, {} ms)";

    // ---------------- Task messages ----------------
    /** Base API path for task-related endpoints */

    /** Message displayed when a task is successfully created */
    public static final String ENTITIES_CREATED = "Task created successfully";

    /** Message displayed when a task is successfully updated */
    public static final String ENTITIES_UPDATED = "Task updated successfully";

    /** Message displayed when a task is successfully fetched */
    public static final String ENTITY_FETCHED   = "Task fetched successfully";

    /** Message displayed when all tasks are successfully fetched */
    public static final String ENTITIES_FETCHED  = "Tasks fetched successfully";

    /** Message displayed when a task is successfully deleted */
    public static final String ENTITY_DELETED   = "Task deleted successfully";
}

package net.sphuta.tms.freelancer.constants;

/**
 * Centralized constants for messages, log templates, and paths.
 * Instead of spreading across multiple constants files,
 * everything is kept in one place for easy maintenance.
 */
public final class TmsMessages {

    private TmsMessages() {
        // Prevent instantiation
    }

    // ===================== TimeEntryController =====================
    public static final String TIME_ENTRY_BASE_PATH = "/api/v1/time-entries";
    public static final String TIME_ENTRY_UNINVOICED_PATH = "/uninvoiced";

    public static final String LOG_TIME_ENTRY_FETCH_REQUEST =
            "HTTP GET /time-entries/uninvoiced clientId={}, from={}, to={}";
    public static final String LOG_TIME_ENTRY_SUSPICIOUS_RANGE =
            "Suspicious date range: from {} > to {} for client {}";
    public static final String LOG_TIME_ENTRY_FETCH_SUCCESS_WITH_COUNT =
            "Uninvoiced entries fetched for client {} ({} ms, count={})";
    public static final String LOG_TIME_ENTRY_FETCH_SUCCESS_NO_COUNT =
            "Uninvoiced entries fetched for client {} ({} ms)";

    // ===================== ClientController =====================
    public static final String CLIENT_BASE_PATH = "/api/v1/clients";

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

    // ===================== EstimateController =====================
    public static final String ESTIMATE_BASE_PATH = "/api/v1/estimates";

    public static final String LOG_ESTIMATE_CREATE_REQUEST = "HTTP POST /estimates for client {}";
    public static final String LOG_ESTIMATE_NO_ITEMS_WARN = "Estimate create request has no items for client {}";
    public static final String LOG_ESTIMATE_ITEMS_TRACE = "Estimate create request contains {} item(s)";
    public static final String LOG_ESTIMATE_CREATED_SUCCESS =
            "Estimate {} created for client {} ({} ms)";

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
}

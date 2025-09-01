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

    /**
     * Private constructor to prevent instantiation.
     * This ensures that the class is only used as a static utility.
     */
    private TmsMessages() {}

    /** Message displayed when all timesheets are successfully fetched */
    public static final String TIMESHEETS_FETCHED = "Timesheets fetched successfully";

    /** Message displayed when a timesheet is successfully created */
    public static final String TIMESHEET_CREATED = "Timesheet created successfully";

    /** Message displayed when a single timesheet is successfully fetched */
    public static final String TIMESHEET_FETCHED = "Timesheet fetched successfully";

    /** Message displayed after bulk insert or update operation is completed */
    public static final String BULK_UPSERT_COMPLETED = "Bulk upsert completed";

    /** Message displayed when a timesheet is successfully submitted */
    public static final String TIMESHEET_SUBMITTED = "Timesheet submitted successfully";

    /** Message displayed when a timesheet is successfully locked */
    public static final String TIMESHEET_LOCKED = "Timesheet locked successfully";

    /** Message displayed when a time entry is successfully created */
    public static final String TIME_ENTRY_CREATED = "Time entry created successfully";

    /** Message displayed when a time entry is successfully deleted */
    public static final String TIME_ENTRY_DELETED = "Time entry deleted successfully";
}

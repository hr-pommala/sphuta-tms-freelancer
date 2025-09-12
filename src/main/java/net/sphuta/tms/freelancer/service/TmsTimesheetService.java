package net.sphuta.tms.freelancer.service;

import net.sphuta.tms.freelancer.dto.*;

import java.util.List;

/**
 * Service interface for managing Timesheets in the Freelancer Timesheet Management System.
 */
public interface TmsTimesheetService {

    /**
     * Creates a new timesheet.
     *
     * @param req {@link TmsTimesheetDto} containing details for the new timesheet
     * @return {@link TmsTimesheetDto} containing details of the created timesheet
     */
    TmsTimesheetDto create(TmsTimesheetDto req);

    /**
     * Retrieves details of a specific timesheet by ID.
     *
     * @param id unique identifier of the timesheet
     * @return {@link TmsTimesheetDto} containing the timesheet details
     */
    TmsTimesheetDto get(int id);

    /**
     * Submits a timesheet for approval.
     *
     * @param id unique identifier of the timesheet
     * @return {@link TmsTimesheetDto} with updated status after submission
     */
    TmsTimesheetDto submit(int id);

    /**
     * Bulk upsert operation for time entries of a timesheet.
     *
     * @param timesheetId unique identifier of the timesheet
     * @param req {@link BulkUpsertDto} containing multiple entries to insert or update
     * @return {@link BulkUpsertDto} with the results of the bulk operation
     */
    BulkUpsertDto bulkUpsert(int timesheetId, BulkUpsertDto req);

    /**
     * Retrieve all timesheets (non-paged).
     *
     * @return list of {@link TmsTimesheetDto}
     */
    List<TmsTimesheetDto> getAll();

    /**
     * Delete a timesheet by id.
     *
     * Business rules:
     * - If the timesheet does not exist -> NotFoundException
     * - Deletes associated time entries first to avoid FK constraints, then deletes the timesheet
     *
     * @param id unique identifier of the timesheet
     */
    void delete(int id);

}

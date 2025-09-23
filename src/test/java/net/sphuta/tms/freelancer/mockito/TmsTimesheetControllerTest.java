package net.sphuta.tms.freelancer.mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.sphuta.tms.freelancer.controller.TmsTimesheetController;
import net.sphuta.tms.freelancer.dto.BulkUpsertDto;
import net.sphuta.tms.freelancer.dto.TimeEntryDto;
import net.sphuta.tms.freelancer.dto.TmsTimesheetDto;
import net.sphuta.tms.freelancer.enums.TimesheetStatus;
import net.sphuta.tms.freelancer.service.TmsTimeEntryService;
import net.sphuta.tms.freelancer.service.TmsTimesheetService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for TmsTimesheetController using MockMvc and Mockito.
 * Mocks service layer to isolate controller behavior.
 * Covers key endpoints for creating, retrieving, updating timesheets and time entries.
 * Validates HTTP status codes and JSON response structure.
 * Uses JUnit 5 and Spring Boot Test framework.
 * Focuses on typical success scenarios.
 * Error and edge cases should be tested separately.
 */
@WebMvcTest(controllers = TmsTimesheetController.class)
@AutoConfigureMockMvc(addFilters = false)  // ✅ disables Spring Security filters in MockMvc
class TmsTimesheetControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @MockBean
    private TmsTimesheetService timesheetService;

    @MockBean
    private TmsTimeEntryService timeEntryService;

    // ------------------- TESTS -------------------//

    /**
     * Test creating a new timesheet via POST /api/v1/timesheets
     * Expects 201 Created with correct response body.
     */
    @Test
    @DisplayName("POST /api/v1/timesheets → 201 Created")
    void create_timesheet_created() throws Exception {
        var req = new TmsTimesheetDto(
                2001,
                LocalDate.parse("2025-09-01"),
                LocalDate.parse("2025-09-15"),
                0,
                null,
                TimesheetStatus.DRAFT,
                List.of(),
                List.of(),
                BigDecimal.ZERO
        );

        var resp = new TmsTimesheetDto(
                req.projectId(),
                req.periodStart(),
                req.periodEnd(),
                101,
                "Project-2001",
                TimesheetStatus.DRAFT,
                List.of(),
                List.of(),
                BigDecimal.ZERO
        );

        Mockito.when(timesheetService.create(any(TmsTimesheetDto.class))).thenReturn(resp);

        mvc.perform(post("/api/v1/timesheets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.timesheetId").value(101)); // << changed here
    }

    /**
     * Test retrieving a timesheet by ID via GET /api/v1/timesheets/{id}
     * Expects 200 OK with correct response body.
     */
    @Test
    @DisplayName("GET /api/v1/timesheets/{id} → 200 OK")
    void get_timesheet_ok() throws Exception {
        var id = 101;
        var resp = new TmsTimesheetDto(
                2001,
                LocalDate.parse("2025-09-01"),
                LocalDate.parse("2025-09-15"),
                id,
                "Project-2001",
                TimesheetStatus.DRAFT,
                List.of(),
                List.of(),
                BigDecimal.ZERO
        );

        Mockito.when(timesheetService.get(eq(id))).thenReturn(resp);

        mvc.perform(get("/api/v1/timesheets/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.timesheetId").value(id)); // << changed here
    }

    /**
     * Test bulk upserting time entries via PUT /api/v1/timesheets/{id}/entries
     * Expects 200 OK with correct response body.
     */
    @Test
    @DisplayName("PUT /api/v1/timesheets/{id}/entries → 200 OK")
    void bulk_upsert_ok() throws Exception {
        var id = 101;
        var req = new BulkUpsertDto(
                List.of(
                        new TimeEntryDto(
                                id,
                                LocalDate.parse("2025-09-05"),
                                "Backend Refactor",
                                new BigDecimal("5"),
                                new BigDecimal("70"),
                                null,      // taskId
                                null,      // taskName
                                null,      // id
                                null       // costAtEntry
                        )
                ),
                "UPSERT",
                null, null, null, null
        );

        var resp = new BulkUpsertDto(
                req.entries(),
                req.mode(),
                1,
                0,
                0,
                new BigDecimal("5")
        );

        Mockito.when(timesheetService.bulkUpsert(eq(id), any(BulkUpsertDto.class))).thenReturn(resp);

        mvc.perform(put("/api/v1/timesheets/{id}/entries", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.inserted").value(1))
                .andExpect(jsonPath("$.data.totalHours").value(5));
    }

    /**
     * Test submitting a timesheet via POST /api/v1/timesheets/{id}/submit
     * Expects 200 OK with updated status in response body.
     */
    @Test
    @DisplayName("POST /api/v1/timesheets/{id}/submit → 200 OK")
    void submit_ok() throws Exception {
        var id = 101;
        var resp = new TmsTimesheetDto(
                2001,
                LocalDate.parse("2025-09-01"),
                LocalDate.parse("2025-09-15"),
                id,
                "Project-2001",
                TimesheetStatus.APPROVED,
                List.of(),
                List.of(),
                BigDecimal.ZERO
        );

        Mockito.when(timesheetService.submit(eq(id))).thenReturn(resp);

        mvc.perform(post("/api/v1/timesheets/{id}/submit", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    /**
     * Test creating a new time entry via POST /api/v1/time-entries
     * Expects 201 Created with correct response body.
     */
    @Test
    @DisplayName("POST /api/v1/time-entries → 201 Created")
    void create_entry_ok() throws Exception {
        var req = new TimeEntryDto(
                101,
                LocalDate.parse("2025-09-06"),
                "API Development",
                new BigDecimal("6"),
                new BigDecimal("60"),
                null,   // taskId
                null,   // taskName
                null,   // id
                null    // costAtEntry
        );

        var resp = new TimeEntryDto(
                101,
                req.entryDate(),
                req.description(),
                req.hours(),
                req.rateAtEntry(),
                2,               // taskId
                "Backend Task",  // taskName
                5001,            // id
                new BigDecimal("360") // costAtEntry
        );

        Mockito.when(timeEntryService.create(any(TimeEntryDto.class))).thenReturn(resp);

        mvc.perform(post("/api/v1/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(5001));
    }

    /**
     * Test deleting a time entry via DELETE /api/v1/time-entries/{entryId}
     * Expects 200 OK with success message.
     */
    @Test
    @DisplayName("DELETE /api/v1/time-entries/{entryId} → 200 OK with message")
    void delete_time_entry_ok() throws Exception {
        var entryId = 5001;

        // mock service to do nothing (void)
        Mockito.doNothing().when(timeEntryService).delete(eq(entryId));

        mvc.perform(delete("/api/v1/time-entries/{entryId}", entryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Time entry deleted successfully")) // ✅ updated
                .andExpect(jsonPath("$.data").doesNotExist()); // ✅ ensure data is not present
    }
}

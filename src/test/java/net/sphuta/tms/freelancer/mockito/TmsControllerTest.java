package net.sphuta.tms.freelancer.mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.sphuta.tms.freelancer.controller.TimesheetController;
import net.sphuta.tms.freelancer.dto.TimeEntryDtos;
import net.sphuta.tms.freelancer.dto.TimesheetDtos;
import net.sphuta.tms.freelancer.response.PageResponse;
import net.sphuta.tms.freelancer.service.TimeEntryService;
import net.sphuta.tms.freelancer.service.TimesheetService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for Timesheet/Time-entry endpoints in {@link TimesheetController}.
 *
 * <p>Notes:</p>
 * <ul>
 *   <li>Uses @WebMvcTest(TmsController.class) to load only MVC components.</li>
 *   <li>{@link TimesheetService} and {@link TimeEntryService} are mocked.</li>
 *   <li>Assertions focus on HTTP status and presence of ApiResponse envelope.</li>
 * </ul>
 */
@WebMvcTest(controllers = TimesheetController.class)
class TmsControllerTimesheetsTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @MockBean
    private TimesheetService timesheetService;

    @MockBean
    private TimeEntryService timeEntryService;

    /* =======================
     *        TIMESHEETS
     * ======================= */

    @Test
    @DisplayName("GET /api/v1/timesheets returns 200 with ApiResponse<PageResponse>")
    void list_timesheets_ok() throws Exception {
        // Arrange: build an empty page response to keep it DTO-agnostic
        PageResponse<TimesheetDtos.TimesheetSummary> page =
                new PageResponse<>(Collections.emptyList(),
                        new PageResponse.PageMeta(0, 25, 0, 0));

        Mockito.when(timesheetService.list(any(TimesheetDtos.TimesheetFilters.class)))
                .thenReturn(page);

        // Act & Assert
        mvc.perform(get("/api/v1/timesheets"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.page.number").value(0))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/v1/timesheets creates a timesheet (201)")
    void create_timesheet_ok() throws Exception {
        // Arrange: mock returned detail (opaque to avoid coupling to DTO structure)
        var detail = Mockito.mock(TimesheetDtos.TimesheetDetail.class);
        Mockito.when(timesheetService.create(any(TimesheetDtos.TimesheetCreateRequest.class)))
                .thenReturn(detail);

        // Build a minimal JSON body based on what the controller logs (projectId, periodStart, periodEnd)
        var bodyJson = """
                {
                  "projectId": "%s",
                  "periodStart": "2025-09-01",
                  "periodEnd": "2025-09-07"
                }
                """.formatted(UUID.randomUUID());

        // Act & Assert
        mvc.perform(post("/api/v1/timesheets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/timesheets/{id} returns 200 with ApiResponse")
    void get_timesheet_ok() throws Exception {
        // Arrange
        var id = UUID.randomUUID();
        var detail = Mockito.mock(TimesheetDtos.TimesheetDetail.class);
        Mockito.when(timesheetService.get(eq(id))).thenReturn(detail);

        // Act & Assert
        mvc.perform(get("/api/v1/timesheets/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PUT /api/v1/timesheets/{id}/entries bulk upsert returns 200")
    void bulk_upsert_ok() throws Exception {
        // Arrange
        var id = UUID.randomUUID();
        var resp = Mockito.mock(TimeEntryDtos.BulkUpsertResponse.class);
        Mockito.when(timesheetService.bulkUpsert(eq(id), any(TimeEntryDtos.BulkUpsertRequest.class)))
                .thenReturn(resp);

        // Minimal payload: empty entries array (controller only logs size)
        var bodyJson = """
                {
                  "entries": []
                }
                """;

        // Act & Assert
        mvc.perform(put("/api/v1/timesheets/{id}/entries", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/timesheets/{id}/submit returns 200")
    void submit_ok() throws Exception {
        // Arrange
        var id = UUID.randomUUID();
        var detail = Mockito.mock(TimesheetDtos.TimesheetDetail.class);
        Mockito.when(timesheetService.submit(eq(id))).thenReturn(detail);

        // Act & Assert
        mvc.perform(post("/api/v1/timesheets/{id}/submit", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PATCH /api/v1/timesheets/{id}/lock returns 200")
    void lock_ok() throws Exception {
        // Arrange
        var id = UUID.randomUUID();
        Mockito.doNothing().when(timesheetService).lock(eq(id));

        // Act & Assert
        mvc.perform(patch("/api/v1/timesheets/{id}/lock", id))
                .andExpect(status().isOk());
        // Body type depends on ApiResponse.message(...) factory present in your project;
        // we avoid strict JSON assertions here to keep it compatible.
    }

    /* =======================
     *      TIME ENTRIES
     * ======================= */

    @Test
    @DisplayName("POST /api/v1/time-entries creates an entry (201)")
    void create_time_entry_ok() throws Exception {
        // Arrange
        var entry = Mockito.mock(TimeEntryDtos.TimeEntryResponse.class);
        Mockito.when(timeEntryService.create(any(TimeEntryDtos.TimeEntryCreateRequest.class)))
                .thenReturn(entry);

        var bodyJson = """
                {
                  "timesheetId": "%s",
                  "entryDate": "2025-09-02",
                  "hours": 4.5
                }
                """.formatted(UUID.randomUUID());

        // Act & Assert
        mvc.perform(post("/api/v1/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/time-entries/{id} returns 204")
    void delete_time_entry_ok() throws Exception {
        // Arrange
        var entryId = UUID.randomUUID();
        Mockito.doNothing().when(timeEntryService).delete(eq(entryId));

        // Act & Assert
        mvc.perform(delete("/api/v1/time-entries/{entryId}", entryId))
                .andExpect(status().isNoContent());
    }
}
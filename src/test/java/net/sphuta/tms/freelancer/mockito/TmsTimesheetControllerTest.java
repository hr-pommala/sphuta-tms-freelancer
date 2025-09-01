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

@WebMvcTest(controllers = TmsTimesheetController.class)
class TmsTimesheetControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @MockBean
    private TmsTimesheetService timesheetService;

    @MockBean
    private TmsTimeEntryService timeEntryService;

    @Test
    @DisplayName("POST /api/v1/timesheets → 201 Created")
    void create_timesheet_created() throws Exception {
        var req = new TmsTimesheetDto(
                2001,
                LocalDate.parse("2025-09-01"),
                LocalDate.parse("2025-09-15"),
                null,
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
                .andExpect(jsonPath("$.data.id").value(101));
    }

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
                .andExpect(jsonPath("$.data.id").value(id));
    }

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
                                null,
                                null
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

    @Test
    @DisplayName("PATCH /api/v1/timesheets/{id}/lock → 200 OK")
    void lock_ok() throws Exception {
        var id = 101;
        Mockito.doNothing().when(timesheetService).lock(eq(id));

        mvc.perform(patch("/api/v1/timesheets/{id}/lock", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Timesheet locked successfully")) // ✅ updated expectation
                .andExpect(jsonPath("$.data").value("Timesheet locked")); // ✅ matches your response "data"
    }

    @Test
    @DisplayName("POST /api/v1/time-entries → 201 Created")
    void create_entry_ok() throws Exception {
        var req = new TimeEntryDto(
                101,
                LocalDate.parse("2025-09-06"),
                "API Development",
                new BigDecimal("6"),
                new BigDecimal("60"),
                null,
                null
        );

        var resp = new TimeEntryDto(
                101,
                req.entryDate(),
                req.description(),
                req.hours(),
                req.rateAtEntry(),
                5001,
                new BigDecimal("360")
        );

        Mockito.when(timeEntryService.create(any(TimeEntryDto.class))).thenReturn(resp);

        mvc.perform(post("/api/v1/time-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(5001));
    }

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

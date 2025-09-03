package net.sphuta.tms.freelancer.mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.sphuta.tms.freelancer.constants.ApiMessageConstants;
import net.sphuta.tms.freelancer.controller.TmsProjectController;
import net.sphuta.tms.freelancer.dto.TmsClientDto;
import net.sphuta.tms.freelancer.dto.TmsProjectDto;
import net.sphuta.tms.freelancer.service.TmsProjectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for {@link TmsProjectController}.
 *
 * <p>Loads MVC only; service is mocked.</p>
 */
@WebMvcTest(controllers = TmsProjectController.class)
class TmsProjectControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper om;

    @MockBean private TmsProjectService service;

    /* ===========================
     *         CLIENTS
     * =========================== */

    @Test
    @DisplayName("GET /api/v1/clients returns paged clients")
    void listClients_ok() throws Exception {
        var c1 = TmsClientDto.builder()
            .id(1)
            .companyName("Acme LLC")
            .build();
        Page<TmsClientDto> page = new PageImpl<>(List.of(c1));

        Mockito.when(service.listClients(eq(""), eq(0), eq(100))).thenReturn(page);

        mvc.perform(get("/api/v1/projects/clients")
                        .queryParam("active", "true")
                        .queryParam("search", "")
                        .queryParam("page", "0")
                        .queryParam("size", "100"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(ApiMessageConstants.CLIENTS_FETCHED))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].companyName").value("Acme LLC"))
                .andExpect(jsonPath("$.data.page.number").value(0))
                .andExpect(jsonPath("$.timestamp", not(emptyString())));
    }

    /* ===========================
     *         PROJECTS
     * =========================== */

    @Test
    @DisplayName("GET /api/v1/projects (active=true) returns active projects")
    void listProjects_active_ok() throws Exception {
        var dto = sampleProjectDto(101, true);
        Page<TmsProjectDto> page = new PageImpl<>(List.of(dto));

        Mockito.when(service.listProjects(eq(true), isNull(), eq(""), eq(0), eq(25)))
                .thenReturn(page);

        mvc.perform(get("/api/v1/projects/projects")
                        .queryParam("active", "true")
                        .queryParam("search", "")
                        .queryParam("page", "0")
                        .queryParam("size", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(ApiMessageConstants.PROJECTS_FETCHED_SUCCESS))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(101))
                .andExpect(jsonPath("$.data.content[0].projectName").value("Backend API"))
                .andExpect(jsonPath("$.data.content[0].isActive").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/projects (active=false) returns archived projects")
    void listProjects_archived_ok() throws Exception {
        var dto = sampleProjectDto(102, false);
        Page<TmsProjectDto> page = new PageImpl<>(List.of(dto));

        Mockito.when(service.listProjects(eq(false), isNull(), eq(""), eq(0), eq(25)))
                .thenReturn(page);

        mvc.perform(get("/api/v1/projects/projects")
                        .queryParam("active", "false")
                        .queryParam("search", "")
                        .queryParam("page", "0")
                        .queryParam("size", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].isActive").value(false));
    }

    @Test
    @DisplayName("GET /api/v1/projects with clientId + search filters correctly")
    void listProjects_filter_ok() throws Exception {
        var dto = sampleProjectDto(103, true);
        Page<TmsProjectDto> page = new PageImpl<>(List.of(dto));

        Mockito.when(service.listProjects(eq(true), eq(5), eq("backend"), eq(0), eq(25)))
                .thenReturn(page);

        mvc.perform(get("/api/v1/projects/projects")
                        .queryParam("active", "true")
                        .queryParam("clientId", "5")
                        .queryParam("search", "backend")
                        .queryParam("page", "0")
                        .queryParam("size", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].projectName").value("Backend API"));
    }

    @Test
    @DisplayName("POST /api/v1/projects creates and returns project + Location")
    void create_ok() throws Exception {
        var created = sampleProjectDto(201, true);

        var requestBody = new TmsProjectDto(
                null, // id (create)
                null, // client (server fills for response)
                1,
                "Backend API",
                "ACME-BE",
                new BigDecimal("65.00"),
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2025, 9, 30),
                "MVP build",
                true,
                null,
                null
        );

        Mockito.when(service.createProject(Mockito.any(TmsProjectDto.class))).thenReturn(created);

        mvc.perform(post("/api/v1/projects/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/projects/201")))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.message").value(ApiMessageConstants.PROJECT_CREATED))
                .andExpect(jsonPath("$.data.id").value(201))
                .andExpect(jsonPath("$.data.projectName").value("Backend API"));
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{id} fully updates a project")
    void put_ok() throws Exception {
        int id = 301;

        // Response we expect back after update
        var updated = new TmsProjectDto(
                id,
                new TmsClientDto(1, "Acme LLC",null,              // email
                        null,       // use displayName as companyName
                        null, null,        // firstName, lastName
                        null, null,        // phones
                        null, null,        // addresses
                        null, null,        // city, state
                        null, null,        // postal, country
                        null, null,        // reminders, late fees
                        null,              // lateFeePercent
                        null, null,        // currency, language
                        null,              // allowInvoiceAttachments
                        null,              // isActive
                        null,              // createdAt
                        null  ),
                1,
                "Backend API v2",
                "ACME-BE",
                new BigDecimal("70.00"),
                LocalDate.of(2025, 9, 5),
                LocalDate.of(2025, 10, 15),
                "Scope expanded",
                true,
                "2025-08-25T18:20:00Z",
                "2025-08-25T18:20:00Z"
        );

        // Full replacement request body
        var body = new TmsProjectDto(
                id,
                null, // client in request not needed
                1,
                "Backend API v2",
                "ACME-BE",
                new BigDecimal("70.00"),
                LocalDate.of(2025, 9, 5),
                LocalDate.of(2025, 10, 15),
                "Scope expanded",
                true,
                null,
                null
        );

        Mockito.when(service.updateProject(eq(id), Mockito.any(TmsProjectDto.class), eq(true)))
                .thenReturn(updated);

        mvc.perform(put("/api/v1/projects/projects/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(ApiMessageConstants.PROJECT_UPDATED))
                .andExpect(jsonPath("$.data.projectName").value("Backend API v2"));
    }

    @Test
    @DisplayName("PATCH /api/v1/projects/{id} partially updates a project")
    void patch_ok() throws Exception {
        int id = 302;

        // Response after patch (only description changed here)
        var patched = new TmsProjectDto(
                id,
                new TmsClientDto(1, "Acme LLC",null,              // email
                        null,       // use displayName as companyName
                        null, null,        // firstName, lastName
                        null, null,        // phones
                        null, null,        // addresses
                        null, null,        // city, state
                        null, null,        // postal, country
                        null, null,        // reminders, late fees
                        null,              // lateFeePercent
                        null, null,        // currency, language
                        null,              // allowInvoiceAttachments
                        null,              // isActive
                        null,              // createdAt
                        null  ),
                1,
                "Backend API",
                "ACME-BE",
                new BigDecimal("65.00"),
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2025, 9, 30),
                "Phase 2",
                true,
                "2025-08-25T18:20:00Z",
                "2025-08-25T18:20:00Z"
        );

        // Partial request (just description)
        var patchBody = new TmsProjectDto(
                null, null, null, // id/client/clientId unchanged
                null, // projectName
                null, // code
                null, // hourlyRate
                null, // startDate
                null, // endDate
                "Phase 2",
                null, // isActive
                null, null // createdAt/updatedAt ignored on request
        );

        Mockito.when(service.updateProject(eq(id), Mockito.any(TmsProjectDto.class), eq(false)))
                .thenReturn(patched);

        mvc.perform(patch("/api/v1/projects/projects/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(patchBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(ApiMessageConstants.PROJECT_UPDATED))
                .andExpect(jsonPath("$.data.description").value("Phase 2"));
    }

    @Test
    @DisplayName("POST /api/v1/projects/{id}/archive marks project inactive")
    void archive_ok() throws Exception {
        int id = 401;
        var archived = sampleProjectDto(id, false);

        Mockito.when(service.archiveProject(eq(id), eq(false))).thenReturn(archived);

        mvc.perform(post("/api/v1/projects/projects/{id}/archive", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(ApiMessageConstants.PROJECT_ARCHIVED))
                .andExpect(jsonPath("$.data.isActive").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/projects/{id}/unarchive marks project active")
    void unarchive_ok() throws Exception {
        int id = 402;
        var active = sampleProjectDto(id, true);

        Mockito.when(service.archiveProject(eq(id), eq(true))).thenReturn(active);

        mvc.perform(post("/api/v1/projects/projects/{id}/unarchive", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(ApiMessageConstants.PROJECT_UNARCHIVED))
                .andExpect(jsonPath("$.data.isActive").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/projects/{id} returns success envelope")
    void delete_ok() throws Exception {
        int id = 403;
        Mockito.doNothing().when(service).deleteProject(id);

        mvc.perform(delete("/api/v1/projects/projects/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(ApiMessageConstants.PROJECT_DELETED))
                .andExpect(jsonPath("$.data").doesNotExist());

        Mockito.verify(service).deleteProject(id);
    }

    /* ===========================
     *        HELPERS
     * =========================== */

    private TmsProjectDto sampleProjectDto(Integer id, boolean isActive) {
        var client =new TmsClientDto(1, "Acme LLC",null,              // email
                null,       // use displayName as companyName
                null, null,        // firstName, lastName
                null, null,        // phones
                null, null,        // addresses
                null, null,        // city, state
                null, null,        // postal, country
                null, null,        // reminders, late fees
                null,              // lateFeePercent
                null, null,        // currency, language
                null,              // allowInvoiceAttachments
                null,              // isActive
                null,              // createdAt
                null  );
        return new TmsProjectDto(
                id,
                client,
                client.id(),
                "Backend API",
                "ACME-BE",
                new BigDecimal("65.00"),
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2025, 9, 30),
                "MVP build",
                isActive,
                "2025-08-25T18:20:00Z",
                "2025-08-25T18:20:00Z"
        );
    }
}

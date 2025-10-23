package net.sphuta.tms.freelancer.mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.sphuta.tms.freelancer.constants.TmsMessages;
import net.sphuta.tms.freelancer.controller.TmsProjectController;
import net.sphuta.tms.freelancer.dto.TmsClientDto;
import net.sphuta.tms.freelancer.dto.TmsProjectDto;
import net.sphuta.tms.freelancer.service.TmsProjectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
 * Unit test class for {@link TmsProjectController}.
 *
 * <p>
 * Uses {@link WebMvcTest} to load only the web layer (controller and related configs).
 * Service layer is mocked using {@link MockBean}.
 * </p>
 *
 * <p>
 * This ensures tests verify only controller-level request/response mappings,
 * JSON serialization, status codes, and headers without touching database or service logic.
 * </p>
 */
@WebMvcTest(controllers = TmsProjectController.class)
@AutoConfigureMockMvc(addFilters = false)  // ✅ disables Spring Security filters in MockMvc
class TmsProjectControllerTest {

    /** MockMvc simulates HTTP requests and validates controller responses */
    @Autowired private MockMvc mvc;

    /** ObjectMapper for serializing/deserializing request/response bodies */
    @Autowired private ObjectMapper om;

    /** Mocked service layer to isolate controller tests */
    @MockBean private TmsProjectService service;

    /* =====================================================
     *                CLIENT ENDPOINT TESTS
     * ===================================================== */

    /**
     * Test: GET /api/v1/projects/clients
     * Scenario: Should return paginated clients list.
     * Validates JSON structure, message, and pagination fields.
     */
    @Test
    @DisplayName("GET /api/v1/clients returns paged clients")
    void listClients_ok() throws Exception {
        var c1 = TmsClientDto.builder()
                .id(1)
                .companyName("Acme LLC")
                .build();
        Page<TmsClientDto> page = new PageImpl<>(List.of(c1));

        Mockito.when(service.listClients(any())).thenReturn(page);

        mvc.perform(get("/api/v1/projects/clients")
                        .queryParam("active", "true")
                        .queryParam("search", "")
                        .queryParam("page", "0")
                        .queryParam("size", "100"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(TmsMessages.CLIENTS_FETCHED))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].companyName").value("Acme LLC"))
                .andExpect(jsonPath("$.data.page.number").value(0))
                .andExpect(jsonPath("$.timestamp", not(emptyString())));
    }

    /* =====================================================
     *               PROJECT ENDPOINT TESTS
     * ===================================================== */

    /**
     * Test: GET /api/v1/projects/projects
     * Scenario: Fetch active projects.
     */
    @Test
    @DisplayName("GET /api/v1/projects (active=true) returns active projects")
    void listProjects_active_ok() throws Exception {
        var dto = sampleProjectDto(101, true);
        Page<TmsProjectDto> page = new PageImpl<>(List.of(dto));

        Mockito.when(service.listProjects(any())).thenReturn(page);

        mvc.perform(get("/api/v1/projects/projects")
                        .queryParam("active", "true")
                        .queryParam("search", "")
                        .queryParam("page", "0")
                        .queryParam("size", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(TmsMessages.PROJECTS_FETCHED_SUCCESS))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id").value(101))
                .andExpect(jsonPath("$.data.content[0].projectName").value("Backend API"))
                .andExpect(jsonPath("$.data.content[0].isActive").value(true));
    }

    /**
     * Test: GET /api/v1/projects/projects
     * Scenario: Fetch archived (inactive) projects.
     */
    @Test
    @DisplayName("GET /api/v1/projects (active=false) returns archived projects")
    void listProjects_archived_ok() throws Exception {
        var dto = sampleProjectDto(102, false);
        Page<TmsProjectDto> page = new PageImpl<>(List.of(dto));

        Mockito.when(service.listProjects(any())).thenReturn(page);

        mvc.perform(get("/api/v1/projects/projects")
                        .queryParam("active", "false")
                        .queryParam("search", "")
                        .queryParam("page", "0")
                        .queryParam("size", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].isActive").value(false));
    }

    /**
     * Test: GET /api/v1/projects/projects
     * Scenario: Apply filters clientId + search.
     */
    @Test
    @DisplayName("GET /api/v1/projects with clientId + search filters correctly")
    void listProjects_filter_ok() throws Exception {
        var dto = sampleProjectDto(103, true);
        Page<TmsProjectDto> page = new PageImpl<>(List.of(dto));

        Mockito.when(service.listProjects(any())).thenReturn(page);

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

    /**
     * Test: POST /api/v1/projects/projects
     * Scenario: Creates a new project and returns Location header + created object.
     */
    @Test
    @DisplayName("POST /api/v1/projects creates and returns project + Location")
    void create_ok() throws Exception {
        var created = sampleProjectDto(201, true);

        var requestBody = TmsProjectDto.builder()
                .id(0)
                .client(null)
                .clientId(1)
                .userId(null)
                .projectName("Backend API")
                .code("ACME-BE")
                .hourlyRate(new BigDecimal("65.00"))
                .startDate(LocalDate.of(2025, 9, 1))
                .endDate(LocalDate.of(2025, 9, 30))
                .description("MVP build")
                .isActive(true)
                .createdDt(null)
                .updatedDt(null)
                .active(null)
                .search(null)
                .page(null)
                .size(null)
                .build();

        Mockito.when(service.createProject(Mockito.any(TmsProjectDto.class))).thenReturn(created);

        mvc.perform(post("/api/v1/projects/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/v1/projects/201")))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.message").value(TmsMessages.PROJECT_CREATED))
                .andExpect(jsonPath("$.data.id").value(201))
                .andExpect(jsonPath("$.data.projectName").value("Backend API"));
    }

    /**
     * Test: PUT /api/v1/projects/{id}
     * Scenario: Fully updates project (replacement).
     */
    @Test
    @DisplayName("PUT /api/v1/projects/{id} fully updates a project")
    void put_ok() throws Exception {
        int id = 301;

        var updated = TmsProjectDto.builder()
                .id(id)
                .client(TmsClientDto.builder().id(1).userId(1).companyName("Acme LLC").build())
                .clientId(1)
                .userId(1)
                .projectName("Backend API v2")
                .code("ACME-BE")
                .hourlyRate(new BigDecimal("70.00"))
                .startDate(LocalDate.of(2025, 9, 5))
                .endDate(LocalDate.of(2025, 10, 15))
                .description("Scope expanded")
                .isActive(true)
                .createdDt("2025-08-25T18:20:00Z")
                .updatedDt("2025-08-25T18:20:00Z")
                .active(null)
                .search(null)
                .page(null)
                .size(null)
                .build();

        var body = TmsProjectDto.builder()
                .id(id)
                .client(null)
                .clientId(1)
                .userId(null)
                .projectName("Backend API v2")
                .code("ACME-BE")
                .hourlyRate(new BigDecimal("70.00"))
                .startDate(LocalDate.of(2025, 9, 5))
                .endDate(LocalDate.of(2025, 10, 15))
                .description("Scope expanded")
                .isActive(true)
                .createdDt(null)
                .updatedDt(null)
                .active(null)
                .search(null)
                .page(null)
                .size(null)
                .build();

        Mockito.when(service.updateProject(eq(id), Mockito.any(TmsProjectDto.class)))
                .thenReturn(updated);

        mvc.perform(put("/api/v1/projects/projects/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(TmsMessages.PROJECT_UPDATED))
                .andExpect(jsonPath("$.data.projectName").value("Backend API v2"));
    }

    /**
     * Test: POST /api/v1/projects/{id}/archive
     * Scenario: Marks project as inactive.
     */
    @Test
    @DisplayName("POST /api/v1/projects/{id}/archive marks project inactive")
    void archive_ok() throws Exception {
        int id = 401;
        var archived = sampleProjectDto(id, false);

        Mockito.when(service.archiveProject(eq(id), eq(false))).thenReturn(archived);

        mvc.perform(post("/api/v1/projects/projects/{id}/archive", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(TmsMessages.PROJECT_ARCHIVED))
                .andExpect(jsonPath("$.data.isActive").value(false));
    }

    /**
     * Test: POST /api/v1/projects/{id}/unarchive
     * Scenario: Marks project as active.
     */
    @Test
    @DisplayName("POST /api/v1/projects/{id}/unarchive marks project active")
    void unarchive_ok() throws Exception {
        int id = 402;
        var active = sampleProjectDto(id, true);

        Mockito.when(service.archiveProject(eq(id), eq(true))).thenReturn(active);

        mvc.perform(post("/api/v1/projects/projects/{id}/unarchive", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(TmsMessages.PROJECT_UNARCHIVED))
                .andExpect(jsonPath("$.data.isActive").value(true));
    }

    /**
     * Test: DELETE /api/v1/projects/{id}
     * Scenario: Deletes a project successfully.
     */
    @Test
    @DisplayName("DELETE /api/v1/projects/{id} returns success envelope")
    void delete_ok() throws Exception {
        int id = 403;
        Mockito.doNothing().when(service).deleteProject(id);

        mvc.perform(delete("/api/v1/projects/projects/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(TmsMessages.PROJECT_DELETED))
                .andExpect(jsonPath("$.data").doesNotExist());

        Mockito.verify(service).deleteProject(id);
    }

    /* =====================================================
     *                  HELPER METHODS
     * ===================================================== */

    /**
     * Helper method to build sample {@link TmsProjectDto} with dummy data.
     *
     * @param id       project ID
     * @param isActive active status
     * @return sample DTO instance
     */
    private TmsProjectDto sampleProjectDto(Integer id, boolean isActive) {
        var client = TmsClientDto.builder()
                .id(1)
                .userId(1)
                .companyName("Acme LLC")
                .build();
        return TmsProjectDto.builder()
                .id(id != null ? id : 0)
                .client(client)
                .clientId(client.id())
                .userId(client.userId())
                .projectName("Backend API")
                .code("ACME-BE")
                .hourlyRate(new BigDecimal("65.00"))
                .startDate(LocalDate.of(2025, 9, 1))
                .endDate(LocalDate.of(2025, 9, 30))
                .description("MVP build")
                .isActive(isActive)
                .createdDt("2025-08-25T18:20:00Z")
                .updatedDt("2025-08-25T18:20:00Z")
                .active(null)
                .search(null)
                .page(null)
                .size(null)
                .build();
    }
}

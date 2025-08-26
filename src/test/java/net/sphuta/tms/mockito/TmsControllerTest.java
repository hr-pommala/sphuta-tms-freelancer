package net.sphuta.tms.mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.sphuta.tms.controller.TmsController;
import net.sphuta.tms.dto.ClientDto;
import net.sphuta.tms.dto.ProjectDtos;
import net.sphuta.tms.response.PageResponse;
import net.sphuta.tms.service.TmsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web layer tests for {@link TmsController}.
 *
 * <p>Notes:</p>
 * <ul>
 *   <li>Uses {@link WebMvcTest} to load only MVC components.</li>
 *   <li>{@link TmsService} is mocked via {@link MockBean}.</li>
 *   <li>Verifies status codes, envelope shape, and selected fields.</li>
 * </ul>
 */
@WebMvcTest(controllers = TmsController.class)
class TmsControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper om;

    @MockBean
    private TmsService tmsService;

    /* ===========================
     *         CLIENTS
     * =========================== */

    @Test
    @DisplayName("GET /api/v1/clients returns paged clients in ApiResponse envelope")
    void listClients_ok() throws Exception {
        // Arrange
        var c1 = ClientDto.builder().id(UUID.randomUUID().toString()).name("Acme LLC").build();
        Page<ClientDto> page = new PageImpl<>(List.of(c1));

        Mockito.when(tmsService.listClients(eq(""), eq(0), eq(100)))
                .thenReturn(page);

        // Act & Assert
        mvc.perform(get("/api/v1/clients")
                        .queryParam("active", "true")
                        .queryParam("search", "")
                        .queryParam("page", "0")
                        .queryParam("size", "100"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].id", not(emptyString())))
                .andExpect(jsonPath("$.data.content[0].name").value("Acme LLC"))
                .andExpect(jsonPath("$.data.page.number").value(0))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    /* ===========================
     *         PROJECTS
     * =========================== */

    @Test
    @DisplayName("GET /api/v1/projects?active=true returns active projects in envelope")
    void listActive_ok() throws Exception {
        // Arrange
        var view = sampleProjectView("prj-1");
        Page<ProjectDtos.View> page = new PageImpl<>(List.of(view));

        Mockito.when(tmsService.listActiveProjects(eq(0), eq(25)))
                .thenReturn(page);

        // Act & Assert
        mvc.perform(get("/api/v1/projects")
                        .queryParam("active", "true")
                        .queryParam("page", "0")
                        .queryParam("size", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content", hasSize(1)))
                .andExpect(jsonPath("$.data.content[0].projectName").value("Backend API"))
                .andExpect(jsonPath("$.data.content[0].isActive").value(true))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/v1/projects?active=false returns archived projects in envelope")
    void listArchived_ok() throws Exception {
        // Arrange
        var archived = sampleProjectView("prj-2");
        archived.setIsActive(false);
        Page<ProjectDtos.View> page = new PageImpl<>(List.of(archived));

        Mockito.when(tmsService.listArchivedProjects(eq(0), eq(25)))
                .thenReturn(page);

        // Act & Assert
        mvc.perform(get("/api/v1/projects")
                        .queryParam("active", "false")
                        .queryParam("page", "0")
                        .queryParam("size", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].isActive").value(false))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/v1/projects?active=true&clientId=...&search=... filters by owner+search")
    void listActiveByOwnerAndSearch_ok() throws Exception {
        // Arrange
        var view = sampleProjectView("prj-3");
        Page<ProjectDtos.View> page = new PageImpl<>(List.of(view));

        String clientId = UUID.randomUUID().toString();
        Mockito.when(tmsService.listActiveProjectsByOwnerAndSearch(eq(clientId), eq("backend"), eq(0), eq(25)))
                .thenReturn(page);

        // Act & Assert
        mvc.perform(get("/api/v1/projects")
                        .queryParam("active", "true")
                        .queryParam("clientId", clientId)
                        .queryParam("search", "backend")
                        .queryParam("page", "0")
                        .queryParam("size", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].projectName").value("Backend API"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/v1/projects creates and returns project in envelope with Location header")
    void create_ok() throws Exception {
        // Arrange
        var created = sampleProjectView("prj-4");
        Mockito.when(tmsService.createProject(Mockito.any(ProjectDtos.Create.class)))
                .thenReturn(created);

        var body = ProjectDtos.Create.builder()
                .clientId(UUID.randomUUID().toString())
                .projectName("Backend API")
                .code("ACME-BE")
                .hourlyRate(new BigDecimal("65.00"))
                .startDate(LocalDate.of(2025, 9, 1))
                .endDate(LocalDate.of(2025, 9, 30))
                .description("MVP build")
                .isActive(true)
                .build();

        // Act & Assert
        mvc.perform(post("/api/v1/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", notNullValue()))
                .andExpect(header().string("Location", containsString("/api/v1/projects/")))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectName").value("Backend API"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    @DisplayName("PUT /api/v1/projects/{id} fully updates a project")
    void put_ok() throws Exception {
        // Arrange
        var id = UUID.randomUUID();
        var updated = sampleProjectView("prj-5");
        updated.setProjectName("Backend API v2");
        Mockito.when(tmsService.updateProject(eq(id), Mockito.any(ProjectDtos.Update.class), eq(true)))
                .thenReturn(updated);

        var body = ProjectDtos.Update.builder()
                .clientId(UUID.randomUUID().toString())
                .projectName("Backend API v2")
                .code("ACME-BE")
                .hourlyRate(new BigDecimal("70.00"))
                .startDate(LocalDate.of(2025, 9, 5))
                .endDate(LocalDate.of(2025, 10, 15))
                .description("Scope expanded")
                .isActive(true)
                .build();

        // Act & Assert
        mvc.perform(put("/api/v1/projects/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.projectName").value("Backend API v2"));
    }

    @Test
    @DisplayName("PATCH /api/v1/projects/{id} partially updates a project")
    void patch_ok() throws Exception {
        // Arrange
        var id = UUID.randomUUID();
        var updated = sampleProjectView("prj-6");
        updated.setDescription("Phase 2");
        Mockito.when(tmsService.updateProject(eq(id), Mockito.any(ProjectDtos.Update.class), eq(false)))
                .thenReturn(updated);

        var body = ProjectDtos.Update.builder()
                .description("Phase 2")
                .build();

        // Act & Assert
        mvc.perform(patch("/api/v1/projects/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.description").value("Phase 2"));
    }

    @Test
    @DisplayName("POST /api/v1/projects/{id}/archive toggles to archived")
    void archive_ok() throws Exception {
        // Arrange
        var id = UUID.randomUUID();
        var archived = sampleProjectView("prj-7");
        archived.setIsActive(false);
        Mockito.when(tmsService.archiveProject(eq(id), eq(false)))
                .thenReturn(archived);

        // Act & Assert
        mvc.perform(post("/api/v1/projects/{id}/archive", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isActive").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/projects/{id}/unarchive toggles to active")
    void unarchive_ok() throws Exception {
        // Arrange
        var id = UUID.randomUUID();
        var active = sampleProjectView("prj-8");
        active.setIsActive(true);
        Mockito.when(tmsService.archiveProject(eq(id), eq(true)))
                .thenReturn(active);

        // Act & Assert
        mvc.perform(post("/api/v1/projects/{id}/unarchive", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isActive").value(true));
    }

    @Test
    @DisplayName("DELETE /api/v1/projects/{id} returns 204 and calls service")
    void delete_ok() throws Exception {
        // Arrange
        var id = UUID.randomUUID();
        Mockito.doNothing().when(tmsService).deleteProject(id);

        // Act & Assert
        mvc.perform(delete("/api/v1/projects/{id}", id))
                .andExpect(status().isNoContent());

        Mockito.verify(tmsService).deleteProject(id);
    }

    /* ===========================
     *        HELPERS
     * =========================== */

    /**
     * Builds a sample ProjectDtos.View for tests.
     */
    private ProjectDtos.View sampleProjectView(String idSuffix) {
        var client = ClientDto.builder()
                .id(UUID.randomUUID().toString())
                .name("Acme LLC")
                .build();

        return ProjectDtos.View.builder()
                .id(idSuffix) // string is fine; controller just relays view
                .client(client)
                .projectName("Backend API")
                .code("ACME-BE")
                .hourlyRate(new BigDecimal("65.00"))
                .startDate(LocalDate.of(2025, 9, 1))
                .endDate(LocalDate.of(2025, 9, 30))
                .description("MVP build")
                .isActive(true)
                .createdAt("2025-08-25T18:20:00Z")
                .updatedAt("2025-08-25T18:20:00Z")
                .build();
    }
}

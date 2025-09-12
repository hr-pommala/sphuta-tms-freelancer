package net.sphuta.tms.freelancer.mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
//import net.sphuta.tms.freelancer.controller.TimeClientTimeEntryController;
import net.sphuta.tms.freelancer.controller.TmsClientController;
import net.sphuta.tms.freelancer.controller.TmsClientEstimateController;
//import net.sphuta.tms.freelancer.controller.TmsClientInvoiceController;
import net.sphuta.tms.freelancer.dto.*;
import net.sphuta.tms.freelancer.service.impl.TmsClientServiceImpl;
import net.sphuta.tms.freelancer.service.impl.TmsEstimateService;
import net.sphuta.tms.freelancer.service.impl.TmsInvoiceService;
//import net.sphuta.tms.freelancer.service.impl.TmsTimeEntryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static net.sphuta.tms.freelancer.constants.TmsMessages.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ==========================================================
 * {@code TmsClientControllerTest}
 * ==========================================================
 *
 * Integration-style Web layer tests for client-facing controllers.
 *
 * <p><b>Purpose:</b>
 * - Verify HTTP → Controller → Service delegation and response structure.
 * - Run controller unit tests with mocked service dependencies (no DB).</p>
 *
 * <p><b>Controllers covered:</b></p>

 * - {@link TmsClientController} <br>
 * - {@link TmsClientEstimateController} <br>

 *
 * <p>Logging policy for tests:
 * - DEBUG: test setup, mock stubbing, input/params. <br>
 * - INFO: test completion / notable outcomes. <br>
 * - ERROR: only used when a test experiences an unexpected exception (not common).</p>
 */
@Slf4j
@WebMvcTest(controllers = {
//        TimeClientTimeEntryController.class,
        TmsClientController.class,
        TmsClientEstimateController.class,
//        TmsClientInvoiceController.class
})
public class TmsClientControllerTest {

    // -----------------------------
    // TEST INFRASTRUCTURE INJECTIONS
    // -----------------------------

    /**
     * MockMvc - entry point for server-side Spring MVC test support.
     */
    @Autowired
    MockMvc mvc;

    /**
     * Jackson mapper used for serializing/deserializing JSON bodies in tests.
     */
    @Autowired
    ObjectMapper om;

    // -----------------------------
    // MOCKED SERVICE DEPENDENCIES
    // -----------------------------

    /** Mocked TimeEntry service used by TimeClientTimeEntryController. */
//    @MockBean
//    TmsTimeEntryService timeEntryService;

    /**
     * Mocked Client service used by TmsClientController.
     */
    @MockBean
    TmsClientServiceImpl clientService;

    /**
     * Mocked Estimate service used by TmsClientEstimateController.
     */
    @MockBean
    TmsEstimateService estimateService;

    /**
     * Mocked Invoice service used by TmsClientInvoiceController.
     */
    @MockBean
    TmsInvoiceService invoiceService;

    // -----------------------------
    // TEST DATA
    // -----------------------------

    /**
     * Reusable sample client DTO used across many tests.
     */
    private TmsClientDto sampleClient;

    // -----------------------------
    // TEST LIFECYCLE
    // -----------------------------

    /**
     * Initialize common test data before each test.
     */
    @BeforeEach
    void init() {
        // Pre-populate a reusable client DTO for most tests
        sampleClient = TmsClientDto.builder()
                .id(101)
                .companyName("Acme LLC")
                .firstName(null)
                .lastName(null)
                .email("billing@acme.com")
                .isActive(true)
                .createdDt(OffsetDateTime.parse("2025-08-28T11:44:28Z"))
                .updatedDt(OffsetDateTime.parse("2025-08-29T15:22:10Z"))
                .build();

        log.debug("Test setup complete - sampleClient initialized: id={}, email={}",
                sampleClient.id(), sampleClient.email());
    }

    // ==========================================================
    // Time Entries Endpoints
    // ==========================================================
    @Nested
    class TimeEntries {

        /**
         * GET /time-entries/uninvoiced
         * Expected: 200 OK, service invoked with query params.
         */
//        @Test
//        @DisplayName("GET /time-entries/uninvoiced → 200 OK and service called")
//        void uninvoiced_ok() throws Exception {
//            log.debug("Starting test: uninvoiced_ok - stubbing timeEntryService.findUninvoiced(...)");
//
//            // Mock response
//            TmsUninvoicedResponse resp = TmsUninvoicedResponse.builder()
//                    .clientId(7)
//                    .from(LocalDate.parse("2025-08-01"))
//                    .to(LocalDate.parse("2025-08-29"))
//                    .entries(emptyList())
//                    .build();
//            when(timeEntryService.findUninvoiced(eq(7),
//                    eq(LocalDate.parse("2025-08-01")),
//                    eq(LocalDate.parse("2025-08-29")))).thenReturn(resp);
//
//            // Perform request
//            mvc.perform(get(TIME_ENTRY_BASE_PATH + TIME_ENTRY_UNINVOICED_PATH)
//                            .param("clientId", "7")
//                            .param("from", "2025-08-01")
//                            .param("to", "2025-08-29"))
//                    .andExpect(status().isOk());
//
//            // Verify delegation
//            verify(timeEntryService).findUninvoiced(7,
//                    LocalDate.parse("2025-08-01"),
//                    LocalDate.parse("2025-08-29"));
//
//            log.info("Test uninvoiced_ok completed successfully");
//        }

        /**
         * GET /time-entries/uninvoiced with inverted date range.
         * Expected: 200 OK (controller does not enforce validation).
         */
//        @Test
//        @DisplayName("GET uninvoiced works even if from > to")
//        void uninvoiced_invertedRange_ok() throws Exception {
//            log.debug("Starting test: uninvoiced_invertedRange_ok - stubbing service for inverted range");
//
//            when(timeEntryService.findUninvoiced(anyInt(), any(), any()))
//                    .thenReturn(TmsUninvoicedResponse.builder()
//                            .clientId(7).from(LocalDate.parse("2025-08-29"))
//                            .to(LocalDate.parse("2025-08-01"))
//                            .entries(emptyList()).build());
//
//            mvc.perform(get(TIME_ENTRY_BASE_PATH + TIME_ENTRY_UNINVOICED_PATH)
//                            .param("clientId", "7")
//                            .param("from", "2025-08-29")
//                            .param("to", "2025-08-01"))
//                    .andExpect(status().isOk());
//
//            verify(timeEntryService).findUninvoiced(eq(7),
//                    eq(LocalDate.parse("2025-08-29")),
//                    eq(LocalDate.parse("2025-08-01")));
//
//            log.info("Test uninvoiced_invertedRange_ok completed successfully");
//        }
    }

    // ==========================================================
    // Clients CRUD Endpoints
    // ==========================================================
    @Nested
    class Clients {

        /**
         * GET /clients → list clients with paging
         */
        @Test
        @DisplayName("GET /clients list → 200 OK and delegates to service.list")
        void list_ok() throws Exception {
            log.debug("Starting test: list_ok - preparing page result and stubbing clientService.list(...)");

            Page<TmsClientDto> page = new PageImpl<>(List.of(sampleClient), PageRequest.of(0, 25), 1);
            when(clientService.list(true, "", 0, 25)).thenReturn(page);

            mvc.perform(get(CLIENT_BASE_PATH)
                            .param("active", "true")
                            .param("search", "")
                            .param("page", "0")
                            .param("size", "25"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].id").value(101))
                    .andExpect(jsonPath("$.data[0].companyName").value("Acme LLC"))
                    .andExpect(jsonPath("$.data[0].email").value("billing@acme.com"));

            verify(clientService).list(true, "", 0, 25);

            log.info("Test list_ok completed - service.list verified");
        }

        /**
         * GET /clients/{id} → found
         */
        @Test
        @DisplayName("GET /clients/{id} found → 200 OK")
        void get_found() throws Exception {
            log.debug("Starting test: get_found - stubbing clientService.get(101)");

            when(clientService.get(101)).thenReturn(sampleClient);

            mvc.perform(get(CLIENT_BASE_PATH + "/{id}", 101))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.statusCode").value(200))
                    .andExpect(jsonPath("$.message").value("Client fetched"))
                    .andExpect(jsonPath("$.status").value("OK"));

            verify(clientService).get(101);

            log.info("Test get_found completed successfully");
        }

        /**
         * GET /clients/{id} → not found
         */
        @Test
        @DisplayName("GET /clients/{id} not found → 404")
        void get_notFound() throws Exception {
            log.debug("Starting test: get_notFound - stubbing clientService.get(999) to throw 404");

            when(clientService.get(999)).thenThrow(new net.sphuta.tms.freelancer.exception.TmsException(org.springframework.http.HttpStatus.NOT_FOUND, "Client not found"));

            mvc.perform(get(CLIENT_BASE_PATH + "/{id}", 999))
                    .andExpect(status().isNotFound());

            verify(clientService).get(999);

            log.info("Test get_notFound completed - 404 behavior verified");
        }

        /**
         * POST /clients → create new client
         */

        @Test
        @DisplayName("POST /clients → 201 Created + Location header")
        void create_created201() throws Exception {
            TmsClientDto sampleDto = TmsClientDto.builder()
                    .id(101)
                    .userId(1)
                    .email("billing@acme.com")
                    .companyName("Acme LLC")
                    .isActive(true)
                    .build();

            when(clientService.create(any(TmsClientDto.class))).thenReturn(sampleDto);

            Map<String,Object> reqBody = new HashMap<>();
            reqBody.put("userId", 1);
            reqBody.put("firstName", "John");
            reqBody.put("lastName", "Doe");
            reqBody.put("email", "billing@acme.com");
            reqBody.put("companyName", "Acme LLC");

            mvc.perform(post(CLIENT_BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(reqBody)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString(CLIENT_BASE_PATH + "/101")));

            ArgumentCaptor<TmsClientDto> captor = ArgumentCaptor.forClass(TmsClientDto.class);
            verify(clientService).create(captor.capture());
            assertEquals("billing@acme.com", captor.getValue().email());
        }



        /**
         * PUT /clients/{id} → full replace
         */
        @Test
        @DisplayName("PUT /clients/{id} → 200 OK")
        void replace_ok() throws Exception {
            // make sampleDto (service returns TmsClientDto)
            TmsClientDto sampleDto = TmsClientDto.builder()
                    .id(101)
                    .userId(1)
                    .email("billing@acme.com")
                    .companyName("Acme LLC")
                    .isActive(true)
                    .build();

            when(clientService.update(eq(101), any(TmsClientDto.class))).thenReturn(sampleDto);

            Map<String,Object> reqBody = new HashMap<>();
            reqBody.put("userId", 1);
            reqBody.put("firstName", "John");
            reqBody.put("lastName", "Doe");
            reqBody.put("email", "billing@acme.com");
            reqBody.put("companyName", "Acme LLC");

            mvc.perform(put(CLIENT_BASE_PATH + "/{id}", 101)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(reqBody)))
                    .andDo(print())
                    .andExpect(status().isOk());

            verify(clientService).update(eq(101), any(TmsClientDto.class));
        }



        /**
         * DELETE /clients/{id} → delete client
         */
        @Test
        @DisplayName("DELETE /clients/{id} → 200 OK")
        void delete_ok() throws Exception {
            log.debug("Starting test: delete_ok - stubbing clientService.delete(101)");

            doNothing().when(clientService).delete(101);

            mvc.perform(delete(CLIENT_BASE_PATH + "/{id}", 101))
                    .andExpect(status().isOk());

            verify(clientService).delete(101);

            log.info("Test delete_ok completed - delete delegation verified");
        }

        /**
         * POST /clients/{id}/archive → archive client
         */
        @Test
        @DisplayName("POST /clients/{id}/archive → 200 OK")
        void archive_ok() throws Exception {
            log.debug("Starting test: archive_ok - stubbing clientService.archive(101)");

            when(clientService.archive(101)).thenReturn(sampleClient.toBuilder().isActive(false).build());

            mvc.perform(post(CLIENT_BASE_PATH + "/{id}/archive", 101))
                    .andExpect(status().isOk());

            verify(clientService).archive(101);

            log.info("Test archive_ok completed - archive delegation verified");
        }

        /**
         * POST /clients/{id}/unarchive → unarchive client
         */
        @Test
        @DisplayName("POST /clients/{id}/unarchive → 200 OK")
        void unarchive_ok() throws Exception {
            log.debug("Starting test: unarchive_ok - stubbing clientService.unarchive(101)");

            when(clientService.unarchive(101)).thenReturn(sampleClient.toBuilder().isActive(true).build());

            mvc.perform(post(CLIENT_BASE_PATH + "/{id}/unarchive", 101))
                    .andExpect(status().isOk());

            verify(clientService).unarchive(101);

            log.info("Test unarchive_ok completed - unarchive delegation verified");
        }

        /**
         * GET /clients/export → CSV response
         */
        @Test
        @DisplayName("GET /clients/export → CSV with header and rows")
        void exportCsv_ok() throws Exception {
            log.debug("Starting test: exportCsv_ok - stubbing clientService.exportCsv(...)");

            String csv = "id,companyName,firstName,lastName,email,isActive\n101,Acme LLC,,,billing@acme.com,true\n";
            when(clientService.exportCsv("true", "acme")).thenReturn(csv.getBytes());

            mvc.perform(get(CLIENT_BASE_PATH + "/export")
                            .param("active", "true")
                            .param("search", "acme"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", containsString("text/csv")))
                    .andExpect(content().string(containsString("id,companyName,firstName,lastName,email,isActive")))
                    .andExpect(content().string(containsString("Acme LLC")));

            log.info("Test exportCsv_ok completed - CSV content and headers validated");
        }
    }

}


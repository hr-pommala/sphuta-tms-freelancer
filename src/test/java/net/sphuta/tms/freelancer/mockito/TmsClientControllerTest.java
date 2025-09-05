package net.sphuta.tms.freelancer.mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.sphuta.tms.freelancer.controller.TimeClientTimeEntryController;
import net.sphuta.tms.freelancer.controller.TmsClientController;
import net.sphuta.tms.freelancer.controller.TmsClientEstimateController;
import net.sphuta.tms.freelancer.controller.TmsClientInvoiceController;
import net.sphuta.tms.freelancer.dto.*;
import net.sphuta.tms.freelancer.service.impl.TmsClientServiceImpl;
import net.sphuta.tms.freelancer.service.impl.TmsEstimateService;
import net.sphuta.tms.freelancer.service.impl.TmsInvoiceService;
import net.sphuta.tms.freelancer.service.impl.TmsTimeEntryService;
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
import java.util.List;

import static java.util.Collections.emptyList;
import static net.sphuta.tms.freelancer.constants.TmsMessages.*;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
 * - {@link TimeClientTimeEntryController} <br>
 * - {@link TmsClientController} <br>
 * - {@link TmsClientEstimateController} <br>
 * - {@link TmsClientInvoiceController} <br>
 *
 * <p>Logging policy for tests:
 * - DEBUG: test setup, mock stubbing, input/params. <br>
 * - INFO: test completion / notable outcomes. <br>
 * - ERROR: only used when a test experiences an unexpected exception (not common).</p>
 */
@Slf4j
@WebMvcTest(controllers = {
        TimeClientTimeEntryController.class,
        TmsClientController.class,
        TmsClientEstimateController.class,
        TmsClientInvoiceController.class
})
public class TmsClientControllerTest {

    // -----------------------------
    // TEST INFRASTRUCTURE INJECTIONS
    // -----------------------------

    /** MockMvc - entry point for server-side Spring MVC test support. */
    @Autowired
    MockMvc mvc;

    /** Jackson mapper used for serializing/deserializing JSON bodies in tests. */
    @Autowired
    ObjectMapper om;

    // -----------------------------
    // MOCKED SERVICE DEPENDENCIES
    // -----------------------------

    /** Mocked TimeEntry service used by TimeClientTimeEntryController. */
    @MockBean
    TmsTimeEntryService timeEntryService;

    /** Mocked Client service used by TmsClientController. */
    @MockBean
    TmsClientServiceImpl clientService;

    /** Mocked Estimate service used by TmsClientEstimateController. */
    @MockBean
    TmsEstimateService estimateService;

    /** Mocked Invoice service used by TmsClientInvoiceController. */
    @MockBean
    TmsInvoiceService invoiceService;

    // -----------------------------
    // TEST DATA
    // -----------------------------

    /** Reusable sample client DTO used across many tests. */
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
                .createdAt(OffsetDateTime.parse("2025-08-28T11:44:28Z"))
                .updatedAt(OffsetDateTime.parse("2025-08-29T15:22:10Z"))
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
        @Test
        @DisplayName("GET /time-entries/uninvoiced → 200 OK and service called")
        void uninvoiced_ok() throws Exception {
            log.debug("Starting test: uninvoiced_ok - stubbing timeEntryService.findUninvoiced(...)");

            // Mock response
            TmsUninvoicedResponse resp = TmsUninvoicedResponse.builder()
                    .clientId(7)
                    .from(LocalDate.parse("2025-08-01"))
                    .to(LocalDate.parse("2025-08-29"))
                    .entries(emptyList())
                    .build();
            when(timeEntryService.findUninvoiced(eq(7),
                    eq(LocalDate.parse("2025-08-01")),
                    eq(LocalDate.parse("2025-08-29")))).thenReturn(resp);

            // Perform request
            mvc.perform(get(TIME_ENTRY_BASE_PATH + TIME_ENTRY_UNINVOICED_PATH)
                            .param("clientId", "7")
                            .param("from", "2025-08-01")
                            .param("to", "2025-08-29"))
                    .andExpect(status().isOk());

            // Verify delegation
            verify(timeEntryService).findUninvoiced(7,
                    LocalDate.parse("2025-08-01"),
                    LocalDate.parse("2025-08-29"));

            log.info("Test uninvoiced_ok completed successfully");
        }

        /**
         * GET /time-entries/uninvoiced with inverted date range.
         * Expected: 200 OK (controller does not enforce validation).
         */
        @Test
        @DisplayName("GET uninvoiced works even if from > to")
        void uninvoiced_invertedRange_ok() throws Exception {
            log.debug("Starting test: uninvoiced_invertedRange_ok - stubbing service for inverted range");

            when(timeEntryService.findUninvoiced(anyInt(), any(), any()))
                    .thenReturn(TmsUninvoicedResponse.builder()
                            .clientId(7).from(LocalDate.parse("2025-08-29"))
                            .to(LocalDate.parse("2025-08-01"))
                            .entries(emptyList()).build());

            mvc.perform(get(TIME_ENTRY_BASE_PATH + TIME_ENTRY_UNINVOICED_PATH)
                            .param("clientId", "7")
                            .param("from", "2025-08-29")
                            .param("to", "2025-08-01"))
                    .andExpect(status().isOk());

            verify(timeEntryService).findUninvoiced(eq(7),
                    eq(LocalDate.parse("2025-08-29")),
                    eq(LocalDate.parse("2025-08-01")));

            log.info("Test uninvoiced_invertedRange_ok completed successfully");
        }
    }

    // ==========================================================
    // Clients CRUD Endpoints
    // ==========================================================
    @Nested
    class Clients {

        /** GET /clients → list clients with paging */
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

        /** GET /clients/{id} → found */
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

        /** GET /clients/{id} → not found */
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

        /** POST /clients → create new client */
        @Test
        @DisplayName("POST /clients → 201 Created + Location header")
        void create_created201() throws Exception {
            log.debug("Starting test: create_created201 - preparing request DTO and stubbing clientService.create(...)");

            TmsClientDto req = TmsClientDto.builder()
                    .email("billing@acme.com").companyName("Acme LLC").build();

            when(clientService.create(any())).thenReturn(sampleClient);

            mvc.perform(post(CLIENT_BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString(CLIENT_BASE_PATH + "/101")));

            // Capture service input
            ArgumentCaptor<TmsClientDto> captor = ArgumentCaptor.forClass(TmsClientDto.class);
            verify(clientService).create(captor.capture());
            assertEquals("billing@acme.com", captor.getValue().email());

            log.info("Test create_created201 completed - request captured and verified");
        }

        /** PUT /clients/{id} → full replace */
        @Test
        @DisplayName("PUT /clients/{id} → 200 OK")
        void replace_ok() throws Exception {
            log.debug("Starting test: replace_ok - stubbing clientService.update(...)");

            TmsClientDto req = TmsClientDto.builder()
                    .email("billing@acme.com").companyName("Acme LLC").build();

            when(clientService.update(eq(101), any())).thenReturn(sampleClient);

            mvc.perform(put(CLIENT_BASE_PATH + "/{id}", 101)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isOk());

            verify(clientService).update(eq(101), any());

            log.info("Test replace_ok completed - update delegation verified");
        }

        /** PATCH /clients/{id} → partial update */
        @Test
        @DisplayName("PATCH /clients/{id} → 200 OK")
        void patch_ok() throws Exception {
            log.debug("Starting test: patch_ok - stubbing partial update result");

            TmsClientDto req = TmsClientDto.builder().email("new@acme.com").build();

            when(clientService.update(eq(101), any())).thenReturn(
                    sampleClient.toBuilder().email("new@acme.com").build()
            );

            mvc.perform(patch(CLIENT_BASE_PATH + "/{id}", 101)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isOk());

            verify(clientService).update(eq(101), any());

            log.info("Test patch_ok completed - partial update flow validated");
        }

        /** DELETE /clients/{id} → delete client */
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

        /** POST /clients/{id}/archive → archive client */
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

        /** POST /clients/{id}/unarchive → unarchive client */
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

        /** GET /clients/export → CSV response */
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

    // ==========================================================
    // Invoice Endpoints
    // ==========================================================

    /** POST /invoices → create invoice from entries */
    @Test
    @DisplayName("POST /invoices → create from timeEntryIds")
    void createInvoice_ok() throws Exception {
        log.debug("Starting test: createInvoice_ok - stubbing invoiceService.createFromEntries(...)");

        when(invoiceService.createFromEntries(any())).thenReturn(mock(TmsInvoiceDto.class));

        String json = """
        {
          "clientId": 101,
          "issueDate": "2025-08-15",
          "dueDate": "2025-09-15",
          "currencyCode": "USD",
          "notes": "Payment due in 30 days",
          "timeEntryIds": [1, 2, 3]
        }
        """;

        mvc.perform(post(INVOICE_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(invoiceService).createFromEntries(any());

        log.info("Test createInvoice_ok completed - invoice creation delegation verified");
    }

    /** POST /invoices/{id}/send → send invoice */
    @Test
    @DisplayName("POST /invoices/{id}/send → 200 OK")
    void sendInvoice_ok() throws Exception {
        log.debug("Starting test: sendInvoice_ok - stubbing invoiceService.send(777)");

        TmsInvoiceDto resp = TmsInvoiceDto.builder()
                .id(777)
                .clientId(101)
                .status("SENT")
                .build();

        when(invoiceService.send(777)).thenReturn(resp);

        mvc.perform(post(INVOICE_BASE_PATH + INVOICE_SEND_PATH, 777))
                .andExpect(status().isOk());

        verify(invoiceService).send(777);

        log.info("Test sendInvoice_ok completed - invoice send verified");
    }

    // ==========================================================
    // Estimate Endpoints
    // ==========================================================

    /** POST /estimates → create estimate */
    @Test
    @DisplayName("POST /estimates → 200 OK with valid JSON")
    void createEstimate_ok() throws Exception {
        log.debug("Starting test: createEstimate_ok - stubbing estimateService.create(...)");

        when(estimateService.create(any())).thenReturn(mock(TmsEstimateDto.class));

        String json = """
        {
          "clientId": 101,
          "issueDate": "2025-08-10",
          "validUntil": "2025-09-10",
          "currencyCode": "USD",
          "notes": "Valid for 30 days",
          "items": [
            { "description": "Design", "quantity": 10.00, "unitPrice": 50.00 }
          ]
        }
        """;

        mvc.perform(post(ESTIMATE_BASE_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        verify(estimateService).create(any());

        log.info("Test createEstimate_ok completed - estimate creation delegation verified");
    }
}

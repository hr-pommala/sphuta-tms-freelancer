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
 * TmsClientControllerTest
 * ==========================================================
 *
 * Purpose:
 * - Full WebMvcTest for client-facing controllers:
 *   - {@link TimeClientTimeEntryController}
 *   - {@link TmsClientController}
 *   - {@link TmsClientEstimateController}
 *   - {@link TmsClientInvoiceController}
 *
 * Scope:
 * - Verifies HTTP request → response flow.
 * - Asserts status codes, headers, and service interactions.
 * - Uses Mockito mocks for services (no DB or real services).
 *
 * Design:
 * - Organized into @Nested classes (TimeEntries, Clients, etc.).
 * - Each test method validates one controller endpoint.
 * - Favors black-box testing of controller layer.
 */
@WebMvcTest(controllers = {
        TimeClientTimeEntryController.class,
        TmsClientController.class,
        TmsClientEstimateController.class,
        TmsClientInvoiceController.class
})
public class TmsClientControllerTest {

    // --- Injected MVC & JSON mapper ---
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    // --- Mocked service dependencies ---
    @MockBean TmsTimeEntryService timeEntryService;
    @MockBean TmsClientServiceImpl clientService;
    @MockBean TmsEstimateService estimateService;
    @MockBean TmsInvoiceService invoiceService;

    // --- Common reusable sample client ---
    private TmsClientDto sampleClient;

    @BeforeEach
    void init() {
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
    }

    // ==========================================================
    // Time Entries (Uninvoiced) Endpoints
    // ==========================================================
    @Nested
    class TimeEntries {

        /**
         * GET /time-entries/uninvoiced
         * → returns 200 when service responds normally.
         */
        @Test
        @DisplayName("GET /time-entries/uninvoiced returns 200 and calls service with params")
        void uninvoiced_ok() throws Exception {
            // Prepare fake service response
            TmsUninvoicedResponse resp = TmsUninvoicedResponse.builder()
                    .clientId(7)
                    .from(LocalDate.parse("2025-08-01"))
                    .to(LocalDate.parse("2025-08-29"))
                    .entries(emptyList())
                    .build();

            when(timeEntryService.findUninvoiced(eq(7),
                    eq(LocalDate.parse("2025-08-01")),
                    eq(LocalDate.parse("2025-08-29")))).thenReturn(resp);

            // Perform GET request
            mvc.perform(get(TIME_ENTRY_BASE_PATH + TIME_ENTRY_UNINVOICED_PATH)
                            .param("clientId", "7")
                            .param("from", "2025-08-01")
                            .param("to", "2025-08-29"))
                    .andExpect(status().isOk());

            // Verify correct delegation
            verify(timeEntryService).findUninvoiced(7,
                    LocalDate.parse("2025-08-01"),
                    LocalDate.parse("2025-08-29"));
        }

        /**
         * GET /time-entries/uninvoiced with inverted range
         * → still 200 (controller does not enforce business validation).
         */
        @Test
        @DisplayName("GET uninvoiced works even if from > to (controller only warns)")
        void uninvoiced_invertedRange_ok() throws Exception {
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
        }
    }

    // ==========================================================
    // Clients CRUD Endpoints
    // ==========================================================
    @Nested
    class Clients {

        /** GET /clients list: verifies paging & delegation. */
        @Test
        @DisplayName("GET /clients list: returns 200 and delegates to service.list")
        void list_ok() throws Exception {
            Page<TmsClientDto> page = new PageImpl<>(
                    List.of(sampleClient),
                    PageRequest.of(0,25),
                    1
            );
            when(clientService.list(true, "", 0, 25)).thenReturn(page);

            mvc.perform(get(CLIENT_BASE_PATH)
                            .param("active", "true")
                            .param("search", "")
                            .param("page", "0")
                            .param("size", "25"))
                    .andExpect(status().isOk());

            verify(clientService).list(true, "", 0, 25);
        }

        /** GET /clients/{id}: found → 200. */
        @Test
        @DisplayName("GET /clients/{id}: found → 200")
        void get_found() throws Exception {
            Page<TmsClientDto> page = new PageImpl<>(List.of(sampleClient));
            when(clientService.list(true, "", 0, Integer.MAX_VALUE))
                    .thenReturn(page);

            mvc.perform(get(CLIENT_BASE_PATH + "/{id}", 101))
                    .andExpect(status().isOk());

            verify(clientService).list(true, "", 0, Integer.MAX_VALUE);
        }

        /** GET /clients/{id}: not found → 404. */
        @Test
        @DisplayName("GET /clients/{id}: not found → 404")
        void get_notFound() throws Exception {
            when(clientService.list(true, "", 0, Integer.MAX_VALUE))
                    .thenReturn(new PageImpl<>(List.of())); // empty

            mvc.perform(get(CLIENT_BASE_PATH + "/{id}", 999))
                    .andExpect(status().isNotFound());

            verify(clientService).list(true, "", 0, Integer.MAX_VALUE);
        }

        /** POST /clients: creates new client → 201 Created. */
        @Test
        @DisplayName("POST /clients: created → 201 + Location header")
        void create_created201() throws Exception {
            TmsClientDto req = TmsClientDto.builder()
                    .email("billing@acme.com").companyName("Acme LLC").build();

            when(clientService.create(any())).thenReturn(sampleClient);

            mvc.perform(post(CLIENT_BASE_PATH)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString(CLIENT_BASE_PATH + "/101")));

            // Capture and verify input
            ArgumentCaptor<TmsClientDto> captor = ArgumentCaptor.forClass(TmsClientDto.class);
            verify(clientService).create(captor.capture());
            assertEquals("billing@acme.com", captor.getValue().email());
        }

        /** PUT /clients/{id}: full replace → 200 OK. */
        @Test
        @DisplayName("PUT /clients/{id}: ok")
        void replace_ok() throws Exception {
            TmsClientDto req = TmsClientDto.builder()
                    .email("billing@acme.com").companyName("Acme LLC").build();

            when(clientService.update(eq(101), any())).thenReturn(sampleClient);

            mvc.perform(put(CLIENT_BASE_PATH + "/{id}", 101)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isOk());

            verify(clientService).update(eq(101), any());
        }

        /** PATCH /clients/{id}: partial update → 200 OK. */
        @Test
        @DisplayName("PATCH /clients/{id}: ok")
        void patch_ok() throws Exception {
            TmsClientDto req = TmsClientDto.builder()
                    .email("new@acme.com").build();

            when(clientService.update(eq(101), any())).thenReturn(
                    sampleClient.toBuilder().email("new@acme.com").build()
            );

            mvc.perform(patch(CLIENT_BASE_PATH + "/{id}", 101)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(req)))
                    .andExpect(status().isOk());

            verify(clientService).update(eq(101), any());
        }

        /** DELETE /clients/{id}: → 200 OK. */
        @Test
        @DisplayName("DELETE /clients/{id}: ok")
        void delete_ok() throws Exception {
            doNothing().when(clientService).delete(101);

            mvc.perform(delete(CLIENT_BASE_PATH + "/{id}", 101))
                    .andExpect(status().isOk());

            verify(clientService).delete(101);
        }

        /** POST /clients/{id}/archive: archive client → 200 OK. */
        @Test
        @DisplayName("POST /clients/{id}/archive: ok")
        void archive_ok() throws Exception {
            when(clientService.archive(101)).thenReturn(sampleClient.toBuilder().isActive(false).build());

            mvc.perform(post(CLIENT_BASE_PATH + "/{id}/archive", 101))
                    .andExpect(status().isOk());

            verify(clientService).archive(101);
        }

        /** POST /clients/{id}/unarchive: unarchive client → 200 OK. */
        @Test
        @DisplayName("POST /clients/{id}/unarchive: ok")
        void unarchive_ok() throws Exception {
            when(clientService.unarchive(101)).thenReturn(sampleClient.toBuilder().isActive(true).build());

            mvc.perform(post(CLIENT_BASE_PATH + "/{id}/unarchive", 101))
                    .andExpect(status().isOk());

            verify(clientService).unarchive(101);
        }

        /** GET /clients/export: returns CSV output. */
        @Test
        @DisplayName("GET /clients/export: text/csv with header")
        void exportCsv_ok() throws Exception {
            Page<TmsClientDto> page = new PageImpl<>(List.of(sampleClient));
            when(clientService.list(true, "acme", 0, Integer.MAX_VALUE))
                    .thenReturn(page);

            mvc.perform(get(CLIENT_BASE_PATH + "/export")
                            .param("active", "true")
                            .param("search", "acme"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", containsString("text/csv")))
                    .andExpect(content().string(containsString("id,companyName,firstName,lastName,email,isActive")))
                    .andExpect(content().string(containsString("Acme LLC")));
        }
    }

    // ==========================================================
    // Invoice Endpoints
    // ==========================================================

    /** POST /invoices: happy path with valid JSON. */
    @Test
    @DisplayName("POST /invoices: create from timeEntryIds → ok (raw JSON)")
    void createInvoice_ok() throws Exception {
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
    }

    /** POST /invoices/{invoiceId}/send: send invoice → 200 OK. */
    @Test
    @DisplayName("POST /invoices/{invoiceId}/send: ok")
    void sendInvoice_ok() throws Exception {
        TmsInvoiceDto resp = TmsInvoiceDto.builder()
                .id(777)
                .clientId(101)
                .status("SENT")
                .build();

        when(invoiceService.send(777)).thenReturn(resp);

        mvc.perform(post(INVOICE_BASE_PATH + INVOICE_SEND_PATH, 777))
                .andExpect(status().isOk());

        verify(invoiceService).send(777);
    }

    // ==========================================================
    // Estimate Endpoints
    // ==========================================================

    /** POST /estimates: happy path with valid JSON. */
    @Test
    @DisplayName("POST /estimates: ok (send valid JSON body)")
    void createEstimate_ok() throws Exception {
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
    }
}

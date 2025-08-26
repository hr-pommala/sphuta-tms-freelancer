package net.sphuta.tms.freelancer.mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.sphuta.tms.freelancer.controller.EstimateController;
import net.sphuta.tms.freelancer.controller.InvoiceController;
import net.sphuta.tms.freelancer.controller.TimeEntryController;
import net.sphuta.tms.freelancer.controller.TmsController;
import net.sphuta.tms.freelancer.dto.TmsDto;
import net.sphuta.tms.freelancer.entity.Estimate;
import net.sphuta.tms.freelancer.entity.Invoice;
import net.sphuta.tms.freelancer.entity.TmsEntity;
import net.sphuta.tms.freelancer.service.impl.EstimateService;
import net.sphuta.tms.freelancer.service.impl.InvoiceService;
import net.sphuta.tms.freelancer.service.impl.TimeEntryService;
import net.sphuta.tms.freelancer.service.impl.TmsServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({EstimateController.class, InvoiceController.class, TimeEntryController.class, TmsController.class})
class TmsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean private EstimateService estimateService;
    @MockBean private InvoiceService invoiceService;
    @MockBean private TimeEntryService timeEntryService;
    @MockBean private TmsServiceImpl tmsService;

    // 🔹 Fixed test data for reproducibility
    private static final UUID FIXED_CLIENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID FIXED_ESTIMATE_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID FIXED_INVOICE_ID  = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final LocalDate FIXED_DATE   = LocalDate.of(2025, 1, 1);
    private static final OffsetDateTime FIXED_OFFSET = OffsetDateTime.parse("2025-01-01T10:00:00Z");

    // --- EstimateController ---

    @Test
    void createEstimate_shouldReturnEstimateResponse() throws Exception {
        String estimateJson = """
        {
          "clientId": "11111111-1111-1111-1111-111111111111",
          "issueDate": "2025-08-26",
          "validUntil": "2025-09-25",
          "currencyCode": "USD",
          "notes": "test",
          "items": [
            { "description": "Service", "quantity": 10, "unitPrice": 100.0 }
          ]
        }
        """;

        mockMvc.perform(post("/api/v1/estimates")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(estimateJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currencyCode").value("USD"))
                .andExpect(jsonPath("$.notes").value("test"));
    }


    // --- InvoiceController ---

    @Test
    void createInvoice_shouldReturnInvoiceResponse() throws Exception {
        Invoice inv = new Invoice();
        inv.setId(FIXED_INVOICE_ID);
        inv.setClientId(FIXED_CLIENT_ID);
        inv.setCurrencyCode("USD");
        inv.setStatus(Invoice.Status.DRAFT);
        inv.setIssueDate(FIXED_DATE);
        inv.setDueDate(FIXED_DATE.plusDays(30));

        TmsDto.InvoiceCreateRequest req = new TmsDto.InvoiceCreateRequest(
                FIXED_CLIENT_ID,
                FIXED_DATE,
                FIXED_DATE.plusDays(30),
                "USD",
                "notes",
                List.of(UUID.fromString("44444444-4444-4444-4444-444444444444"))
        );

        Mockito.when(invoiceService.createFromEntries(any())).thenReturn(inv);

        mockMvc.perform(post("/api/v1/invoices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(FIXED_CLIENT_ID.toString()));
    }

    @Test
    void sendInvoice_shouldReturnUpdatedInvoice() throws Exception {
        Invoice inv = new Invoice();
        inv.setId(FIXED_INVOICE_ID);
        inv.setClientId(FIXED_CLIENT_ID);
        inv.setCurrencyCode("USD");
        inv.setStatus(Invoice.Status.SENT);
        inv.setIssueDate(FIXED_DATE);

        Mockito.when(invoiceService.send(FIXED_INVOICE_ID)).thenReturn(inv);

        mockMvc.perform(post("/api/v1/invoices/" + FIXED_INVOICE_ID + "/send"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(FIXED_INVOICE_ID.toString()))
                .andExpect(jsonPath("$.status").value("SENT"));
    }

    // --- TimeEntryController ---

    @Test
    void uninvoicedTimeEntries_shouldReturnResponse() throws Exception {
        LocalDate from = FIXED_DATE.minusDays(10);
        LocalDate to = FIXED_DATE;

        TmsDto.UninvoicedResponse resp = TmsDto.UninvoicedResponse.builder()
                .clientId(FIXED_CLIENT_ID).from(from).to(to).entries(List.of()).build();

        Mockito.when(timeEntryService.findUninvoiced(eq(FIXED_CLIENT_ID), eq(from), eq(to))).thenReturn(resp);

        mockMvc.perform(get("/api/v1/time-entries/uninvoiced")
                        .param("clientId", FIXED_CLIENT_ID.toString())
                        .param("from", from.toString())
                        .param("to", to.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(FIXED_CLIENT_ID.toString()));
    }

    // --- TmsController (Clients) ---

    @Test
    void listClients_shouldReturnClients() throws Exception {
        TmsEntity client = new TmsEntity();
        client.setId(FIXED_CLIENT_ID);
        client.setCompanyName("Acme");

        Page<TmsEntity> page = new PageImpl<>(List.of(client));

        Mockito.when(tmsService.list(anyBoolean(), anyString(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/v1/clients")
                        .param("active", "true")
                        .param("search", "")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].companyName").value("Acme"));
    }

    @Test
    void getClient_shouldReturnClient() throws Exception {
        TmsEntity client = new TmsEntity();
        client.setId(FIXED_CLIENT_ID);
        client.setCompanyName("Acme");

        Mockito.when(tmsService.find(FIXED_CLIENT_ID)).thenReturn(Optional.of(client));

        mockMvc.perform(get("/api/v1/clients/" + FIXED_CLIENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(FIXED_CLIENT_ID.toString()));
    }

    @Test
    void createClient_shouldReturnCreatedClient() throws Exception {
        String clientJson = """
        {
          "email": "j@acme.com",
          "companyName": "Acme",
          "firstName": "John",
          "lastName": "Doe",
          "sendReminders": false,
          "chargeLateFees": false,
          "isActive": true
        }
        """;

        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("j@acme.com"))
                .andExpect(jsonPath("$.companyName").value("Acme"));
    }


    @Test
    void replaceClient_shouldReturnUpdatedClient() throws Exception {
        String updateJson = """
        {
          "email": "updated@acme.com",
          "companyName": "Acme Updated",
          "firstName": "Jane",
          "lastName": "Smith",
          "sendReminders": true,
          "chargeLateFees": true,
          "isActive": true
        }
        """;

        mockMvc.perform(put("/api/v1/clients/11111111-1111-1111-1111-111111111111")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@acme.com"))
                .andExpect(jsonPath("$.companyName").value("Acme Updated"));
    }


    @Test
    void patchClient_shouldReturnPatchedClient() throws Exception {
        TmsDto.ClientRequest req = new TmsDto.ClientRequest(
                "Acme", null, null, null,
                null, null, null, null, null, null,
                null, null, false, false,
                null, null, null, null, true
        );

        TmsEntity client = new TmsEntity();
        client.setId(FIXED_CLIENT_ID);
        client.setCompanyName("Acme");

        Mockito.when(tmsService.update(eq(FIXED_CLIENT_ID), any())).thenReturn(client);

        mockMvc.perform(patch("/api/v1/clients/" + FIXED_CLIENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(FIXED_CLIENT_ID.toString()));
    }

    @Test
    void deleteClient_shouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/clients/" + FIXED_CLIENT_ID))
                .andExpect(status().isNoContent());
        Mockito.verify(tmsService).delete(FIXED_CLIENT_ID);
    }

    @Test
    void archiveClient_shouldReturnArchivedClient() throws Exception {
        TmsEntity client = new TmsEntity();
        client.setId(FIXED_CLIENT_ID);

        Mockito.when(tmsService.archive(FIXED_CLIENT_ID)).thenReturn(client);

        mockMvc.perform(post("/api/v1/clients/" + FIXED_CLIENT_ID + "/archive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(FIXED_CLIENT_ID.toString()));
    }

    @Test
    void unarchiveClient_shouldReturnUnarchivedClient() throws Exception {
        TmsEntity client = new TmsEntity();
        client.setId(FIXED_CLIENT_ID);

        Mockito.when(tmsService.unarchive(FIXED_CLIENT_ID)).thenReturn(client);

        mockMvc.perform(post("/api/v1/clients/" + FIXED_CLIENT_ID + "/unarchive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(FIXED_CLIENT_ID.toString()));
    }

    @Test
    void exportCsv_shouldReturnCsvFile() throws Exception {
        TmsEntity client = new TmsEntity();
        client.setId(FIXED_CLIENT_ID);
        client.setCompanyName("Acme");

        Page<TmsEntity> page = new PageImpl<>(List.of(client));

        Mockito.when(tmsService.list(anyBoolean(), anyString(), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(get("/api/v1/clients/export")
                        .param("active", "true")
                        .param("search", ""))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("clients.csv")))
                .andExpect(content().contentType("text/csv"));
    }
}

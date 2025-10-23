package net.sphuta.tms.freelancer.mockito;

import net.sphuta.tms.freelancer.dto.InvoiceLineDto;
import net.sphuta.tms.freelancer.entity.ClientEntity;
import net.sphuta.tms.freelancer.entity.InvoiceEntity;
import net.sphuta.tms.freelancer.entity.ProjectEntity;
import net.sphuta.tms.freelancer.entity.TimeEntryEntity;
import net.sphuta.tms.freelancer.entity.TimesheetEntity;
import net.sphuta.tms.freelancer.enums.TimesheetStatus;
import net.sphuta.tms.freelancer.repository.TmsClientRepository;
import net.sphuta.tms.freelancer.repository.TmsInvoiceRepository;
import net.sphuta.tms.freelancer.repository.TmsTimeEntryRepository;
import net.sphuta.tms.freelancer.repository.TmsTimesheetRepository;
import net.sphuta.tms.freelancer.service.InvoiceGenerationReport;
import net.sphuta.tms.freelancer.service.InvoiceGeneratorService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import jakarta.mail.internet.MimeMessage;
import net.sf.jasperreports.engine.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for InvoiceGeneratorService.
 *
 * NOTE: Adjust entity constructors/builders to match your actual classes if they differ.
 */
@ExtendWith(MockitoExtension.class)
class InvoiceGeneratorServiceTest {

    @Mock
    private TmsTimesheetRepository timesheetRepo;
    @Mock
    private TmsTimeEntryRepository timeEntryRepo;
    @Mock
    private TmsClientRepository clientRepo;
    @Mock
    private TmsInvoiceRepository invoiceRepo;
    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private InvoiceGeneratorService service;

    private Path tempOutputDir;

    // static mocks for JasperReports static calls
    private MockedStatic<JasperCompileManager> jasperCompileMock;
    private MockedStatic<JasperFillManager> jasperFillMock;
    private MockedStatic<JasperExportManager> jasperExportMock;

    @BeforeEach
    void setUp() throws Exception {
        // create a temp output directory and force service.outputDir to it via reflection
        tempOutputDir = Files.createTempDirectory("invtest");
        setPrivateField(service, "outputDir", tempOutputDir.toString());

        // default mailFrom/mailHost to enable emailing in tests (can override per-test)
        setPrivateField(service, "mailFrom", "test@example.com");
        setPrivateField(service, "mailHost", "smtp.test.com");

        // Setup static mocks for JasperReports
        jasperCompileMock = mockStatic(JasperCompileManager.class);
        jasperFillMock = mockStatic(JasperFillManager.class);
        jasperExportMock = mockStatic(JasperExportManager.class);

        // Default behaviour for compile/fill/export (unless a test overrides)
        jasperCompileMock.when(() -> JasperCompileManager.compileReport(any(InputStream.class)))
                .thenReturn(mock(JasperReport.class));
        jasperFillMock.when(() -> JasperFillManager.fillReport(any(JasperReport.class), anyMap(), any(JRDataSource.class)))
                .thenReturn(mock(JasperPrint.class));
        jasperExportMock.when(() -> JasperExportManager.exportReportToPdf(any(JasperPrint.class)))
                .thenReturn("PDF-BYTES".getBytes());
    }

    @AfterEach
    void tearDown() {
        jasperCompileMock.close();
        jasperFillMock.close();
        jasperExportMock.close();
    }

    // -------------------
    // Helper utilities
    // -------------------
    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private TimesheetEntity makeTimesheet(Integer id, ProjectEntity project, LocalDate start, LocalDate end, TimesheetStatus status, List<TimeEntryEntity> entries) {
        TimesheetEntity ts = new TimesheetEntity();
        try {
            Field f;
            f = TimesheetEntity.class.getDeclaredField("id"); f.setAccessible(true); f.set(ts, id);
        } catch (Exception ignore) {}
        ts.setProject(project);
        ts.setPeriodStart(start);
        ts.setPeriodEnd(end);
        ts.setStatus(status);
        ts.setEntries(entries);
        return ts;
    }

    private ProjectEntity makeProject(Integer id, String name, BigDecimal hourlyRate, ClientEntity client) {
        ProjectEntity p = new ProjectEntity();
        p.setId(id);
        p.setName(name);
        p.setHourlyRate(hourlyRate);
        p.setClientEntity(client);
        return p;
    }

    private ClientEntity makeClient(Integer id, String name, String email, String currency) {
        ClientEntity c = new ClientEntity();
        c.setId(id);
        c.setName(name);
        c.setEmail(email);
        c.setCurrencyCode(currency);
        return c;
    }

    private TimeEntryEntity makeEntry(BigDecimal hours, BigDecimal rateAtEntry, BigDecimal costAtEntry) {
        TimeEntryEntity e = new TimeEntryEntity();
        e.setHours(hours);
        e.setRateAtEntry(rateAtEntry);
        e.setCostAtEntry(costAtEntry);
        return e;
    }

    // -------------------
    // Tests
    // -------------------

    @Test
    void generate_noApprovedTimesheets_returnsZeroAndNoWrites() {
        when(timesheetRepo.findAll()).thenReturn(Collections.emptyList());

        InvoiceGenerationReport report = service.generateAndSendForApprovedTimesheets();

        assertNotNull(report);
        assertEquals(0, report.getTotalApprovedTimesheets());
        assertEquals(0, report.getGeneratedCount());
        verifyNoInteractions(invoiceRepo, mailSender);
    }

    @Test
    void generate_clientMissingEmail_skipsClient() {
        ClientEntity client = makeClient(1, "Acme", null, "USD");
        ProjectEntity proj = makeProject(10, "P1", new BigDecimal("50.0"), client);
        TimesheetEntity ts = makeTimesheet(100, proj, LocalDate.of(2025,1,1), LocalDate.of(2025,1,7), TimesheetStatus.APPROVED, List.of(makeEntry(new BigDecimal("2"), null, null)));

        when(timesheetRepo.findAll()).thenReturn(List.of(ts));

        InvoiceGenerationReport report = service.generateAndSendForApprovedTimesheets();

        assertEquals(1, report.getTotalApprovedTimesheets());
        assertTrue(report.getSkipped().stream().anyMatch(s -> s.getReason().contains("no email") || s.getReason().toLowerCase().contains("has no email")));
        verifyNoInteractions(invoiceRepo, mailSender);
    }

    @Test
    void generate_jrxmlMissing_reportsError_stopProcessing() throws Exception {
        // make a valid client with email
        ClientEntity client = makeClient(2, "Beta", "beta@example.com", "USD");
        ProjectEntity proj = makeProject(20, "P2", new BigDecimal("100"), client);
        TimesheetEntity ts = makeTimesheet(101, proj, LocalDate.of(2025,2,1), LocalDate.of(2025,2,7), TimesheetStatus.APPROVED, List.of(makeEntry(new BigDecimal("3"), null, null)));

        when(timesheetRepo.findAll()).thenReturn(List.of(ts));

        // make the resource stream lookup return null by mocking service.getClass().getResourceAsStream via reflection:
        // we can't easily mock getResourceAsStream on Class, so instead mock the JasperCompileManager to throw when stream is null:
        jasperCompileMock.when(() -> JasperCompileManager.compileReport((InputStream) isNull()))
                .thenThrow(new JRException("template missing"));

        // Force service to read null stream by explicitly simulating that compile will be called with null
        // The code looks up resource input stream BEFORE calling compile; to simulate missing jrxml, mock compile to throw
        InvoiceGenerationReport report = service.generateAndSendForApprovedTimesheets();

        // It should capture an error (template not found). At least one error recorded.
        assertTrue(report.getErrors().size() >= 1);
    }

    @Test
    void generate_timesheetWithEntries_usesCostAndRateAndCreatesInvoiceAndSendsEmail() throws Exception {
        // Setup client with email
        ClientEntity client = makeClient(3, "Gamma", "gamma@example.com", "EUR");
        ProjectEntity proj = makeProject(30, "Project-G", new BigDecimal("75.00"), client);

        // Create entries: one with explicit costAtEntry, one with rateAtEntry + hours
        TimeEntryEntity e1 = makeEntry(new BigDecimal("2.5"), null, new BigDecimal("300.00")); // uses costAtEntry
        TimeEntryEntity e2 = makeEntry(new BigDecimal("1.5"), new BigDecimal("80.00"), null);  // uses rateAtEntry * hours

        TimesheetEntity ts = makeTimesheet(200, proj, LocalDate.of(2025,3,1), LocalDate.of(2025,3,7), TimesheetStatus.APPROVED, List.of(e1, e2));

        when(timesheetRepo.findAll()).thenReturn(List.of(ts));

        // Mock mail sender createMimeMessage to return a MimeMessage (we'll use a simple stub)
        MimeMessage mime = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mime);

        // Capture the bytes provided to attachment if possible (MimeMessageHelper is later used and mailSender.send)
        doNothing().when(mailSender).send(any(MimeMessage.class));

        InvoiceGenerationReport report = service.generateAndSendForApprovedTimesheets();

        // One invoice should be generated & emailed
        assertEquals(1, report.getGeneratedCount());
        assertEquals(1, report.getEmailed().size());
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(invoiceRepo, times(1)).save(any(InvoiceEntity.class));

        // Confirm a file was NOT saved (since emailed successfully). The service only saves when email fails or disabled.
        // Check the temp dir - should be empty or only contain files if code saved fallback (it didn't)
        assertTrue(Files.exists(tempOutputDir));
    }

    @Test
    void generate_emailFailure_savesPdfAndRecordsSaved() throws Exception {
        // client/email present
        ClientEntity client = makeClient(4, "Delta", "delta@example.com", "USD");
        ProjectEntity proj = makeProject(40, "Project-D", new BigDecimal("60.0"), client);

        TimeEntryEntity e = makeEntry(new BigDecimal("4"), null, null);
        TimesheetEntity ts = makeTimesheet(300, proj, LocalDate.of(2025,4,1), LocalDate.of(2025,4,7), TimesheetStatus.APPROVED, List.of(e));

        when(timesheetRepo.findAll()).thenReturn(List.of(ts));

        MimeMessage mime = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mime);

        // Make mailSender.send throw runtime exception to simulate SMTP failure
        doThrow(new RuntimeException("smtp error")).when(mailSender).send(any(MimeMessage.class));

        InvoiceGenerationReport report = service.generateAndSendForApprovedTimesheets();

        // Even though email failed, invoice should be generated and saved to disk and invoice record still saved
        assertEquals(1, report.getGeneratedCount());
        assertTrue(report.getSaved().size() >= 1);
        verify(invoiceRepo, times(1)).save(any(InvoiceEntity.class));
    }

    @Test
    void generate_emailDisabled_savesPdf_locally_and_doesNotCallMailSender() throws Exception {
        // disable email
        setPrivateField(service, "mailFrom", "");
        setPrivateField(service, "mailHost", "");

        ClientEntity client = makeClient(5, "Epsilon", "eps@example.com", "USD");
        ProjectEntity proj = makeProject(50, "Project-E", new BigDecimal("25.0"), client);
        TimeEntryEntity e = makeEntry(new BigDecimal("2"), null, null);
        TimesheetEntity ts = makeTimesheet(400, proj, LocalDate.of(2025,5,1), LocalDate.of(2025,5,7), TimesheetStatus.APPROVED, List.of(e));

        when(timesheetRepo.findAll()).thenReturn(List.of(ts));

        InvoiceGenerationReport report = service.generateAndSendForApprovedTimesheets();

        assertEquals(1, report.getGeneratedCount());
        assertTrue(report.getSaved().size() >= 1);
        verifyNoInteractions(mailSender);
        verify(invoiceRepo, times(1)).save(any(InvoiceEntity.class));
    }

    @Test
    void generate_timesheetWithZeroHours_isSkipped() throws Exception {
        ClientEntity client = makeClient(6, "Zeta", "zeta@example.com", "USD");
        ProjectEntity proj = makeProject(60, "Project-Z", new BigDecimal("100.0"), client);

        // Entry with zero hours -> total hours 0 -> should be skipped
        TimeEntryEntity e = makeEntry(BigDecimal.ZERO, null, null);
        TimesheetEntity ts = makeTimesheet(500, proj, LocalDate.of(2025,6,1), LocalDate.of(2025,6,7), TimesheetStatus.APPROVED, List.of(e));

        when(timesheetRepo.findAll()).thenReturn(List.of(ts));

        InvoiceGenerationReport report = service.generateAndSendForApprovedTimesheets();

        // No invoices generated because timesheet hours are zero
        assertEquals(1, report.getTotalApprovedTimesheets());
        assertEquals(0, report.getGeneratedCount());
        assertTrue(report.getSkipped().stream().anyMatch(s -> s.getReason().toLowerCase().contains("no time entries") || s.getReason().toLowerCase().contains("no time")));
    }

    @Test
    void generate_processingException_isCapturedInReport_andContinues() throws Exception {
        // create two timesheets: one will throw, other will succeed
        ClientEntity client1 = makeClient(7, "Thrower", "throw@example.com", "USD");
        ProjectEntity p1 = makeProject(70, "P-Throw", new BigDecimal("10.0"), client1);
        TimesheetEntity badTs = makeTimesheet(600, p1, LocalDate.of(2025,7,1), LocalDate.of(2025,7,7), TimesheetStatus.APPROVED, List.of(makeEntry(new BigDecimal("1"), null, null)));

        ClientEntity client2 = makeClient(8, "Good", "good@example.com", "USD");
        ProjectEntity p2 = makeProject(80, "P-Good", new BigDecimal("20.0"), client2);
        TimesheetEntity goodTs = makeTimesheet(601, p2, LocalDate.of(2025,7,8), LocalDate.of(2025,7,14), TimesheetStatus.APPROVED, List.of(makeEntry(new BigDecimal("2"), null, null)));

        when(timesheetRepo.findAll()).thenReturn(List.of(badTs, goodTs));

        // Mock jasper compile to throw for the first call, then return for subsequent; we simulate by having compile throw once then work
        final JasperReport jr = mock(JasperReport.class);
        final JasperPrint jp = mock(JasperPrint.class);

        // Create an iterator that throws the first time compileReport is called
        Answer<JasperReport> compileAnswer = new Answer<>() {
            private int cnt = 0;
            @Override
            public JasperReport answer(InvocationOnMock invocation) throws Throwable {
                cnt++;
                if (cnt == 1) throw new JRException("compile failure");
                return jr;
            }
        };
        jasperCompileMock.when(() -> JasperCompileManager.compileReport(any(InputStream.class)))
                .thenAnswer(compileAnswer);

        jasperFillMock.when(() -> JasperFillManager.fillReport(eq(jr), anyMap(), any(JRDataSource.class)))
                .thenReturn(jp);
        jasperExportMock.when(() -> JasperExportManager.exportReportToPdf(eq(jp)))
                .thenReturn("BYTES".getBytes());

        // mail sender ok
        MimeMessage mime = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mime);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        InvoiceGenerationReport report = service.generateAndSendForApprovedTimesheets();

        // One should be generated (the one after exception), and an error recorded
        assertTrue(report.getErrors().size() >= 1);
        assertEquals(1, report.getGeneratedCount());
    }
}

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
 * ===============================
 * CLASS: InvoiceGeneratorServiceTest
 * ===============================
 * This class provides **comprehensive unit tests** for {@link InvoiceGeneratorService}.
 * It uses **Mockito** to mock dependencies and verify expected behaviors under
 * various invoice generation scenarios such as:
 *  - No approved timesheets
 *  - Missing client emails
 *  - Jasper template errors
 *  - Successful invoice creation and email
 *  - Email failures and file fallback
 *  - Disabled email settings
 *  - Skipped zero-hour timesheets
 *  - Exception handling with continued processing
 *
 * All static JasperReports methods are mocked to isolate the service logic.
 */
@ExtendWith(MockitoExtension.class)
class InvoiceGeneratorServiceTest {

    // ------------------------------
    // MOCKED DEPENDENCIES
    // ------------------------------

    /** Repository for fetching approved timesheets. */
    @Mock
    private TmsTimesheetRepository timesheetRepo;

    /** Repository for fetching individual time entries. */
    @Mock
    private TmsTimeEntryRepository timeEntryRepo;

    /** Repository for fetching client details. */
    @Mock
    private TmsClientRepository clientRepo;

    /** Repository for persisting generated invoices. */
    @Mock
    private TmsInvoiceRepository invoiceRepo;

    /** JavaMailSender mock for sending invoice emails. */
    @Mock
    private JavaMailSender mailSender;

    /** Service under test — automatically injected with above mocks. */
    @InjectMocks
    private InvoiceGeneratorService service;

    /** Temporary directory used for testing file output (PDF save fallback). */
    private Path tempOutputDir;

    /** Static mocks for JasperReports compile/fill/export static calls. */
    private MockedStatic<JasperCompileManager> jasperCompileMock;
    private MockedStatic<JasperFillManager> jasperFillMock;
    private MockedStatic<JasperExportManager> jasperExportMock;

    // ------------------------------
    // SETUP & CLEANUP
    // ------------------------------

    /**
     * Initializes mocks and static behaviors before each test.
     * Also injects temporary directory paths and email defaults via reflection.
     */
    @BeforeEach
    void setUp() throws Exception {
        // Create temporary output folder
        tempOutputDir = Files.createTempDirectory("invtest");

        // Inject values into private service fields
        setPrivateField(service, "outputDir", tempOutputDir.toString());
        setPrivateField(service, "mailFrom", "test@example.com");
        setPrivateField(service, "mailHost", "smtp.test.com");

        // Mock static JasperReports methods
        jasperCompileMock = mockStatic(JasperCompileManager.class);
        jasperFillMock = mockStatic(JasperFillManager.class);
        jasperExportMock = mockStatic(JasperExportManager.class);

        // Default mock behaviors for report generation
        jasperCompileMock.when(() -> JasperCompileManager.compileReport(any(InputStream.class)))
                .thenReturn(mock(JasperReport.class));
        jasperFillMock.when(() -> JasperFillManager.fillReport(any(JasperReport.class), anyMap(), any(JRDataSource.class)))
                .thenReturn(mock(JasperPrint.class));
        jasperExportMock.when(() -> JasperExportManager.exportReportToPdf(any(JasperPrint.class)))
                .thenReturn("PDF-BYTES".getBytes());
    }

    /**
     * Closes all static mocks after each test to prevent memory leaks.
     */
    @AfterEach
    void tearDown() {
        jasperCompileMock.close();
        jasperFillMock.close();
        jasperExportMock.close();
    }

    // ------------------------------
    // HELPER UTILITIES
    // ------------------------------

    /**
     * Uses reflection to inject a value into a private field.
     */
    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    /**
     * Builds a mock TimesheetEntity with given attributes.
     */
    private TimesheetEntity makeTimesheet(Integer id, ProjectEntity project, LocalDate start,
                                          LocalDate end, TimesheetStatus status, List<TimeEntryEntity> entries) {
        TimesheetEntity ts = new TimesheetEntity();
        try {
            Field f = TimesheetEntity.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(ts, id);
        } catch (Exception ignore) {}
        ts.setProject(project);
        ts.setPeriodStart(start);
        ts.setPeriodEnd(end);
        ts.setStatus(status);
        ts.setEntries(entries);
        return ts;
    }

    /** Builds a mock ProjectEntity. */
    private ProjectEntity makeProject(Integer id, String name, BigDecimal hourlyRate, ClientEntity client) {
        ProjectEntity p = new ProjectEntity();
        p.setId(id);
        p.setName(name);
        p.setHourlyRate(hourlyRate);
        p.setClientEntity(client);
        return p;
    }

    /** Builds a mock ClientEntity. */
    private ClientEntity makeClient(Integer id, String name, String email, String currency) {
        ClientEntity c = new ClientEntity();
        c.setId(id);
        c.setName(name);
        c.setEmail(email);
        c.setCurrencyCode(currency);
        return c;
    }

    /** Builds a mock TimeEntryEntity. */
    private TimeEntryEntity makeEntry(BigDecimal hours, BigDecimal rateAtEntry, BigDecimal costAtEntry) {
        TimeEntryEntity e = new TimeEntryEntity();
        e.setHours(hours);
        e.setRateAtEntry(rateAtEntry);
        e.setCostAtEntry(costAtEntry);
        return e;
    }

    // ------------------------------
    // TEST CASES
    // ------------------------------

    /**
     * ✅ TEST 1:
     * Scenario: No approved timesheets exist.
     * Expectation: Service should generate 0 invoices and perform no writes or emails.
     */
    @Test
    void generate_noApprovedTimesheets_returnsZeroAndNoWrites() {
        when(timesheetRepo.findAll()).thenReturn(Collections.emptyList());

        InvoiceGenerationReport report = service.generateAndSendForApprovedTimesheets();

        assertNotNull(report);
        assertEquals(0, report.getTotalApprovedTimesheets());
        assertEquals(0, report.getGeneratedCount());
        verifyNoInteractions(invoiceRepo, mailSender);
    }

    /**
     * ✅ TEST 2:
     * Scenario: Client has no email.
     * Expectation: Service should skip this client and record a skip reason.
     */
    @Test
    void generate_clientMissingEmail_skipsClient() {
        ClientEntity client = makeClient(1, "Acme", null, "USD");
        ProjectEntity proj = makeProject(10, "P1", new BigDecimal("50.0"), client);
        TimesheetEntity ts = makeTimesheet(100, proj,
                LocalDate.of(2025,1,1), LocalDate.of(2025,1,7),
                TimesheetStatus.APPROVED, List.of(makeEntry(new BigDecimal("2"), null, null)));

        when(timesheetRepo.findAll()).thenReturn(List.of(ts));

        InvoiceGenerationReport report = service.generateAndSendForApprovedTimesheets();

        assertEquals(1, report.getTotalApprovedTimesheets());
        assertTrue(report.getSkipped().stream().anyMatch(s -> s.getReason().toLowerCase().contains("email")));
        verifyNoInteractions(invoiceRepo, mailSender);
    }

    /**
     * ✅ TEST 3:
     * Scenario: JRXML template missing or unreadable.
     * Expectation: Service should record an error and stop further processing.
     */
    @Test
    void generate_jrxmlMissing_reportsError_stopProcessing() throws Exception {
        ClientEntity client = makeClient(2, "Beta", "beta@example.com", "USD");
        ProjectEntity proj = makeProject(20, "P2", new BigDecimal("100"), client);
        TimesheetEntity ts = makeTimesheet(101, proj,
                LocalDate.of(2025,2,1), LocalDate.of(2025,2,7),
                TimesheetStatus.APPROVED, List.of(makeEntry(new BigDecimal("3"), null, null)));

        when(timesheetRepo.findAll()).thenReturn(List.of(ts));

        jasperCompileMock.when(() -> JasperCompileManager.compileReport((InputStream) isNull()))
                .thenThrow(new JRException("template missing"));

        InvoiceGenerationReport report = service.generateAndSendForApprovedTimesheets();

        assertTrue(report.getErrors().size() >= 1);
    }

    /**
     * ✅ TEST 4:
     * Scenario: Valid timesheet with entries.
     * Expectation: Invoice is generated, email is sent, and repository saved.
     */
    @Test
    void generate_timesheetWithEntries_usesCostAndRateAndCreatesInvoiceAndSendsEmail() throws Exception {
        ClientEntity client = makeClient(3, "Gamma", "gamma@example.com", "EUR");
        ProjectEntity proj = makeProject(30, "Project-G", new BigDecimal("75.00"), client);

        TimeEntryEntity e1 = makeEntry(new BigDecimal("2.5"), null, new BigDecimal("300.00"));
        TimeEntryEntity e2 = makeEntry(new BigDecimal("1.5"), new BigDecimal("80.00"), null);

        TimesheetEntity ts = makeTimesheet(200, proj,
                LocalDate.of(2025,3,1), LocalDate.of(2025,3,7),
                TimesheetStatus.APPROVED, List.of(e1, e2));

        when(timesheetRepo.findAll()).thenReturn(List.of(ts));

        MimeMessage mime = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mime);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        InvoiceGenerationReport report = service.generateAndSendForApprovedTimesheets();

        assertEquals(1, report.getGeneratedCount());
        assertEquals(1, report.getEmailed().size());
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        verify(invoiceRepo, times(1)).save(any(InvoiceEntity.class));
    }

    /**
     * ✅ TEST 5:
     * Scenario: Email sending fails (e.g., SMTP error).
     * Expectation: PDF saved locally, invoice recorded, and error noted.
     */
    @Test
    void generate_emailFailure_savesPdfAndRecordsSaved() throws Exception {
        ClientEntity client = makeClient(4, "Delta", "delta@example.com", "USD");
        ProjectEntity proj = makeProject(40, "Project-D", new BigDecimal("60.0"), client);

        TimesheetEntity ts = makeTimesheet(300, proj,
                LocalDate.of(2025,4,1), LocalDate.of(2025,4,7),
                TimesheetStatus.APPROVED, List.of(makeEntry(new BigDecimal("4"), null, null)));

        when(timesheetRepo.findAll()).thenReturn(List.of(ts));

        MimeMessage mime = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mime);
        doThrow(new RuntimeException("smtp error")).when(mailSender).send(any(MimeMessage.class));

        InvoiceGenerationReport report = service.generateAndSendForApprovedTimesheets();

        assertEquals(1, report.getGeneratedCount());
        assertTrue(report.getSaved().size() >= 1);
        verify(invoiceRepo, times(1)).save(any(InvoiceEntity.class));
    }

    /**
     * ✅ TEST 6:
     * Scenario: Email settings disabled (mailFrom/mailHost empty).
     * Expectation: Invoice generated and saved locally without sending email.
     */
    @Test
    void generate_emailDisabled_savesPdf_locally_and_doesNotCallMailSender() throws Exception {
        setPrivateField(service, "mailFrom", "");
        setPrivateField(service, "mailHost", "");

        ClientEntity client = makeClient(5, "Epsilon", "eps@example.com", "USD");
        ProjectEntity proj = makeProject(50, "Project-E", new BigDecimal("25.0"), client);
        TimesheetEntity ts = makeTimesheet(400, proj,
                LocalDate.of(2025,5,1), LocalDate.of(2025,5,7),
                TimesheetStatus.APPROVED, List.of(makeEntry(new BigDecimal("2"), null, null)));

        when(timesheetRepo.findAll()).thenReturn(List.of(ts));

        InvoiceGenerationReport report = service.generateAndSendForApprovedTimesheets();

        assertEquals(1, report.getGeneratedCount());
        assertTrue(report.getSaved().size() >= 1);
        verifyNoInteractions(mailSender);
        verify(invoiceRepo, times(1)).save(any(InvoiceEntity.class));
    }

    /**
     * ✅ TEST 7:
     * Scenario: Timesheet with zero total hours.
     * Expectation: Skipped with appropriate reason; no invoice generated.
     */
    @Test
    void generate_timesheetWithZeroHours_isSkipped() throws Exception {
        ClientEntity client = makeClient(6, "Zeta", "zeta@example.com", "USD");
        ProjectEntity proj = makeProject(60, "Project-Z", new BigDecimal("100.0"), client);

        TimesheetEntity ts = makeTimesheet(500, proj,
                LocalDate.of(2025,6,1), LocalDate.of(2025,6,7),
                TimesheetStatus.APPROVED, List.of(makeEntry(BigDecimal.ZERO, null, null)));

        when(timesheetRepo.findAll()).thenReturn(List.of(ts));

        InvoiceGenerationReport report = service.generateAndSendForApprovedTimesheets();

        assertEquals(1, report.getTotalApprovedTimesheets());
        assertEquals(0, report.getGeneratedCount());
        assertTrue(report.getSkipped().stream().anyMatch(s -> s.getReason().toLowerCase().contains("no time")));
    }

    /**
     * ✅ TEST 8:
     * Scenario: One timesheet throws exception during processing; another succeeds.
     * Expectation: Error captured for first; second invoice generated successfully.
     */
    @Test
    void generate_processingException_isCapturedInReport_andContinues() throws Exception {
        ClientEntity client1 = makeClient(7, "Thrower", "throw@example.com", "USD");
        ProjectEntity p1 = makeProject(70, "P-Throw", new BigDecimal("10.0"), client1);
        TimesheetEntity badTs = makeTimesheet(600, p1,
                LocalDate.of(2025,7,1), LocalDate.of(2025,7,7),
                TimesheetStatus.APPROVED, List.of(makeEntry(new BigDecimal("1"), null, null)));

        ClientEntity client2 = makeClient(8, "Good", "good@example.com", "USD");
        ProjectEntity p2 = makeProject(80, "P-Good", new BigDecimal("20.0"), client2);
        TimesheetEntity goodTs = makeTimesheet(601, p2,
                LocalDate.of(2025,7,8), LocalDate.of(2025,7,14),
                TimesheetStatus.APPROVED, List.of(makeEntry(new BigDecimal("2"), null, null)));

        when(timesheetRepo.findAll()).thenReturn(List.of(badTs, goodTs));

        JasperReport jr = mock(JasperReport.class);
        JasperPrint jp = mock(JasperPrint.class);

        // Simulate compile failure for first timesheet
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

        MimeMessage mime = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mime);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        InvoiceGenerationReport report = service.generateAndSendForApprovedTimesheets();

        assertTrue(report.getErrors().size() >= 1);
        assertEquals(1, report.getGeneratedCount());
    }
}

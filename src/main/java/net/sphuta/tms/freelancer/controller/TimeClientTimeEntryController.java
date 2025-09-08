//package net.sphuta.tms.freelancer.controller;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import net.sphuta.tms.freelancer.dto.TmsUninvoicedResponse;
//import net.sphuta.tms.freelancer.service.impl.TmsTimeEntryService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDate;
//
//import static net.sphuta.tms.freelancer.constants.TmsMessages.*;
//
///**
// * ==========================================================
// * TimeClientTimeEntryController
// * ==========================================================
// *
// * REST controller exposing endpoints related to time entries that a *client*
// * can query. Currently provides an endpoint to fetch approved, uninvoiced
// * time entries for a given client within a date range.
// *
// * Design notes:
// * - Keeps controller thin; delegates business logic to {@link TmsTimeEntryService}.
// * - Uses Swagger annotations for API documentation.
// * - Uses SLF4J for structured logging at appropriate levels.
// */
//@Slf4j
//@RestController
//@RequiredArgsConstructor
//@RequestMapping(TIME_ENTRY_BASE_PATH)
//@Tag(name = "Time Entries")
//public class TimeClientTimeEntryController {
//
//    /**
//     * Service layer dependency handling the time-entry domain logic.
//     * Field injection is preserved as provided in the original codebase.
//     */
//    @Autowired
//    private TmsTimeEntryService service;
//
//    /**
//     * Fetch approved, uninvoiced time entries for a client within a given date range.
//     *
//     * <p>Behavior:</p>
//     * <ul>
//     *   <li>Accepts a client identifier and inclusive date range.</li>
//     *   <li>Logs inputs and warns if the date range seems inverted (from &gt; to).</li>
//     *   <li>Delegates to service layer and returns HTTP 200 with the response body.</li>
//     * </ul>
//     *
//     * <p>Observability:</p>
//     * <ul>
//     *   <li>DEBUG logs on entry with input parameters.</li>
//     *   <li>WARN log for suspicious ranges.</li>
//     *   <li>TRACE/DEBUG logs for execution duration and result size (if available).</li>
//     * </ul>
//     */
//    @Operation(summary = "Fetch approved, uninvoiced time entries for a client in a date range")
//    @GetMapping(TIME_ENTRY_UNINVOICED_PATH)
//    public ResponseEntity<TmsUninvoicedResponse> uninvoiced(
//            @RequestParam Integer clientId,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
//            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to)
//    {
//
//        // ----------- entry logging -----------
//        log.debug(LOG_TIME_ENTRY_FETCH_REQUEST, clientId, from, to);
//        long _startNs = System.nanoTime(); // start timing for basic performance insight
//
//        // ----------- input sanity checks (non-fatal) -----------
//        if (from != null && to != null && from.isAfter(to)) {
//            log.warn(LOG_TIME_ENTRY_SUSPICIOUS_RANGE, from, to, clientId);
//        }
//
//        // ----------- service delegation -----------
//        TmsUninvoicedResponse body = service.findUninvoiced(clientId, from, to);
//
//        // ----------- response logging (exit) -----------
//        long _tookMs = (System.nanoTime() - _startNs) / 1_000_000L;
//        try {
//            Integer size = (body != null && body.entries() != null) ? body.entries().size() : null;
//            log.debug(LOG_TIME_ENTRY_FETCH_SUCCESS_WITH_COUNT, clientId, _tookMs, size);
//        } catch (Exception ignore) {
//            log.debug(LOG_TIME_ENTRY_FETCH_SUCCESS_NO_COUNT, clientId, _tookMs);
//        }
//
//        return ResponseEntity.ok(body);
//    }
//}

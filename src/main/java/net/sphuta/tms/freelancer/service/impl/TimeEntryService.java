package net.sphuta.tms.freelancer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.TmsDto;
import net.sphuta.tms.freelancer.entity.TimeEntry;
import net.sphuta.tms.freelancer.repository.TimeEntryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * {@code TimeEntryService} – Business logic for handling {@link TimeEntry} operations.
 *
 * <p>This service provides functionality to query time entries that are not yet
 * invoiced for a given client within a specified date range.</p>
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Encapsulate repository calls related to time entry queries.</li>
 *   <li>Transform {@link TimeEntry} entities into lightweight DTOs
 *       ({@link TmsDto.TimeEntrySummary}) for API responses.</li>
 *   <li>Provide audit logging for traceability.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TimeEntryService {

    /** Repository for CRUD operations and custom queries on {@link TimeEntry} entities. */
    private final TimeEntryRepository repo;

    /**
     * Finds all uninvoiced {@link TimeEntry} records for a given client
     * within a specified date range.
     *
     * <p>Steps performed:</p>
     * <ol>
     *   <li>Log the query parameters (clientId, date range).</li>
     *   <li>Fetch uninvoiced entries via repository custom query.</li>
     *   <li>Transform entities into {@link TmsDto.TimeEntrySummary} DTOs
     *       for lightweight response payloads.</li>
     *   <li>Wrap results in a {@link TmsDto.UninvoicedResponse} object.</li>
     *   <li>Log the result size for auditing.</li>
     * </ol>
     *
     * @param clientId the client identifier
     * @param from start date (inclusive) for filtering time entries
     * @param to end date (inclusive) for filtering time entries
     * @return DTO response containing a list of uninvoiced time entries
     */
    public TmsDto.UninvoicedResponse findUninvoiced(UUID clientId, LocalDate from, LocalDate to) {
        // Debug log for input parameters (useful in API request tracing)
        log.debug("Query uninvoiced entries clientId={}, from={}, to={}", clientId, from, to);

        // Fetch uninvoiced entries from repository
        List<TimeEntry> list = repo.findUninvoiced(clientId, from, to);

        // Transform entities into summary DTOs for API-friendly response
        var items = list.stream()
                .map(t -> TmsDto.TimeEntrySummary.builder()
                        .id(t.getId())
                        .clientId(t.getClientId())
                        .entryDate(t.getEntryDate())
                        .hours(t.getHours())
                        .status(t.getStatus().name())
                        .build())
                .toList();

        // Info log for result size (business-level audit log)
        log.info("Found {} uninvoiced entries", items.size());

        // Wrap in UninvoicedResponse DTO and return
        return TmsDto.UninvoicedResponse.builder()
                .clientId(clientId)
                .from(from)
                .to(to)
                .entries(items)
                .build();
    }
}

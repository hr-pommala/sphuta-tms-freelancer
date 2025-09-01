package net.sphuta.tms.freelancer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.TmsUninvoicedResponse;
import net.sphuta.tms.freelancer.entity.TimeEntryEntity;
import net.sphuta.tms.freelancer.repository.TmsTimeEntryRepository;
import net.sphuta.tms.freelancer.util.TmsTimeEntryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * ==========================================================
 * TmsTimeEntryService
 * ==========================================================
 *
 * Service layer for handling **time entry operations**.
 *
 * Responsibilities:
 * - Fetches uninvoiced and APPROVED time entries for a given client and date range.
 * - Maps persistence entities to summary DTOs for API responses.
 *
 * Business rules:
 * - Only entries with APPROVED status and no invoiceId are considered uninvoiced.
 *
 * Logging:
 * - DEBUG → query inputs (clientId, date range).
 * - INFO  → result counts for monitoring/reporting.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TmsTimeEntryService {

    /** Repository for accessing time entry entities. */
    @Autowired
    private TmsTimeEntryRepository repo;

    /**
     * Finds all approved, uninvoiced time entries for a client within a date range.
     *
     * Steps:
     * 1) Fetch matching entries from repository.
     * 2) Map entities → DTO summaries.
     * 3) Wrap results in {@link TmsUninvoicedResponse}.
     *
     * @param clientId the client identifier
     * @param from     start date (inclusive)
     * @param to       end date (inclusive)
     * @return response containing list of uninvoiced entries
     */
    public TmsUninvoicedResponse findUninvoiced(Integer clientId, LocalDate from, LocalDate to) {
        // ---- entry log ----
        log.debug("Query uninvoiced entries clientId={}, from={}, to={}", clientId, from, to);

        // ---- repository fetch ----
        List<TimeEntryEntity> list = repo.findUninvoiced(clientId, from, to);

        // ---- map to summary DTOs ----
        var items = list.stream()
                .map(TmsTimeEntryMapper::toSummary)
                .toList();

        // ---- result log ----
        log.info("Found {} uninvoiced entries for clientId={} in range {} to {}", items.size(), clientId, from, to);

        // Build the response
        var response = TmsUninvoicedResponse.builder()
                .clientId(clientId)
                .from(from)
                .to(to)
                .entries(items)
                .build();

// Log the constructed response (for debug / audit purposes)
        log.debug("Built TmsUninvoicedResponse: {}", response);

// Return the response
        return response;


    }
}

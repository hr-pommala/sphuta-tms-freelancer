package net.sphuta.tms.freelancer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.TmsDto;
import net.sphuta.tms.freelancer.entity.Estimate;
import net.sphuta.tms.freelancer.repository.EstimateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {@code EstimateService} – Business logic layer for managing {@link Estimate} entities.
 *
 * <p>This service acts as the intermediary between the controller layer and the persistence layer
 * ({@link EstimateRepository}). It contains domain-specific rules and ensures proper persistence
 * of estimate-related data.</p>
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Convert DTO requests ({@link TmsDto.EstimateCreateRequest}) into {@link Estimate} entities.</li>
 *   <li>Persist new estimates in the database via {@link EstimateRepository}.</li>
 *   <li>Log key actions for traceability (e.g., estimate creation).</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EstimateService {

    /** Repository for interacting with the {@link Estimate} persistence layer. */
    private final EstimateRepository repo;

    /**
     * Creates and persists a new {@link Estimate} entity based on the provided request DTO.
     *
     * <p>Steps performed:</p>
     * <ol>
     *   <li>Convert {@link TmsDto.EstimateCreateRequest#items()} into a list of {@link Estimate.Item}.</li>
     *   <li>Assemble a new {@link Estimate} entity with client, currency, validity, and notes.</li>
     *   <li>Persist the entity using {@link EstimateRepository#save(Object)}.</li>
     *   <li>Log the operation for monitoring and auditing purposes.</li>
     * </ol>
     *
     * <p>Transactional Behavior:</p>
     * This method is annotated with {@link Transactional} to ensure that all database operations
     * either complete successfully or roll back together in case of failure.
     *
     * @param req DTO containing estimate creation data such as client, items, and validity.
     * @return The persisted {@link Estimate} entity with generated ID and timestamps.
     */
    @Transactional
    public Estimate create(TmsDto.EstimateCreateRequest req) {
        // Convert incoming DTO items into entity items
        List<Estimate.Item> items = req.items().stream()
                .map(i -> Estimate.Item.builder()
                        .description(i.description())
                        .quantity(i.quantity())
                        .unitPrice(i.unitPrice())
                        .build())
                .toList();

        // Build the Estimate entity
        Estimate est = Estimate.builder()
                .clientId(req.clientId())
                .issueDate(req.issueDate())
                .validUntil(req.validUntil())
                .currencyCode(req.currencyCode())
                .notes(req.notes())
                .items(items)
                .build();

        // Persist entity in the database
        est = repo.save(est);

        // Log creation for audit and monitoring purposes
        log.info("Created estimate {} for client {}", est.getId(), est.getClientId());

        return est;
    }
}

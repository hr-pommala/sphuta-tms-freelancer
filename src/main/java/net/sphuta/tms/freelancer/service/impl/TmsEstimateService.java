package net.sphuta.tms.freelancer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.TmsEstimateDto;
import net.sphuta.tms.freelancer.entity.EstimateEntity;
import net.sphuta.tms.freelancer.exception.TmsException;
import net.sphuta.tms.freelancer.repository.TmsClientRepository;
import net.sphuta.tms.freelancer.repository.TmsEstimateRepository;
import net.sphuta.tms.freelancer.util.TmsEstimateMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ==========================================================
 * TmsEstimateService
 * ==========================================================
 *
 * Service layer for managing Estimates.
 *
 * Responsibilities:
 * - Validates existence of the target client before estimate creation.
 * - Maps between DTOs ({@link TmsEstimateDto}, {@link TmsEstimateDto})
 *   and persistence entities ({@link EstimateEntity}).
 * - Persists estimates through {@link TmsEstimateRepository}.
 * - Throws {@link TmsException} with appropriate HTTP status for error scenarios.
 *
 * Logging:
 * - DEBUG → input payloads and validation decisions.
 * - INFO  → successful persistence of new estimates.
 * - WARN  → validation failures or business rule violations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TmsEstimateService {

    /** Repository for managing estimates. */
    @Autowired
    private TmsEstimateRepository estimates;

    /** Repository for validating existence of client (foreign key). */
    @Autowired
    private TmsClientRepository clients;

    /**
     * Creates a new estimate for a given client.
     *
     * Business rules:
     * - Client must exist; otherwise a {@link TmsException}(404) is thrown.
     * - Estimate items must be provided (validated at DTO level).
     *
     * Transactional: ensures atomic persistence of the estimate entity.
     *
     * @param req DTO containing estimate creation request
     * @return response DTO of the saved estimate
     * @throws TmsException if client does not exist
     */
    @Transactional
    public TmsEstimateDto create(TmsEstimateDto req) {
        Integer clientId = req.clientId();
        log.debug("Attempting to create estimate for clientId={}", clientId);

        // Validate client existence
        if (clientId == null || !clients.existsById(clientId)) {
            log.warn("Client {} not found while creating estimate", clientId);
            throw new TmsException(HttpStatus.NOT_FOUND, "Client not found");
        }

        // Map DTO → Entity
        EstimateEntity entity = TmsEstimateMapper.toNewEntity(req);
        // If you want to store an actual relation, you could do:
        // entity.setClient(clients.getReferenceById(clientId));

        // Save entity
        EstimateEntity saved = estimates.save(entity);
        log.info("Created estimate {} for client {}", saved.getId(), clientId);

        // Map Entity → DTO and return
        return TmsEstimateMapper.toResponse(saved);
    }
}

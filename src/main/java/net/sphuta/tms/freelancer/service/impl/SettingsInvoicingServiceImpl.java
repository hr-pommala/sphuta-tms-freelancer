package net.sphuta.tms.freelancer.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.constants.TmsMessages;
import net.sphuta.tms.freelancer.dto.SettingsInvoicingDTO;
import net.sphuta.tms.freelancer.entity.SettingsInvoicing;
import net.sphuta.tms.freelancer.exception.NotFoundException;
import net.sphuta.tms.freelancer.repository.SettingsInvoicingRepository;
import net.sphuta.tms.freelancer.service.SettingsInvoicingService;
import net.sphuta.tms.freelancer.util.ResponseMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <h2>InvoicingSettingsServiceImpl</h2>
 * <p>
 * Service implementation class responsible for managing invoicing settings.
 * Provides full CRUD and partial update operations for invoicing settings
 * and maps between {@link SettingsInvoicing} entity and {@link SettingsInvoicingDTO}.
 * </p>
 *
 * <p>
 * This class uses SLF4J for logging and Spring's {@link Transactional} for
 * transactional operations. All methods log key actions at INFO or DEBUG levels.
 * </p>
 */
@Service
@Slf4j
@Transactional
public class SettingsInvoicingServiceImpl implements SettingsInvoicingService {

    /** Repository for CRUD operations on InvoicingSettings entity */
    @Autowired
    private SettingsInvoicingRepository repository;

    /** Mapper for converting between entity and DTO */
    @Autowired
    private ResponseMapper mapper;

    /**
     * Fetch all invoicing settings from the database.
     *
     * @return List of {@link SettingsInvoicingDTO}
     */
    @Override
    public List<SettingsInvoicingDTO> getAllSettings() {
        log.info("Fetching all invoicing settings");
        return repository.findAll()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Fetch invoicing settings by user ID.
     *
     * @param userId Unique identifier of the user
     * @return Optional containing {@link SettingsInvoicingDTO} if found
     * @throws NotFoundException if no settings exist for the user
     */
    @Override
    public Optional<SettingsInvoicingDTO> getSettingsByUserId(int userId) {
        log.info("Fetching invoicing settings for userId={}", userId);

        if (!repository.existsById(userId)) {
            log.warn("Settings not found for userId={}", userId);
            throw new NotFoundException(TmsMessages.SETTINGS_NOT_FOUND + userId);
        }

        return repository.findById(userId).map(mapper::toDTO);
    }

    /**
     * Create new invoicing settings.
     *
     * @param dto DTO containing invoicing settings data
     * @return Created {@link SettingsInvoicingDTO}
     */
    @Override
    public SettingsInvoicingDTO createSettings(SettingsInvoicingDTO dto) {
        log.info("Creating new invoicing settings for userId={}", dto.userId());
        SettingsInvoicing entity = mapper.toEntity(dto);
        SettingsInvoicing saved = repository.save(entity);
        log.debug("Invoicing settings created: {}", saved);
        return mapper.toDTO(saved);
    }

    /**
     * Update existing invoicing settings completely (PUT).
     * Replaces all fields of the existing entity with values from the DTO.
     *
     * @param userId Unique identifier of the user
     * @param dto    DTO containing updated invoicing settings
     * @return Updated {@link SettingsInvoicingDTO}
     * @throws NotFoundException if settings do not exist for the user
     */
    @Override
    public SettingsInvoicingDTO updateSettings(int userId, SettingsInvoicingDTO dto) {
        log.info("Updating invoicing settings for userId={}", userId);
        SettingsInvoicing existing = repository.findById(userId)
                .orElseThrow(() -> new NotFoundException(TmsMessages.SETTINGS_NOT_FOUND + userId));

        // Replace all fields
        existing.setCurrency(dto.currency());
        existing.setTaxId(dto.taxId());
        existing.setDefaultTaxRate(dto.defaultTaxRate());
        existing.setInvoiceNumberFormat(dto.invoiceNumberFormat());
        existing.setPaymentTermsDays(dto.paymentTermsDays());
        existing.setLateFeePercent(dto.lateFeePercent());
        existing.setTemplateId(dto.templateId());
        existing.setLogoFileId(dto.logoFileId());
        existing.setUpdatedAt(java.time.LocalDateTime.now());

        SettingsInvoicing updated = repository.save(existing);
        log.debug("Invoicing settings updated: {}", updated);
        return mapper.toDTO(updated);
    }

    /**
     * Delete invoicing settings by user ID.
     *
     * @param userId Unique identifier of the user
     * @throws NotFoundException if settings do not exist for the user
     */
    @Override
    public void deleteSettings(int userId) {
        log.info("Deleting invoicing settings for userId={}", userId);
        if (!repository.existsById(userId)) {
            log.warn("Settings not found for userId={}", userId);
            throw new NotFoundException(TmsMessages.SETTINGS_NOT_FOUND + userId);
        }
        repository.deleteById(userId);
        log.info("Invoicing settings deleted for userId={}", userId);
    }
}

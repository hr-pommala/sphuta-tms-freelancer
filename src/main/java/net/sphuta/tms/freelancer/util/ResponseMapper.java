package net.sphuta.tms.freelancer.util;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.SettingsInvoicingDTO;
import net.sphuta.tms.freelancer.entity.SettingsInvoicing;
import org.springframework.stereotype.Component;

/**
 * <h2>ResponseMapper</h2>
 * <p>
 * Component responsible for mapping between {@link SettingsInvoicing} entity and
 * {@link SettingsInvoicingDTO}. Provides utility methods to convert entity to DTO
 * and DTO to entity, used throughout the service layer.
 * </p>
 *
 * <p>
 * Logging is performed using SLF4J to track mapping operations and warn when null
 * inputs are encountered.
 * </p>
 */
@Component
@Slf4j
public class ResponseMapper {

    /**
     * Converts an {@link SettingsInvoicing} entity to {@link SettingsInvoicingDTO}.
     *
     * @param entity the InvoicingSettings entity to convert
     * @return corresponding {@link SettingsInvoicingDTO}, or null if input is null
     */
    public SettingsInvoicingDTO toDTO(SettingsInvoicing entity) {
        // Warn if null entity is passed
        if (entity == null) {
            log.warn("Attempted to convert null entity to DTO");
            return null;
        }

        // Map entity fields to DTO
        SettingsInvoicingDTO dto = new SettingsInvoicingDTO(
                entity.getUserId(),
                entity.getCurrency(),
                entity.getTaxId(),
                entity.getDefaultTaxRate(),
                entity.getInvoiceNumberFormat(),
                entity.getPaymentTermsDays(),
                entity.getLateFeePercent(),
                entity.getTemplateId(),
                entity.getLogoFileId(),
                entity.getUpdatedAt()
        );

        log.debug("Converted entity to DTO: {}", dto);
        return dto;
    }

    /**
     * Converts an {@link SettingsInvoicingDTO} to {@link SettingsInvoicing} entity.
     *
     * @param dto the {@link SettingsInvoicingDTO} to convert
     * @return corresponding {@link SettingsInvoicing} entity, or null if input is null
     */
    public SettingsInvoicing toEntity(SettingsInvoicingDTO dto) {
        // Warn if null DTO is passed
        if (dto == null) {
            log.warn("Attempted to convert null DTO to entity");
            return null;
        }

        // Map DTO fields to entity using builder pattern
        SettingsInvoicing entity = SettingsInvoicing.builder()
                .userId(dto.userId())
                .currency(dto.currency())
                .taxId(dto.taxId())
                .defaultTaxRate(dto.defaultTaxRate())
                .invoiceNumberFormat(dto.invoiceNumberFormat())
                .paymentTermsDays(dto.paymentTermsDays())
                .lateFeePercent(dto.lateFeePercent())
                .templateId(dto.templateId())
                .logoFileId(dto.logoFileId())
                .build();

        log.debug("Converted DTO to entity: {}", entity);
        return entity;
    }
}

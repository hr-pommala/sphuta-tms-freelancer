package net.sphuta.tms.freelancer.util;

import net.sphuta.tms.freelancer.dto.TmsEstimateDto;
import net.sphuta.tms.freelancer.dto.TmsEstimateItemDto;
import net.sphuta.tms.freelancer.entity.EstimateEntity;

import java.util.List;

/**
 * ==========================================================
 * TmsEstimateMapper
 * ==========================================================
 *
 * Utility for converting between:
 * - {@link TmsEstimateDto} (API create payload),
 * - {@link EstimateEntity} (JPA entity),
 * - {@link TmsEstimateDto} (API read model),
 * - {@link TmsEstimateItemDto}/{@link TmsEstimateItemDto} (line items).
 *
 * Design:
 * - Pure static methods; no side effects or logging.
 * - Keeps mapping concerns outside services/controllers.
 */
public class TmsEstimateMapper {

    /**
     * Map a create request DTO into a NEW {@link EstimateEntity}.
     * <p>
     * Notes:
     * - Converts request line items to embeddable {@code TmsEstimateEntity.Item}.
     * - The returned entity is not persisted; services handle save().
     *
     * @param req the incoming create request
     * @return a new entity ready for persistence
     */
    public static EstimateEntity toNewEntity(TmsEstimateDto req) {
        // Map request items → entity embeddables
        List<EstimateEntity.Item> items = req.items().stream()
                .map(i -> EstimateEntity.Item.builder()
                        .description(i.description())
                        .quantity(i.quantity())
                        .unitPrice(i.unitPrice())
                        .build())
                .toList();

        // Build entity
        return EstimateEntity.builder()
                .clientId(req.clientId())
                .issueDate(req.issueDate())
                .validUntil(req.validUntil())
                .currencyCode(req.currencyCode())
                .notes(req.notes())
                .items(items)
                .build();
    }

    /**
     * Map a persisted {@link EstimateEntity} into a {@link TmsEstimateDto}.
     * <p>
     * Notes:
     * - Converts entity items to response DTO items.
     * - Preserves system timestamps and identifiers.
     *
     * @param e the entity loaded from persistence
     * @return the API response DTO
     */
    public static TmsEstimateDto toResponse(EstimateEntity e) {
        // Map entity items → response DTO items
        List<TmsEstimateItemDto> items = e.getItems().stream()
                .map(i -> TmsEstimateItemDto.builder()
                        .description(i.getDescription())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .build())
                .toList();

        // Build response DTO
        return TmsEstimateDto.builder()
                .id(e.getId())
                .clientId(e.getClientId())
                .issueDate(e.getIssueDate())
                .validUntil(e.getValidUntil())
                .currencyCode(e.getCurrencyCode())
                .notes(e.getNotes())
                .items(items)
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}

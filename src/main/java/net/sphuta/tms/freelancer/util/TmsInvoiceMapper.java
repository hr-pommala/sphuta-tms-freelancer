package net.sphuta.tms.freelancer.util;

import net.sphuta.tms.freelancer.dto.TmsInvoiceDto;
import net.sphuta.tms.freelancer.entity.InvoiceEntity;

/**
 * ==========================================================
 * TmsInvoiceMapper
 * ==========================================================
 *
 * Utility class for converting between:
 * - {@link TmsInvoiceDto} (API create payload),
 * - {@link InvoiceEntity} (JPA entity),
 * - {@link TmsInvoiceDto} (API read model).
 *
 * Design:
 * - Pure static methods (stateless).
 * - Encapsulates mapping logic to keep controllers/services clean.
 */
public class TmsInvoiceMapper {

    /**
     * Build a NEW {@link InvoiceEntity} in {@code DRAFT} status from request.
     * <p>
     * Notes:
     * - Sets initial status to {@link InvoiceEntity.Status#DRAFT}.
     * - Ignores any request fields that should not be client-controlled
     *   (e.g., {@code id}, {@code createdAt}, {@code updatedAt}).
     *
     * @param req the incoming invoice create request
     * @return a new invoice entity ready for persistence
     */
    public static InvoiceEntity toNewDraft(TmsInvoiceDto req) {
        return InvoiceEntity.builder()
                .clientId(req.clientId())
                .issueDate(req.issueDate())
                .dueDate(req.dueDate())
                .currencyCode(req.currencyCode())
                .status(InvoiceEntity.Status.DRAFT) // ✅ always start as DRAFT
                .notes(req.notes())
                .build();
    }

    /**
     * Map a persisted {@link InvoiceEntity} into a {@link TmsInvoiceDto}.
     * <p>
     * Notes:
     * - Converts enum {@code Status} into string for the API response.
     * - Preserves system-managed fields like {@code createdAt}, {@code updatedAt}.
     *
     * @param e the entity loaded from persistence
     * @return the API response DTO
     */
    public static TmsInvoiceDto toResponse(InvoiceEntity e) {
        return TmsInvoiceDto.builder()
                .id(e.getId())
                .clientId(e.getClientId())
                .issueDate(e.getIssueDate())
                .dueDate(e.getDueDate())
                .currencyCode(e.getCurrencyCode())
                .status(e.getStatus().name()) // ✅ expose enum as string
                .notes(e.getNotes())
                .createdDt(e.getCreatedAt())
                .updatedDt(e.getUpdatedAt())
                .build();
    }
}

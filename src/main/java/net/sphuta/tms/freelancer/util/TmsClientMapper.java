package net.sphuta.tms.freelancer.util;

import net.sphuta.tms.freelancer.dto.TmsClientDto;
import net.sphuta.tms.freelancer.entity.ClientEntity;

/**
 * ==========================================================
 * TmsClientMapper
 * ==========================================================
 *
 * Utility class for converting between:
 * - {@link TmsClientDto} (incoming API payloads),
 * - {@link ClientEntity} (JPA persistence model),
 * - {@link TmsClientDto} (outgoing API response).
 *
 * Responsibilities:
 * - Ensure consistent mapping between DTOs and entities.
 * - Apply sensible defaults for new entities.
 * - Support partial updates by merging only non-null fields.
 *
 * Design notes:
 * - Pure utility methods (static).
 * - No logging inside mapper (responsibility of services).
 */
public class TmsClientMapper {

    /**
     * Builds a **new entity** from a request payload.
     * - Applies defaults for booleans and active status.
     *
     * @param r request DTO
     * @return new entity ready to be persisted
     */
    public static ClientEntity toNewEntity(TmsClientDto r) {
        ClientEntity e = new ClientEntity();
        applyCommonFields(r, e);

        // Apply defaults and flags
        e.setSendReminders(Boolean.TRUE.equals(r.sendReminders()));
        e.setChargeLateFees(Boolean.TRUE.equals(r.chargeLateFees()));
        e.setAllowInvoiceAttachments(Boolean.TRUE.equals(r.allowInvoiceAttachments()));
        e.setIsActive(r.isActive() == null || r.isActive()); // default to active

        return e;
    }

    /**
     * Merges a request into an **existing entity**.
     * - Only non-null request fields overwrite entity fields.
     * - Useful for PATCH/PUT operations.
     *
     * @param r request DTO
     * @param e existing entity to update
     */
    public static void updateEntity(TmsClientDto r, ClientEntity e) {
        applyCommonFields(r, e);

        if (r.sendReminders() != null) e.setSendReminders(r.sendReminders());
        if (r.chargeLateFees() != null) e.setChargeLateFees(r.chargeLateFees());
        if (r.allowInvoiceAttachments() != null) e.setAllowInvoiceAttachments(r.allowInvoiceAttachments());
        if (r.isActive() != null) e.setIsActive(r.isActive());
    }

    /**
     * Converts a **persistence entity** into a response DTO.
     * - Used to return API results to clients.
     *
     * @param e entity from DB
     * @return API response DTO
     */
    public static TmsClientDto toResponse(ClientEntity e) {
        return TmsClientDto.builder()
                .id(e.getId())
                .companyName(e.getCompanyName())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .email(e.getEmail())
                .mobilePhone(e.getMobilePhone())
                .businessPhone(e.getBusinessPhone())
                .addressLine1(e.getAddressLine1())
                .addressLine2(e.getAddressLine2())
                .city(e.getCity())
                .state(e.getState())
                .postalCode(e.getPostalCode())
                .countryCode(e.getCountryCode())
                .sendReminders(e.getSendReminders())
                .chargeLateFees(e.getChargeLateFees())
                .lateFeePercent(e.getLateFeePercent())
                .currencyCode(e.getCurrencyCode())
                .language(e.getLanguage())
                .allowInvoiceAttachments(e.getAllowInvoiceAttachments())
                .isActive(e.getIsActive())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    // ----------------- helper methods -----------------

    /**
     * Common field setter for both new + updated entities.
     * - Only non-null fields from request overwrite entity values.
     */
    private static void applyCommonFields(TmsClientDto r, ClientEntity e) {
        if (r.companyName()   != null) e.setCompanyName(r.companyName());
        if (r.firstName()     != null) e.setFirstName(r.firstName());
        if (r.lastName()      != null) e.setLastName(r.lastName());
        if (r.email()         != null) e.setEmail(r.email());
        if (r.mobilePhone()   != null) e.setMobilePhone(r.mobilePhone());
        if (r.businessPhone() != null) e.setBusinessPhone(r.businessPhone());
        if (r.addressLine1()  != null) e.setAddressLine1(r.addressLine1());
        if (r.addressLine2()  != null) e.setAddressLine2(r.addressLine2());
        if (r.city()          != null) e.setCity(r.city());
        if (r.state()         != null) e.setState(r.state());
        if (r.postalCode()    != null) e.setPostalCode(r.postalCode());
        if (r.countryCode()   != null) e.setCountryCode(r.countryCode());
        if (r.lateFeePercent()!= null) e.setLateFeePercent(r.lateFeePercent());
        if (r.currencyCode()  != null) e.setCurrencyCode(r.currencyCode());
        if (r.language()      != null) e.setLanguage(r.language());
    }
}

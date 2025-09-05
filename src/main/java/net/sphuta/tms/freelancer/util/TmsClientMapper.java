package net.sphuta.tms.freelancer.util;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.TmsClientDto;
import net.sphuta.tms.freelancer.entity.ClientEntity;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * ==========================================================
 * {@code TmsClientMapper}
 * ==========================================================
 *
 * <p>Utility class responsible for mapping between the domain {@link ClientEntity}
 * and the transport {@link TmsClientDto} (both directions).</p>
 *
 * <p><b>Design notes:</b></p>
 * <ul>
 *   <li>All methods are static — this is a stateless mapper.</li>
 *   <li>Includes defensive null checks and SLF4J logging for observability.</li>
 *   <li>Private constructor prevents instantiation.</li>
 * </ul>
 */
@Slf4j
public final class TmsClientMapper {

    // prevent instantiation
    private TmsClientMapper() {}

    // ------------------------------------------------------------------------
    // TO ENTITY (CREATE)
    // ------------------------------------------------------------------------

    /**
     * Create a new {@link ClientEntity} populated from {@link TmsClientDto}.
     *
     * <p>Behavior:</p>
     * <ul>
     *   <li>Applies common fields (name, contact, address).</li>
     *   <li>Sets boolean flags with sensible defaults (true/false handling).</li>
     * </ul>
     *
     * @param r DTO source (must not be null)
     * @return a new ClientEntity instance ready for persistence
     * @throws IllegalArgumentException if {@code r} is null
     */
    public static ClientEntity toNewEntity(TmsClientDto r) {
        if (r == null) {
            log.error("toNewEntity called with null TmsClientDto");
            throw new IllegalArgumentException("TmsClientDto must not be null");
        }

        log.debug("Mapping TmsClientDto -> ClientEntity (new). email={}", r.email());

        ClientEntity e = new ClientEntity();
        applyCommonFields(r, e);

        // boolean fields: use provided value when present, otherwise sensible defaults
        e.setSendReminders(Boolean.TRUE.equals(r.sendReminders()));
        e.setChargeLateFees(Boolean.TRUE.equals(r.chargeLateFees()));
        e.setAllowInvoiceAttachments(Boolean.TRUE.equals(r.allowInvoiceAttachments()));
        // default to active when not specified
        e.setIsActive(r.isActive() == null || r.isActive());

        log.info("Created ClientEntity from DTO; email={}, isActive={}", e.getEmail(), e.getIsActive());
        return e;
    }

    // ------------------------------------------------------------------------
    // TO ENTITY (UPDATE)
    // ------------------------------------------------------------------------

    /**
     * Update an existing {@link ClientEntity} using values from {@link TmsClientDto}.
     *
     * <p>Only non-null fields from the DTO are applied — this supports partial updates.</p>
     *
     * @param r DTO carrying updates (must not be null)
     * @param e target entity to update (must not be null)
     * @throws IllegalArgumentException if {@code r} or {@code e} is null
     */
    public static void updateEntity(TmsClientDto r, ClientEntity e) {
        if (r == null) {
            log.error("updateEntity called with null TmsClientDto");
            throw new IllegalArgumentException("TmsClientDto must not be null");
        }
        if (e == null) {
            log.error("updateEntity called with null ClientEntity");
            throw new IllegalArgumentException("ClientEntity must not be null");
        }

        log.debug("Updating ClientEntity id={} from DTO", e.getId());

        applyCommonFields(r, e);

        if (r.sendReminders() != null) e.setSendReminders(r.sendReminders());
        if (r.chargeLateFees() != null) e.setChargeLateFees(r.chargeLateFees());
        if (r.allowInvoiceAttachments() != null) e.setAllowInvoiceAttachments(r.allowInvoiceAttachments());
        if (r.isActive() != null) e.setIsActive(r.isActive());

        log.info("Updated ClientEntity id={} (email={})", e.getId(), e.getEmail());
    }

    // ------------------------------------------------------------------------
    // TO DTO (RESPONSE)
    // ------------------------------------------------------------------------

    /**
     * Map {@link ClientEntity} -> {@link TmsClientDto} for API responses.
     *
     * @param e source entity (must not be null)
     * @return DTO populated from entity
     * @throws IllegalArgumentException if {@code e} is null
     */
    public static TmsClientDto toResponse(ClientEntity e) {
        if (e == null) {
            log.error("toResponse called with null ClientEntity");
            throw new IllegalArgumentException("ClientEntity must not be null");
        }

        log.debug("Mapping ClientEntity -> TmsClientDto for id={}", e.getId());

        TmsClientDto dto = TmsClientDto.builder()
                .id(e.getId())
                .email(e.getEmail())
                .companyName(e.getCompanyName())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
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
                .createdAt(e.getCreatedAt() != null ? OffsetDateTime.ofInstant(e.getCreatedAt(), ZoneOffset.UTC) : null)
                .updatedAt(e.getUpdatedAt() != null ? OffsetDateTime.ofInstant(e.getUpdatedAt(), ZoneOffset.UTC) : null)
                .name(e.getName()) // from entity lifecycle hooks
                .build();

        log.info("Mapped ClientEntity id={} -> TmsClientDto email={}", e.getId(), e.getEmail());
        return dto;
    }

    // ------------------------------------------------------------------------
    // HELPER: common field copy
    // ------------------------------------------------------------------------

    /**
     * Applies common (non-boolean) fields from DTO -> Entity.
     *
     * <p>Only non-null DTO values are copied to allow partial updates.</p>
     *
     * @param r DTO (source)
     * @param e Entity (target)
     */
    private static void applyCommonFields(TmsClientDto r, ClientEntity e) {
        // defensive: caller already checks for nulls, but keep minimal guard
        if (r == null || e == null) {
            log.debug("applyCommonFields skipped due to null argument (r={}, e={})", r, e);
            return;
        }

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

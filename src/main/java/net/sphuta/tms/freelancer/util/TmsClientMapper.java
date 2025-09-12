package net.sphuta.tms.freelancer.util;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.TmsClientDto;
import net.sphuta.tms.freelancer.entity.ClientEntity;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

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

        // set user id (required)
        e.setUserId(r.userId());

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

        // update userId only when provided (should normally be required and immutable)
        Optional.ofNullable(r.userId()).ifPresent(e::setUserId);
        Optional.ofNullable(r.sendReminders()).ifPresent(e::setSendReminders);
        Optional.ofNullable(r.chargeLateFees()).ifPresent(e::setChargeLateFees);
        Optional.ofNullable(r.allowInvoiceAttachments()).ifPresent(e::setAllowInvoiceAttachments);
        Optional.ofNullable(r.isActive()).ifPresent(e::setIsActive);

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
                .userId(e.getUserId())
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
                .createdDt(e.getCreatedDt() != null ? OffsetDateTime.ofInstant(e.getCreatedDt(), ZoneOffset.UTC) : null)
                .updatedDt(e.getUpdatedDt() != null ? OffsetDateTime.ofInstant(e.getUpdatedDt(), ZoneOffset.UTC) : null)
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

        Optional.ofNullable(r.companyName()).ifPresent(e::setCompanyName);
        Optional.ofNullable(r.firstName()).ifPresent(e::setFirstName);
        Optional.ofNullable(r.lastName()).ifPresent(e::setLastName);
        Optional.ofNullable(r.email()).ifPresent(e::setEmail);
        Optional.ofNullable(r.mobilePhone()).ifPresent(e::setMobilePhone);
        Optional.ofNullable(r.businessPhone()).ifPresent(e::setBusinessPhone);
        Optional.ofNullable(r.addressLine1()).ifPresent(e::setAddressLine1);
        Optional.ofNullable(r.addressLine2()).ifPresent(e::setAddressLine2);
        Optional.ofNullable(r.city()).ifPresent(e::setCity);
        Optional.ofNullable(r.state()).ifPresent(e::setState);
        Optional.ofNullable(r.postalCode()).ifPresent(e::setPostalCode);
        Optional.ofNullable(r.countryCode()).ifPresent(e::setCountryCode);
        Optional.ofNullable(r.lateFeePercent()).ifPresent(e::setLateFeePercent);
        Optional.ofNullable(r.currencyCode()).ifPresent(e::setCurrencyCode);
        Optional.ofNullable(r.language()).ifPresent(e::setLanguage);
    }
}
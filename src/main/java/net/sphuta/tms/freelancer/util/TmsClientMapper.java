package net.sphuta.tms.freelancer.util;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.TmsClientDto;
import net.sphuta.tms.freelancer.entity.ClientEntity;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * Utility class responsible for mapping between ClientEntity and TmsClientDto.
 */
@Slf4j
public final class TmsClientMapper {

    private TmsClientMapper() {}

    // ------------------------------------------------------------------------
    // TO ENTITY (CREATE)
    // ------------------------------------------------------------------------
    public static ClientEntity toNewEntity(TmsClientDto r) {
        if (r == null) {
            log.error("toNewEntity called with null TmsClientDto");
            throw new IllegalArgumentException("TmsClientDto must not be null");
        }

        log.debug("Mapping TmsClientDto -> ClientEntity (new). email={}", r.email());

        ClientEntity e = new ClientEntity();
        applyCommonFields(r, e);

        // set userId only if provided (DTO.userId is Integer and may be null)
        Optional.ofNullable(r.userId()).ifPresent(e::setUserId);

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

        // update userId only when provided
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
    public static TmsClientDto toResponse(ClientEntity e) {
        if (e == null) {
            log.error("toResponse called with null ClientEntity");
            throw new IllegalArgumentException("ClientEntity must not be null");
        }

        log.debug("Mapping ClientEntity -> TmsClientDto for id={}", e.getId());

        // ClientEntity.userId is Integer now — map directly (may be null)
        Integer userId = e.getUserId();

        TmsClientDto dto = TmsClientDto.builder()
                .id(e.getId())
                .userId(userId)
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
                .name(e.getName())
                .build();

        log.info("Mapped ClientEntity id={} -> TmsClientDto email={}", e.getId(), e.getEmail());
        return dto;
    }

    // ------------------------------------------------------------------------
    // HELPER: common field copy
    // ------------------------------------------------------------------------
    private static void applyCommonFields(TmsClientDto r, ClientEntity e) {
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

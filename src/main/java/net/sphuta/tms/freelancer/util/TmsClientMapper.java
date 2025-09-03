package net.sphuta.tms.freelancer.util;

import net.sphuta.tms.freelancer.dto.TmsClientDto;
import net.sphuta.tms.freelancer.entity.ClientEntity;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class TmsClientMapper {

    public static ClientEntity toNewEntity(TmsClientDto r) {
        ClientEntity e = new ClientEntity();
        applyCommonFields(r, e);

        e.setSendReminders(Boolean.TRUE.equals(r.sendReminders()));
        e.setChargeLateFees(Boolean.TRUE.equals(r.chargeLateFees()));
        e.setAllowInvoiceAttachments(Boolean.TRUE.equals(r.allowInvoiceAttachments()));
        e.setIsActive(r.isActive() == null || r.isActive());

        return e;
    }

    public static void updateEntity(TmsClientDto r, ClientEntity e) {
        applyCommonFields(r, e);

        if (r.sendReminders() != null) e.setSendReminders(r.sendReminders());
        if (r.chargeLateFees() != null) e.setChargeLateFees(r.chargeLateFees());
        if (r.allowInvoiceAttachments() != null) e.setAllowInvoiceAttachments(r.allowInvoiceAttachments());
        if (r.isActive() != null) e.setIsActive(r.isActive());
    }

    public static TmsClientDto toResponse(ClientEntity e) {
        return TmsClientDto.builder()
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
                .name(e.getName()) // ✅ comes from entity lifecycle
                .build();
    }

    // ----------------- helper -----------------
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

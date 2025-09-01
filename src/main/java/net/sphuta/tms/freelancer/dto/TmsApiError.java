package net.sphuta.tms.freelancer.dto;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record TmsApiError(
        String error,
        String message,
        String path,
        Integer status,
        OffsetDateTime timestamp
) {}

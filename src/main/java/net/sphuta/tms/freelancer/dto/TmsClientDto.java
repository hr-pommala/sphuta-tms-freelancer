package net.sphuta.tms.freelancer.dto;

/**
 * Slim DTO for client dropdowns or lightweight project owner display.
 */
public record TmsClientDto(

        /** Client identifier (int). */
        Integer id,

        /** Display name of the client. */
        String name
) {}

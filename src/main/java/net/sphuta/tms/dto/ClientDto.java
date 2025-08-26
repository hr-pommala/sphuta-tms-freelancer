package net.sphuta.tms.dto;

import lombok.*;

/**
 * Data Transfer Object (DTO) representing a minimal view of a Client.
 *
 * <p>This DTO is intentionally slim — only the fields needed for dropdowns or lightweight lists
 * are included. For example, in the Projects screen where the Owner dropdown shows only
 * {@code id} and {@code name}.</p>
 *
 * <p><strong>Notes:</strong></p>
 * <ul>
 *   <li>Annotated with Lombok to generate boilerplate (getters, setters, builder, etc.).</li>
 *   <li>No validation constraints here — this is a read-only projection, not input.</li>
 *   <li>Kept serializable-friendly by using only simple types (String).</li>
 * </ul>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDto {

    /** Unique identifier of the client (UUID in string form). */
    private String id;

    /** Display name of the client (company or individual). */
    private String name;

    /**
     * Override of {@code toString()} to make logs/debugging clearer without requiring full JSON.
     * Example: {@code ClientDto{id=..., name=...}}
     */
    @Override
    public String toString() {
        return "ClientDto{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}

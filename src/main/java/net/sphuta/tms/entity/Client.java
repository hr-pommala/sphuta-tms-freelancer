package net.sphuta.tms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * JPA entity representing a Client (Owner).
 *
 * <p>This is a minimal entity model because clients are currently
 * only used for the Projects "Owner" dropdown. The table stores
 * a unique identifier, a display name, and an active flag.</p>
 *
 * <p><strong>Notes:</strong></p>
 * <ul>
 *   <li>UUID is used as the primary key for portability across environments.</li>
 *   <li>{@code active} flag is a soft indicator (not a hard delete).</li>
 *   <li>Lombok annotations reduce boilerplate (getters, setters, builder, etc.).</li>
 * </ul>
 */
@Entity
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {

    /**
     * Primary key for the client.
     * <p>Auto-generated UUID using database or Hibernate strategies,
     * depending on dialect and driver configuration.</p>
     */
    @Id
    @GeneratedValue
    private UUID id;

    /**
     * Display name of the client.
     * <p>Required field; maximum length 255 characters.</p>
     */
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    /**
     * Flag to indicate whether the client is active.
     * <p>Used to filter clients in dropdowns and lists
     * without removing them from the database.</p>
     */
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    /**
     * Override toString for clearer logging/debugging.
     * Avoids printing entire related objects if added later.
     */
    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", active=" + active +
                '}';
    }
}

package net.sphuta.tms.freelancer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Builder;


/**
 * JPA entity representing a Client (Owner).
 *
 * <p>This entity maps to the {@code clients} table and holds information about
 * the project owner (either a company or individual).</p>
 *
 * <p><strong>Changes:</strong></p>
 * <ul>
 *   <li>Primary key type changed from {@code UUID} to {@code int}.</li>
 *   <li>Class renamed to {@code TmsClientEntity}.</li>
 * </ul>
 */
@Entity
@Table(name = "clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ClientEntity {

    /**
     * Primary key for the client.
     * <p>Auto-generated integer (AUTO_INCREMENT) handled by the database.</p>
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

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
}
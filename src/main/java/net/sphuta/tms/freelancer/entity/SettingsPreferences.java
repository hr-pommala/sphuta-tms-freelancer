package net.sphuta.tms.freelancer.entity;



import jakarta.persistence.*;
import lombok.*;
import net.sphuta.tms.freelancer.enums.Rounding;
import net.sphuta.tms.freelancer.enums.WeekStart;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity class representing user settings preferences.
 * <p>
 * This entity maps to the {@code settings_preferences} table in the database.
 * Each user has exactly one preferences record which stores configuration details
 * such as date format, week start day, and rounding preferences.
 * <p>
 * Lombok annotations are used to reduce boilerplate (getters, setters, constructors, builder).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "settings_preferences")
public class SettingsPreferences {

    /**
     * Unique identifier for the user (UUID).
     * <p>
     * Serves as the primary key in the {@code settings_preferences} table.
     */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // auto-increment int
    private int userId;

    /**
     * Preferred date format chosen by the user.
     * <p>
     * Example values: {@code YYYY-MM-DD}, {@code DD-MM-YYYY}.
     * Default is {@code YYYY-MM-DD}.
     */
    @Column(name = "date_format", nullable = false, length = 20)
    private String dateFormat;

    /**
     * Week start day preference.
     * <p>
     * This field is mapped as an enum and stores whether the user’s week starts on
     * Monday ({@code MON}) or Sunday ({@code SUN}).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "week_starts_on", nullable = false, length = 3)
    private WeekStart weekStartsOn;

    /**
     * Rounding preference applied to timesheet entries.
     * <p>
     * Enum values could include: {@code NONE}, {@code NEAREST_6}, {@code NEAREST_15}, {@code NEAREST_30}.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "rounding", nullable = false, length = 16)
    private Rounding rounding;

    /**
     * Timestamp of the last update to this record.
     * <p>
     * Automatically populated whenever the entity is updated using
     * Hibernate’s {@link UpdateTimestamp}.
     */
    @UpdateTimestamp
    @Column(name = "updated_dt", nullable = false)
    private LocalDateTime updatedDt;
}

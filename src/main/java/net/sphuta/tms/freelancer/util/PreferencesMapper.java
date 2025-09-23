package net.sphuta.tms.freelancer.util;


import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.dto.PreferencesDto;
import net.sphuta.tms.freelancer.entity.SettingsPreferences;
import org.springframework.stereotype.Component;

/**
 * Utility class responsible for mapping between
 * Data Transfer Objects (DTOs) and the {@link SettingsPreferences} entity.
 *
 * <p>This class ensures that conversions between
 * {@link PreferencesDto}, {@link PreferencesDto}, and
 * {@link SettingsPreferences} are consistent and reusable across the service layer.</p>
 *
 * <p>Logging is performed at each step to trace mapping activities
 * for debugging and auditing purposes.</p>
 */
@Slf4j
@Component
public class PreferencesMapper {

    /**
     * Convert a {@link PreferencesDto} DTO to a {@link SettingsPreferences} entity.
     *
     * <p>This method is typically used before persisting user preferences
     * into the database. It maps fields directly from request DTO to entity.</p>
     *
     * @param request {@link PreferencesDto} object containing input data
     * @return a fully populated {@link SettingsPreferences} entity
     */
    public SettingsPreferences toEntity(PreferencesDto request) {
        log.info("Entering toEntity() method - converting PreferencesRequest to entity for userId={}", request.userId());
        log.debug("Mapping PreferencesRequest DTO to SettingsPreferences entity: {}", request);

        // Build entity from request DTO
        SettingsPreferences entity = SettingsPreferences.builder()
                .userId(request.userId())
                .dateFormat(request.dateFormat())
                .weekStartsOn(request.weekStartsOn())  // Week start mapping (MON/SUN)
                .rounding(request.rounding())          // Rounding preference mapping
                .build();

        log.info("Exiting toEntity() - Successfully mapped PreferencesRequest with userId={} to entity", entity.getUserId());
        return entity;
    }

    /**
     * Convert a {@link SettingsPreferences} entity to a {@link PreferencesDto} DTO.
     *
     * <p>This method is used when sending data back to clients
     * via REST APIs. It extracts relevant fields and wraps them into a response DTO.</p>
     *
     * @param entity {@link SettingsPreferences} object retrieved from database
     * @return a {@link PreferencesDto} DTO for API response
     */
    public PreferencesDto toResponse(SettingsPreferences entity) {
        log.info("Entering toResponse() method - converting entity to PreferencesResponse for userId={}", entity.getUserId());
        log.debug("Mapping SettingsPreferences entity to PreferencesResponse DTO: {}", entity);

        // Build response DTO from entity
        PreferencesDto response = new PreferencesDto(
                entity.getUserId(),
                entity.getDateFormat(),
                entity.getWeekStartsOn(),
                entity.getRounding(),
                entity.getUpdatedAt()
        );

        log.info("Exiting toResponse() - Successfully mapped SettingsPreferences entity with userId={} to PreferencesResponse", response.userId());
        return response;
    }

    /**
     * Update an existing {@link SettingsPreferences} entity with values from a {@link PreferencesDto}.
     *
     * <p>This method is typically used during update operations to avoid
     * directly manipulating the entity inside the service layer.</p>
     *
     * @param request {@link PreferencesDto} containing updated values
     * @param entity  existing {@link SettingsPreferences} entity to update
     */
    public void updateEntityFromDto(PreferencesDto request, SettingsPreferences entity) {
        log.info("Entering updateEntityFromDto() - updating SettingsPreferences entity for userId={}", entity.getUserId());
        log.debug("Applying updates from PreferencesRequest DTO: {} to entity: {}", request, entity);

        entity.setDateFormat(request.dateFormat());
        entity.setWeekStartsOn(request.weekStartsOn());
        entity.setRounding(request.rounding());

        log.info("Exiting updateEntityFromDto() - Successfully updated entity for userId={}", entity.getUserId());
    }
}

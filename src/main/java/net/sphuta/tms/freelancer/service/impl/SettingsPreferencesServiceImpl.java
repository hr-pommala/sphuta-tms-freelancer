package net.sphuta.tms.freelancer.service.impl;

import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.constants.TmsMessages;
import net.sphuta.tms.freelancer.dto.PreferencesDto;
import net.sphuta.tms.freelancer.entity.SettingsPreferences;
import net.sphuta.tms.freelancer.exception.NotFoundException;
import net.sphuta.tms.freelancer.repository.SettingsPreferencesRepository;
import net.sphuta.tms.freelancer.service.SettingsPreferencesService;
import net.sphuta.tms.freelancer.util.PreferencesMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service implementation for managing user preferences.
 * <p>
 * This class implements the {@link SettingsPreferencesService} interface and
 * provides the business logic for handling user preferences such as:
 * <ul>
 *   <li>Create new preferences</li>
 *   <li>Retrieve preferences by user ID or all users</li>
 *   <li>Update or patch existing preferences</li>
 *   <li>Delete preferences by user ID</li>
 * </ul>
 *
 * Logging is applied at the start, success, and warning/error points
 * to help trace execution flow and debugging.
 */
@Slf4j
@Service
public class SettingsPreferencesServiceImpl implements SettingsPreferencesService {

    /**
     * Repository for performing CRUD operations on
     * {@link SettingsPreferences} entities.
     */
    @Autowired
    private SettingsPreferencesRepository repository;

    /**
     * Mapper utility for converting between
     * {@link PreferencesDto},
     * {@link PreferencesDto}, and
     * {@link SettingsPreferences}.
     */
    @Autowired
    private PreferencesMapper mapper;

    /**
     * Create new preferences for a user.
     *
     * @param request {@link PreferencesDto} containing user preferences
     * @return {@link PreferencesDto} representing created preferences
     * @throws IllegalArgumentException if preferences already exist for the user
     */
    @Override
    public PreferencesDto createPreferences(PreferencesDto request) {
        log.info("Request received to create preferences for userId={}", request.userId());

        if (repository.existsByUserId(request.userId())) {
            log.error("Preferences already exist for userId={}", request.userId());
            throw new IllegalArgumentException("Preferences already exist for userId=" + request.userId());
        }

        SettingsPreferences entity = mapper.toEntity(request);
        SettingsPreferences saved = repository.save(entity);

        log.debug("Preferences entity saved successfully in DB for userId={}", saved.getUserId());
        log.info("Preferences created successfully for userId={}", saved.getUserId());
        return mapper.toResponse(saved);
    }

    /**
     * Retrieve preferences by user ID.
     *
     * @param userId unique identifier of the user
     * @return {@link PreferencesDto} for the given userId
     * @throws NotFoundException if no preferences are found
     */
    @Override
    public PreferencesDto getPreferences(int userId) {
        log.info("Fetching preferences for userId={}", userId);

        SettingsPreferences entity = repository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Preferences not found for userId={}", userId);
                    return new NotFoundException(TmsMessages.PREFERENCES_NOT_FOUND);
                });

        log.info("Preferences fetched successfully for userId={}", userId);
        return mapper.toResponse(entity);
    }

    /**
     * Retrieve all preferences records.
     *
     * @return list of {@link PreferencesDto}
     */
    @Override
    public List<PreferencesDto> getAllPreferences() {
        log.info("Fetching all preferences records from DB");

        List<PreferencesDto> responseList = repository.findAll()
                .stream()
                .map(mapper::toResponse)
                .toList();

        log.info("Total preferences records fetched: {}", responseList.size());
        return responseList;
    }

    /**
     * Update (replace) preferences for a user.
     *
     * @param userId  unique identifier of the user
     * @param request {@link PreferencesDto} containing updated preferences
     * @return updated {@link PreferencesDto}
     * @throws NotFoundException if no preferences exist for the user
     */
    @Override
    public PreferencesDto updatePreferences(int userId, PreferencesDto request) {
        log.info("Request received to update preferences for userId={}", userId);

        SettingsPreferences entity = repository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Preferences not found for userId={} during update", userId);
                    return new NotFoundException(TmsMessages.PREFERENCES_NOT_FOUND);
                });

        // ✅ Use mapper method instead of manually setting fields
        mapper.updateEntityFromDto(request, entity);

        SettingsPreferences updated = repository.save(entity);

        log.debug("Preferences updated in DB for userId={}", updated.getUserId());
        log.info("Preferences updated successfully for userId={}", updated.getUserId());
        return mapper.toResponse(updated);
    }



    /**
     * Delete preferences by user ID.
     *
     * @param userId unique identifier of the user
     * @throws NotFoundException if no preferences exist for the user
     */
    @Override
    public void deletePreferences(int userId) {
        log.warn("Request received to delete preferences for userId={}", userId);

        if (!repository.existsByUserId(userId)) {
            log.error("Cannot delete. Preferences not found for userId={}", userId);
            throw new NotFoundException(TmsMessages.PREFERENCES_NOT_FOUND);
        }

        repository.deleteById(userId);
        log.info("Preferences deleted successfully for userId={}", userId);
    }
}

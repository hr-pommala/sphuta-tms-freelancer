package net.sphuta.tms.freelancer.mockito;


import lombok.extern.slf4j.Slf4j;
import net.sphuta.tms.freelancer.enums.Rounding;
import net.sphuta.tms.freelancer.enums.WeekStart;
import net.sphuta.tms.freelancer.dto.PreferencesDto;
import net.sphuta.tms.freelancer.entity.SettingsPreferences;
import net.sphuta.tms.freelancer.exception.NotFoundException;
import net.sphuta.tms.freelancer.repository.SettingsPreferencesRepository;
import net.sphuta.tms.freelancer.service.impl.SettingsPreferencesServiceImpl;
import net.sphuta.tms.freelancer.util.PreferencesMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for {@link SettingsPreferencesServiceImpl}.
 * <p>
 * Uses single PreferencesDto record for both request and response.
 */
@Slf4j
class SettingsPreferencesServiceImplTest {

    @Mock
    private SettingsPreferencesRepository repository;

    @Mock
    private PreferencesMapper mapper;

    @InjectMocks
    private SettingsPreferencesServiceImpl service;

    private PreferencesDto request;
    private SettingsPreferences entity;
    private PreferencesDto response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Request contains first 4 fields, updatedAt is null
        request = new PreferencesDto(
                123,
                "YYYY-MM-DD",
                WeekStart.MON,
                Rounding.NONE,
                null
        );

        entity = SettingsPreferences.builder()
                .userId(123)
                .dateFormat("YYYY-MM-DD")
                .weekStartsOn(WeekStart.MON)
                .rounding(Rounding.NONE)
                .updatedAt(LocalDateTime.now())
                .build();

        // Response contains updatedAt
        response = new PreferencesDto(
                123,
                "YYYY-MM-DD",
                WeekStart.MON,
                Rounding.NONE,
                entity.getUpdatedAt()
        );

        log.info("Test setup completed for userId: {}", request.userId());
    }

    @Test
    void testCreatePreferences_Success() {
        when(repository.existsByUserId(123)).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        PreferencesDto result = service.createPreferences(request);

        assertNotNull(result);
        assertEquals(123, result.userId());
        verify(repository, times(1)).save(entity);
    }

    @Test
    void testCreatePreferences_AlreadyExists() {
        when(repository.existsByUserId(123)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.createPreferences(request));
        verify(repository, never()).save(any());
    }

    @Test
    void testGetPreferences_Success() {
        when(repository.findById(123)).thenReturn(Optional.of(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        PreferencesDto result = service.getPreferences(123);

        // <-- fixed: expect int 123 (userId is an int)
        assertEquals(123, result.userId());
        verify(repository, times(1)).findById(123);
    }

    @Test
    void testGetPreferences_NotFound() {
        when(repository.findById(123)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getPreferences(123));
    }

    @Test
    void testGetAllPreferences() {
        when(repository.findAll()).thenReturn(Arrays.asList(entity));
        when(mapper.toResponse(entity)).thenReturn(response);

        List<PreferencesDto> results = service.getAllPreferences();

        assertEquals(1, results.size());
        assertEquals(123, results.get(0).userId());
    }

    @Test
    void testUpdatePreferences_Success() {
        when(repository.findById(123)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        PreferencesDto result = service.updatePreferences(123, request);

        assertEquals(123, result.userId());
        verify(repository, times(1)).save(entity);
    }

    @Test
    void testDeletePreferences_Success() {
        when(repository.existsByUserId(123)).thenReturn(true);
        doNothing().when(repository).deleteById(123);

        service.deletePreferences(123);

        verify(repository, times(1)).deleteById(123);
    }

    @Test
    void testDeletePreferences_NotFound() {
        when(repository.existsByUserId(123)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> service.deletePreferences(123));
    }
}

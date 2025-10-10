package net.sphuta.tms.freelancer.controller;

import net.sphuta.tms.freelancer.constants.TmsMessages;
import net.sphuta.tms.freelancer.dto.SettingsInvoicingDTO;
import net.sphuta.tms.freelancer.dto.TmsApiResponse;
import net.sphuta.tms.freelancer.exception.NotFoundException;
import net.sphuta.tms.freelancer.service.SettingsInvoicingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettingsInvoicingControllerTest {

    @Mock
    private SettingsInvoicingService service;

    @InjectMocks
    private SettingsInvoicingController controller;

    private SettingsInvoicingDTO sampleDto;

    @BeforeEach
    void setUp() {
        sampleDto = new SettingsInvoicingDTO(
                1001,
                "USD",
                "TAX-123",
                new BigDecimal("0.08"),
                "INV-${yyyy}${seq:5}",
                14,
                new BigDecimal("0.05"),
                "tmpl_default",
                "logo-123",
                null // updatedAt is not required for request, set as null in tests
        );
    }

    @Test
    void testGetAllSettings() {
        when(service.getAllSettings()).thenReturn(List.of(sampleDto));

        TmsApiResponse<List<SettingsInvoicingDTO>> response = controller.getAllSettings();

        assertEquals(200, response.success());
        assertEquals(TmsMessages.MSG_FETCH_ALL_SETTINGS, response.message());
        assertEquals(1, response.data().size());
        assertEquals("success", response.data().status());
        verify(service, times(1)).getAllSettings();
    }

    @Test
    void testGetSettingsByUserId_Found() {
        when(service.getSettingsByUserId(sampleDto.userId())).thenReturn(Optional.of(sampleDto));

        ResponseEntity<ApiResponse<SettingsInvoicingDTO>> response = controller.getSettingsByUserId(sampleDto.userId());

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(ApiMessageConstants.MSG_FETCH_SINGLE_SETTING, response.getBody().message());
        assertEquals(sampleDto.userId(), response.getBody().data().userId());
        assertEquals("success", response.getBody().status());
        verify(service, times(1)).getSettingsByUserId(sampleDto.userId());
    }

    @Test
    void testGetSettingsByUserId_NotFound() {
        when(service.getSettingsByUserId(9999)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
                controller.getSettingsByUserId(9999));

        verify(service, times(1)).getSettingsByUserId(9999);
    }

    @Test
    void testCreateSettings() {
        when(service.createSettings(sampleDto)).thenReturn(sampleDto);

        ResponseEntity<ApiResponse<SettingsInvoicingDTO>> response = controller.createSettings(sampleDto);

        assertEquals(201, response.getStatusCodeValue());
        assertEquals(ApiMessageConstants.MSG_SETTINGS_CREATED, response.getBody().message());
        assertEquals(sampleDto.userId(), response.getBody().data().userId());
        assertEquals("success", response.getBody().status());
        verify(service, times(1)).createSettings(sampleDto);
    }

    @Test
    void testUpdateSettings() {
        when(service.updateSettings(sampleDto.userId(), sampleDto)).thenReturn(sampleDto);

        ResponseEntity<ApiResponse<SettingsInvoicingDTO>> response = controller.updateSettings(sampleDto.userId(), sampleDto);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(ApiMessageConstants.MSG_SETTINGS_UPDATED, response.getBody().message());
        assertEquals(sampleDto.userId(), response.getBody().data().userId());
        assertEquals("success", response.getBody().status());
        verify(service, times(1)).updateSettings(sampleDto.userId(), sampleDto);
    }

    @Test
    void testDeleteSettings() {
        doNothing().when(service).deleteSettings(sampleDto.userId());

        ResponseEntity<ApiResponse<Void>> response = controller.deleteSettings(sampleDto.userId());

        assertEquals(200, response.getStatusCodeValue());
        assertEquals(ApiMessageConstants.MSG_SETTINGS_DELETED, response.getBody().message());
        assertNull(response.getBody().data());
        assertEquals("success", response.getBody().status());
        verify(service, times(1)).deleteSettings(sampleDto.userId());
    }
}

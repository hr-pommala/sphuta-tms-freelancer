package net.sphuta.tms.freelancer.controller;

import net.sphuta.tms.freelancer.constants.TmsMessages;
import net.sphuta.tms.freelancer.dto.SettingsInvoicingDTO;
import net.sphuta.tms.freelancer.response.TmsApiResponse;
import net.sphuta.tms.freelancer.service.SettingsInvoicingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.NoSuchElementException;

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

        Object response = controller.getAllSettings();
        try {
            var cls = response.getClass();
            var statusCode = (Integer) cls.getMethod("statusCode").invoke(response);
            var success = (Boolean) cls.getMethod("success").invoke(response);
            var message = (String) cls.getMethod("message").invoke(response);
            var data = cls.getMethod("data").invoke(response);

            assertEquals(200, statusCode);
            assertTrue(success);
            assertEquals(TmsMessages.MSG_FETCH_ALL_SETTINGS, message);
            assertNotNull(data);
            assertEquals(1, ((java.util.List<?>) data).size());
        } catch (ReflectiveOperationException e) {
            fail("Reflection failed when inspecting TmsApiResponse: " + e.getMessage());
        }

        verify(service, times(1)).getAllSettings();
    }

    @Test
    void testGetSettingsByUserId_Found() {
        when(service.getSettingsByUserId(sampleDto.userId())).thenReturn(Optional.of(sampleDto));

        Object response = controller.getSettingsByUserId(sampleDto.userId());
        try {
            var cls = response.getClass();
            var statusCode = (Integer) cls.getMethod("statusCode").invoke(response);
            var success = (Boolean) cls.getMethod("success").invoke(response);
            var message = (String) cls.getMethod("message").invoke(response);
            var data = cls.getMethod("data").invoke(response);

            assertEquals(200, statusCode);
            assertTrue(success);
            assertEquals(TmsMessages.MSG_FETCH_SINGLE_SETTING, message);
            assertNotNull(data);
            var dto = data;
            var userId = (Integer) dto.getClass().getMethod("userId").invoke(dto);
            assertEquals(sampleDto.userId(), userId.intValue());
        } catch (ReflectiveOperationException e) {
            fail("Reflection failed when inspecting TmsApiResponse: " + e.getMessage());
        }

        verify(service, times(1)).getSettingsByUserId(sampleDto.userId());
    }

    @Test
    void testGetSettingsByUserId_NotFound() {
        when(service.getSettingsByUserId(9999)).thenReturn(Optional.empty());

        // Controller calls orElseThrow() on Optional — expect NoSuchElementException
        assertThrows(NoSuchElementException.class, () -> controller.getSettingsByUserId(9999));

        verify(service, times(1)).getSettingsByUserId(9999);
    }

    @Test
    void testCreateSettings() {
        when(service.createSettings(sampleDto)).thenReturn(sampleDto);

        Object response = controller.createSettings(sampleDto);
        try {
            var cls = response.getClass();
            var statusCode = (Integer) cls.getMethod("statusCode").invoke(response);
            var success = (Boolean) cls.getMethod("success").invoke(response);
            var message = (String) cls.getMethod("message").invoke(response);
            var data = cls.getMethod("data").invoke(response);

            assertEquals(201, statusCode);
            assertTrue(success);
            assertEquals(TmsMessages.MSG_SETTINGS_CREATED, message);
            assertNotNull(data);
            var userId = (Integer) data.getClass().getMethod("userId").invoke(data);
            assertEquals(sampleDto.userId(), userId.intValue());
        } catch (ReflectiveOperationException e) {
            fail("Reflection failed when inspecting TmsApiResponse: " + e.getMessage());
        }

        verify(service, times(1)).createSettings(sampleDto);
    }

    @Test
    void testUpdateSettings() {
        when(service.updateSettings(sampleDto.userId(), sampleDto)).thenReturn(sampleDto);

        Object response = controller.updateSettings(sampleDto.userId(), sampleDto);
        try {
            var cls = response.getClass();
            var statusCode = (Integer) cls.getMethod("statusCode").invoke(response);
            var success = (Boolean) cls.getMethod("success").invoke(response);
            var message = (String) cls.getMethod("message").invoke(response);
            var data = cls.getMethod("data").invoke(response);

            assertEquals(200, statusCode);
            assertTrue(success);
            assertEquals(TmsMessages.MSG_SETTINGS_UPDATED, message);
            assertNotNull(data);
            var userId = (Integer) data.getClass().getMethod("userId").invoke(data);
            assertEquals(sampleDto.userId(), userId.intValue());
        } catch (ReflectiveOperationException e) {
            fail("Reflection failed when inspecting TmsApiResponse: " + e.getMessage());
        }

        verify(service, times(1)).updateSettings(sampleDto.userId(), sampleDto);
    }

    @Test
    void testDeleteSettings() {
        doNothing().when(service).deleteSettings(sampleDto.userId());

        Object response = controller.deleteSettings(sampleDto.userId());
        try {
            var cls = response.getClass();
            var statusCode = (Integer) cls.getMethod("statusCode").invoke(response);
            var success = (Boolean) cls.getMethod("success").invoke(response);
            var message = (String) cls.getMethod("message").invoke(response);
            var data = cls.getMethod("data").invoke(response);

            assertEquals(200, statusCode);
            assertTrue(success);
            assertEquals(TmsMessages.MSG_SETTINGS_DELETED, message);
            assertNull(data);
        } catch (ReflectiveOperationException e) {
            fail("Reflection failed when inspecting TmsApiResponse: " + e.getMessage());
        }

        verify(service, times(1)).deleteSettings(sampleDto.userId());
    }
}

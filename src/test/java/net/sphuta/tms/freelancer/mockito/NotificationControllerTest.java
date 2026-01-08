package net.sphuta.tms.freelancer.mockito;

import net.sphuta.tms.freelancer.controller.NotificationController;
import net.sphuta.tms.freelancer.dto.*;
import net.sphuta.tms.freelancer.response.TmsApiResponse;
import net.sphuta.tms.freelancer.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationControllerTest {

    @Mock
    private NotificationService svc;

    @InjectMocks
    private NotificationController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testListNotifications() {
        long userId = 1L;

        // Prepare mock response
        NotificationCount count = new NotificationCount(5L, 3L); // total=5, unread=3
        List<NotificationItem> items = List.of(
                new NotificationItem(101L, "Title 1", "Sub 1", "Desc 1", "http://url1", 1, false),
                new NotificationItem(102L, "Title 2", "Sub 2", "Desc 2", "http://url2", 2, false)
        );
        NotificationListResponse mockResponse = new NotificationListResponse(count, items);

        // Mock service
        when(svc.listUnreadNotifications(userId, 20, 0)).thenReturn(mockResponse);

        // Call controller (returns TmsApiResponse)
        TmsApiResponse<NotificationListResponse> response = controller.listNotifications(userId, 20, 0);

        // Assertions
        assertEquals(200, response.statusCode());
        assertTrue(response.success());
        assertEquals(mockResponse, response.data());
        verify(svc, times(1)).listUnreadNotifications(userId, 20, 0);
    }

    @Test
    void testCreateNotification_success() {
        long userId = 1L;

        NotificationCreateRequest request = new NotificationCreateRequest(
                userId,
                "timesheet",
                "pending",
                "Test Title",
                "Test Subtitle",
                "This is a test notification",
                "http://action.url",
                1
        );

        when(svc.createNotification(request, userId)).thenReturn(100L);

        TmsApiResponse<Map<String, Object>> response = controller.createNotification(userId, request);

        assertEquals(201, response.statusCode());
        assertTrue(response.success());
        assertEquals(100L, response.data().get("id"));
        verify(svc, times(1)).createNotification(request, userId);
    }

    @Test
    void testCreateNotification_userIdMismatch() {
        long userId = 1L;
        NotificationCreateRequest request = new NotificationCreateRequest(
                2L, // different userId
                "timesheet",
                "pending",
                "Test Title",
                "Test Subtitle",
                "This is a test notification",
                "http://action.url",
                1
        );

        TmsApiResponse<Map<String, Object>> response = controller.createNotification(userId, request);

        assertEquals(400, response.statusCode());
        assertFalse(response.success());
        assertTrue(response.message().toLowerCase().contains("user id mismatch") || response.message().toLowerCase().contains("invalid"));
        verifyNoInteractions(svc);
    }

    @Test
    void testMarkOneRead() {
        long notificationId = 101L;

        doNothing().when(svc).markOneRead(notificationId);

        TmsApiResponse<String> response = controller.markOneRead(notificationId);

        assertEquals(200, response.statusCode());
        assertTrue(response.success());
        assertEquals("Notification 101 marked as read", response.message());
        verify(svc, times(1)).markOneRead(notificationId);
    }

    @Test
    void testMarkAllRead() {
        doNothing().when(svc).markAllRead();

        TmsApiResponse<Void> response = controller.markAllRead();

        assertEquals(200, response.statusCode());
        assertTrue(response.success());
        assertNull(response.data());
        verify(svc, times(1)).markAllRead();
    }
}

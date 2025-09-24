package net.sphuta.tms.freelancer.dto;

import jakarta.validation.constraints.*;

/**
 * Request DTO for creating a notification. Uses Jakarta validation annotations.
 * Use @Valid in controller to trigger validation.
 */
public record NotificationCreateRequest(

        @NotNull(message = "userId is required")
        Long userId,

        @NotBlank(message = "category is required")
        @Pattern(regexp = "timesheet|invoice", message = "category must be 'timesheet' or 'invoice'")
        String category,

        @NotBlank(message = "status is required")
        @Pattern(regexp = "pending|sent|due", message = "status must be one of 'pending', 'sent', 'due'")
        String status,

        @NotBlank(message = "title is required")
        @Size(max = 255, message = "title must be at most 255 characters")
        String title,

        @Size(max = 255, message = "subtitle must be at most 255 characters")
        String subtitle,

        @NotBlank(message = "notificationDescription is required")
        @Size(max = 1000, message = "notificationDescription must be at most 1000 characters")
        String notificationDescription,

        @Size(max = 1000, message = "actionUrl must be at most 1000 characters")
        String actionUrl,

        @Min(value = 0, message = "priority must be between 0 and 2")
        @Max(value = 2, message = "priority must be between 0 and 2")
        Integer priority

) {}

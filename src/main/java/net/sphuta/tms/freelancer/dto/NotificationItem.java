package net.sphuta.tms.freelancer.dto;

/**
 * Response DTO for a notification item.
 * No validation required because this is a response object.
 */
public record NotificationItem(
        Long id,
        String title,
        String subtitle,
        String notification_description,
        String action_url,
        Integer priority,
        boolean is_read
//        String ack_url
) {}

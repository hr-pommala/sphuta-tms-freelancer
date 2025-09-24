package net.sphuta.tms.freelancer.dto;

import java.util.List;
/**
 * A record representing a list of notifications along with their count.
 *
 * @param count the count of notifications
 * @param items the list of notification items
 */
public record NotificationListResponse(NotificationCount count, List<NotificationItem> items) {}

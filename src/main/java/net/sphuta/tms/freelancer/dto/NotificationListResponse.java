package net.sphuta.tms.freelancer.dto;

import java.util.List;

public record NotificationListResponse(NotificationCount count, List<NotificationItem> items) {}

package net.sphuta.tms.freelancer.dto;

/**
 * A record representing the count of notifications.
 *
 * @param total  the total number of notifications
 * @param unread the number of unread notifications
 */
public record NotificationCount(long total, long unread) {}

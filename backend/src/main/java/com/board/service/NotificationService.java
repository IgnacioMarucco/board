package com.board.service;

import com.board.dto.notification.NotificationResponse;
import com.board.entity.User;

import java.util.List;

/**
 * Service interface for notification operations.
 */
public interface NotificationService {

    /**
     * Gets all notifications for a user.
     *
     * @param userId the user ID
     * @return list of notifications
     */
    List<NotificationResponse> getUserNotifications(Long userId);

    /**
     * Gets unread notifications for a user.
     *
     * @param userId the user ID
     * @return list of unread notifications
     */
    List<NotificationResponse> getUnreadNotifications(Long userId);

    /**
     * Marks a notification as read.
     *
     * @param notificationId the notification ID
     * @param userId         the requesting user's ID
     * @return the updated notification
     */
    NotificationResponse markAsRead(Long notificationId, Long userId);

    /**
     * Marks all notifications as read for a user.
     *
     * @param userId the user ID
     */
    void markAllAsRead(Long userId);

    /**
     * Deletes a notification.
     *
     * @param notificationId the notification ID
     * @param userId         the requesting user's ID
     */
    void deleteNotification(Long notificationId, Long userId);

    /**
     * Creates a notification for a recipient.
     *
     * @param recipient   the recipient user
     * @param triggeredBy the actor user
     * @param type        the notification type
     * @param title       the title
     * @param message     the message
     * @param linkUrl     optional link URL
     */
    void createNotification(User recipient, User triggeredBy,
            String type, String title, String message, String linkUrl);
}

package com.board.controller;

import com.board.dto.notification.NotificationResponse;
import com.board.security.UserPrincipal;
import com.board.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for notification operations.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management endpoints")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Gets all notifications for the current user.
     *
     * @param principal the authenticated user
     * @return list of notifications
     */
    @GetMapping
    @Operation(summary = "Get all notifications for current user")
    public ResponseEntity<List<NotificationResponse>> getUserNotifications(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(notificationService.getUserNotifications(principal.getUserId()));
    }

    /**
     * Gets unread notifications for the current user.
     *
     * @param principal the authenticated user
     * @return list of unread notifications
     */
    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(
                principal.getUserId()));
    }

    /**
     * Marks a notification as read.
     *
     * @param notificationId the notification ID
     * @param principal      the authenticated user
     * @return the updated notification
     */
    @PutMapping("/{notificationId}/read")
    @Operation(summary = "Mark notification as read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(notificationService.markAsRead(notificationId,
                principal.getUserId()));
    }

    /**
     * Marks all notifications as read.
     *
     * @param principal the authenticated user
     * @return no content
     */
    @PutMapping("/read-all")
    @Operation(summary = "Mark all notifications as read")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAllAsRead(principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes a notification.
     *
     * @param notificationId the notification ID
     * @param principal      the authenticated user
     * @return no content
     */
    @DeleteMapping("/{notificationId}")
    @Operation(summary = "Delete a notification")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.deleteNotification(notificationId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}

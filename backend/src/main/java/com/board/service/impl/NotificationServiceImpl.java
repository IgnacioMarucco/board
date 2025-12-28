package com.board.service.impl;

import com.board.dto.notification.NotificationResponse;
import com.board.entity.Notification;
import com.board.entity.User;
import com.board.exception.ForbiddenException;
import com.board.exception.NotFoundException;
import com.board.mapper.NotificationMapper;
import com.board.repository.NotificationRepository;
import com.board.repository.UserRepository;
import com.board.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of NotificationService.
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(Long userId) {
        User user = findUserById(userId);
        List<Notification> notifications = notificationRepository
                .findByRecipientOrderByCreatedAtDesc(user);
        return notificationMapper.toResponseList(notifications);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        User user = findUserById(userId);
        List<Notification> notifications = notificationRepository
                .findByRecipientAndIsReadFalseOrderByCreatedAtDesc(user);
        return notificationMapper.toResponseList(notifications);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = findNotificationById(notificationId);
        validateOwnership(notification, userId);

        notification.markAsRead();
        notification = notificationRepository.save(notification);

        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        User user = findUserById(userId);
        List<Notification> unreadNotifications = notificationRepository
                .findByRecipientAndIsReadFalseOrderByCreatedAtDesc(user);

        unreadNotifications.forEach(Notification::markAsRead);
        notificationRepository.saveAll(unreadNotifications);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = findNotificationById(notificationId);
        validateOwnership(notification, userId);

        notification.softDelete();
        notificationRepository.save(notification);
    }

    private Notification findNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId)
                .filter(n -> n.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Notification not found"));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private void validateOwnership(Notification notification, Long userId) {
        if (!notification.getRecipient().getId().equals(userId)) {
            throw new ForbiddenException("You can only access your own notifications");
        }
    }
}

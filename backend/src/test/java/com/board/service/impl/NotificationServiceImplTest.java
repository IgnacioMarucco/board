package com.board.service.impl;

import com.board.dto.notification.NotificationResponse;
import com.board.entity.Notification;
import com.board.entity.User;
import com.board.exception.ForbiddenException;
import com.board.mapper.NotificationMapper;
import com.board.repository.NotificationRepository;
import com.board.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for NotificationServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User testUser;
    private Notification testNotification;
    private NotificationResponse testNotificationResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .build();

        testNotification = Notification.builder()
                .id(1L)
                .type("MENTION")
                .title("You were mentioned")
                .message("User mentioned you in a comment")
                .isRead(false)
                .recipient(testUser)
                .build();

        testNotificationResponse = NotificationResponse.builder()
                .id(1L)
                .type("MENTION")
                .title("You were mentioned")
                .isRead(false)
                .build();
    }

    @Nested
    @DisplayName("getUserNotifications")
    class GetUserNotifications {

        @Test
        @DisplayName("should return user's notifications")
        void shouldReturnUserNotifications() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(notificationRepository.findByRecipientOrderByCreatedAtDesc(testUser))
                    .thenReturn(List.of(testNotification));
            when(notificationMapper.toResponseList(any()))
                    .thenReturn(List.of(testNotificationResponse));

            // When
            List<NotificationResponse> response = notificationService.getUserNotifications(1L);

            // Then
            assertThat(response).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getUnreadNotifications")
    class GetUnreadNotifications {

        @Test
        @DisplayName("should return only unread notifications")
        void shouldReturnUnreadNotifications() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(notificationRepository.findByRecipientAndIsReadFalseOrderByCreatedAtDesc(testUser))
                    .thenReturn(List.of(testNotification));
            when(notificationMapper.toResponseList(any()))
                    .thenReturn(List.of(testNotificationResponse));

            // When
            List<NotificationResponse> response = notificationService.getUnreadNotifications(1L);

            // Then
            assertThat(response).hasSize(1);
        }
    }

    @Nested
    @DisplayName("markAsRead")
    class MarkAsRead {

        @Test
        @DisplayName("should mark notification as read successfully")
        void shouldMarkAsReadSuccessfully() {
            // Given
            when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));
            when(notificationRepository.save(any(Notification.class)))
                    .thenReturn(testNotification);
            when(notificationMapper.toResponse(any(Notification.class)))
                    .thenReturn(testNotificationResponse);

            // When
            NotificationResponse response = notificationService.markAsRead(1L, 1L);

            // Then
            assertThat(response).isNotNull();
            verify(notificationRepository).save(testNotification);
        }

        @Test
        @DisplayName("should throw ForbiddenException when not recipient")
        void shouldThrowWhenNotRecipient() {
            // Given
            when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));

            // When/Then
            assertThatThrownBy(() -> notificationService.markAsRead(1L, 999L))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessage("You can only access your own notifications");
        }
    }

    @Nested
    @DisplayName("markAllAsRead")
    class MarkAllAsRead {

        @Test
        @DisplayName("should mark all notifications as read")
        void shouldMarkAllAsRead() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(notificationRepository.findByRecipientAndIsReadFalseOrderByCreatedAtDesc(testUser))
                    .thenReturn(List.of(testNotification));

            // When
            notificationService.markAllAsRead(1L);

            // Then
            verify(notificationRepository).saveAll(any());
        }
    }

    @Nested
    @DisplayName("deleteNotification")
    class DeleteNotification {

        @Test
        @DisplayName("should soft delete notification successfully")
        void shouldSoftDeleteNotificationSuccessfully() {
            // Given
            when(notificationRepository.findById(1L)).thenReturn(Optional.of(testNotification));

            // When
            notificationService.deleteNotification(1L, 1L);

            // Then
            verify(notificationRepository).save(testNotification);
            assertThat(testNotification.getDeletedAt()).isNotNull();
        }
    }
}

package com.board.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for notification data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private String type;
    private String title;
    private String message;
    private Boolean isRead;
    private LocalDateTime readAt;
    private String linkUrl;
    private Long recipientId;
    private Long triggeredById;
    private String triggeredByEmail;
    private LocalDateTime createdAt;
}

package com.board.dto.activity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for activity log entries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLogResponse {
    private Long id;
    private String action;
    private String entityType;
    private Long entityId;
    private String details;
    private Long actorId;
    private String actorUsername;
    private LocalDateTime createdAt;
}

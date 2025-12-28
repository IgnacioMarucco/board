package com.board.dto.task;

import com.board.entity.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for task data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private Long id;
    private String key;
    private String title;
    private String description;
    private TaskStatus status;
    private BigDecimal estimatedHours;
    private Integer position;

    private Long storyId;
    private String storyKey;

    private Long assigneeId;
    private String assigneeEmail;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

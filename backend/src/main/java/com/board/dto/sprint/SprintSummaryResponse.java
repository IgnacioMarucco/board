package com.board.dto.sprint;

import com.board.entity.enums.SprintStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for sprint summary metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintSummaryResponse {

    private Long sprintId;
    private String sprintName;
    private SprintStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer committedPoints;
    private Integer committedStoryCount;
    private Integer completedPoints;
    private Integer completedStoryCount;
    private Integer spilloverCount;
    private Integer scopeChangeCount;
    private Double completionRate;
}

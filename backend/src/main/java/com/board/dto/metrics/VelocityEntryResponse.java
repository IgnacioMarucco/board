package com.board.dto.metrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Represents a velocity entry for a sprint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VelocityEntryResponse {
    private Long sprintId;
    private String sprintName;
    private Integer completedPoints;
    private LocalDate endDate;
}

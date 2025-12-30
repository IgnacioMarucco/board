package com.board.dto.metrics;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "Sprint identifier", example = "42")
    private Long sprintId;
    @Schema(description = "Sprint name", example = "Sprint 5")
    private String sprintName;
    @Schema(description = "Completed story points for the sprint", example = "28")
    private Integer completedPoints;
    @Schema(description = "Sprint end date", example = "2025-01-14")
    private LocalDate endDate;
}

package com.board.dto.metrics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for sprint burndown metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BurndownResponse {
    @Schema(description = "Sprint identifier", example = "42")
    private Long sprintId;
    @Schema(description = "Sprint name", example = "Sprint 5")
    private String sprintName;
    @Schema(description = "Story points committed at sprint start", example = "34")
    private Integer committedPoints;
    @Schema(
            description = "Daily burndown points",
            example = "[{\"date\":\"2025-01-01\",\"remainingPoints\":34}]")
    private List<BurndownPointResponse> points;
}

package com.board.dto.metrics;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Represents a single burndown data point.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BurndownPointResponse {
    @Schema(description = "Snapshot date in project time zone", example = "2025-01-01")
    private LocalDate date;
    @Schema(description = "Remaining story points for the sprint", example = "21")
    private Integer remainingPoints;
}

package com.board.dto.metrics;

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
    private LocalDate date;
    private Integer remainingPoints;
}

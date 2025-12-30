package com.board.dto.metrics;

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
    private Long sprintId;
    private String sprintName;
    private Integer committedPoints;
    private List<BurndownPointResponse> points;
}

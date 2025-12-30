package com.board.dto.metrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for project velocity metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VelocityResponse {
    private Long projectId;
    private List<VelocityEntryResponse> sprints;
}

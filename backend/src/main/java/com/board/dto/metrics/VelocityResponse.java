package com.board.dto.metrics;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "Project identifier", example = "10")
    private Long projectId;
    @Schema(
            description = "Velocity entries by sprint",
            example = "[{\"sprintId\":42,\"sprintName\":\"Sprint 5\","
                    + "\"completedPoints\":28,\"endDate\":\"2025-01-14\"}]")
    private List<VelocityEntryResponse> sprints;
}

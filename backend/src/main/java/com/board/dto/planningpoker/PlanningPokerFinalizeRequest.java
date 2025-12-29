package com.board.dto.planningpoker;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for finalizing a planning poker session.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanningPokerFinalizeRequest {

    @NotNull(message = "Final points are required")
    private Integer finalPoints;
}

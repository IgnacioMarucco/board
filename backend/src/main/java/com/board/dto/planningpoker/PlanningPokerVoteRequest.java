package com.board.dto.planningpoker;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for submitting a planning poker vote.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanningPokerVoteRequest {

    @NotNull(message = "Vote value is required")
    private Integer value;
}

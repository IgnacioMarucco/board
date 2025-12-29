package com.board.dto.planningpoker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for a planning poker vote.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanningPokerVoteResponse {

    private Long userId;
    private Integer value;
    private LocalDateTime createdAt;
}

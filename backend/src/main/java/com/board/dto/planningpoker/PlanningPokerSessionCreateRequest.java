package com.board.dto.planningpoker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a planning poker session.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanningPokerSessionCreateRequest {

    private Long ceremonyId;
}

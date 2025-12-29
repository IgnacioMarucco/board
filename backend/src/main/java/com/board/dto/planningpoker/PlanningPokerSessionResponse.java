package com.board.dto.planningpoker;

import com.board.entity.enums.PlanningPokerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for planning poker session data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanningPokerSessionResponse {

    private Long id;
    private PlanningPokerStatus status;
    private Long storyId;
    private String storyKey;
    private Long ceremonyId;
    private Long createdById;
    private Integer finalPoints;
    private Integer voteCount;
    private List<PlanningPokerVoteResponse> votes;
    private LocalDateTime createdAt;
    private LocalDateTime revealedAt;
    private LocalDateTime closedAt;
}

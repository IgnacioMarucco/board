package com.board.service;

import com.board.dto.planningpoker.PlanningPokerFinalizeRequest;
import com.board.dto.planningpoker.PlanningPokerSessionCreateRequest;
import com.board.dto.planningpoker.PlanningPokerSessionResponse;
import com.board.dto.planningpoker.PlanningPokerVoteRequest;

/**
 * Service interface for planning poker operations.
 */
public interface PlanningPokerService {

    PlanningPokerSessionResponse createSession(Long storyId,
            PlanningPokerSessionCreateRequest request, Long userId);

    PlanningPokerSessionResponse getSession(Long sessionId, Long userId);

    PlanningPokerSessionResponse vote(Long sessionId, PlanningPokerVoteRequest request, Long userId);

    PlanningPokerSessionResponse reveal(Long sessionId, Long userId);

    PlanningPokerSessionResponse finalizeSession(Long sessionId,
            PlanningPokerFinalizeRequest request, Long userId);
}

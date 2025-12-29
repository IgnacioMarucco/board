package com.board.controller;

import com.board.dto.planningpoker.PlanningPokerFinalizeRequest;
import com.board.dto.planningpoker.PlanningPokerSessionCreateRequest;
import com.board.dto.planningpoker.PlanningPokerSessionResponse;
import com.board.dto.planningpoker.PlanningPokerVoteRequest;
import com.board.security.UserPrincipal;
import com.board.service.PlanningPokerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for planning poker operations.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Planning Poker", description = "Planning poker endpoints")
public class PlanningPokerController {

    private final PlanningPokerService planningPokerService;

    @PostMapping("/stories/{storyId}/planning-poker")
    @Operation(summary = "Create a planning poker session for a story")
    public ResponseEntity<PlanningPokerSessionResponse> createSession(
            @PathVariable Long storyId,
            @Valid @RequestBody PlanningPokerSessionCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        PlanningPokerSessionResponse response = planningPokerService.createSession(
                storyId, request, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/planning-poker/{sessionId}")
    @Operation(summary = "Get a planning poker session")
    public ResponseEntity<PlanningPokerSessionResponse> getSession(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(planningPokerService.getSession(sessionId, principal.getUserId()));
    }

    @PostMapping("/planning-poker/{sessionId}/votes")
    @Operation(summary = "Submit a planning poker vote")
    public ResponseEntity<PlanningPokerSessionResponse> vote(
            @PathVariable Long sessionId,
            @Valid @RequestBody PlanningPokerVoteRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(planningPokerService.vote(sessionId, request, principal.getUserId()));
    }

    @PostMapping("/planning-poker/{sessionId}/reveal")
    @Operation(summary = "Reveal planning poker votes")
    public ResponseEntity<PlanningPokerSessionResponse> reveal(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(planningPokerService.reveal(sessionId, principal.getUserId()));
    }

    @PostMapping("/planning-poker/{sessionId}/finalize")
    @Operation(summary = "Finalize planning poker and assign points")
    public ResponseEntity<PlanningPokerSessionResponse> finalizeSession(
            @PathVariable Long sessionId,
            @Valid @RequestBody PlanningPokerFinalizeRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(planningPokerService.finalizeSession(
                sessionId, request, principal.getUserId()));
    }
}

package com.board.controller;

import com.board.dto.metrics.BurndownResponse;
import com.board.dto.metrics.VelocityResponse;
import com.board.security.UserPrincipal;
import com.board.service.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for metrics endpoints.
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/metrics")
@RequiredArgsConstructor
@Tag(name = "Metrics", description = "Metrics endpoints for burndown and velocity")
public class MetricsController {

    private final MetricsService metricsService;

    @GetMapping("/sprints/{sprintId}/burndown")
    @Operation(summary = "Get sprint burndown data")
    public ResponseEntity<BurndownResponse> getBurndown(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(metricsService.getBurndown(
                projectId, sprintId, principal.getUserId()));
    }

    @GetMapping("/velocity")
    @Operation(summary = "Get project velocity")
    public ResponseEntity<VelocityResponse> getVelocity(
            @PathVariable Long projectId,
            @RequestParam(value = "limit", required = false) Integer limit,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(metricsService.getVelocity(
                projectId, principal.getUserId(), limit));
    }
}

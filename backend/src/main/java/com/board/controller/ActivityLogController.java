package com.board.controller;

import com.board.dto.activity.ActivityLogResponse;
import com.board.security.UserPrincipal;
import com.board.service.ActivityLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for activity feed endpoints.
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/activity")
@RequiredArgsConstructor
@Tag(name = "Activity", description = "Project activity feed endpoints")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @GetMapping
    @Operation(summary = "Get activity feed for a project")
    public ResponseEntity<List<ActivityLogResponse>> getProjectActivity(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(activityLogService.getActivityForProject(
                projectId, principal.getUserId()));
    }
}

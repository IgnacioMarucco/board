package com.board.controller;

import com.board.dto.sprint.SprintCreateRequest;
import com.board.dto.sprint.SprintResponse;
import com.board.dto.sprint.SprintUpdateRequest;
import com.board.security.UserPrincipal;
import com.board.service.SprintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for sprint operations.
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/sprints")
@RequiredArgsConstructor
@Tag(name = "Sprints", description = "Sprint management endpoints")
public class SprintController {

    private final SprintService sprintService;

    /**
     * Creates a new sprint.
     *
     * @param projectId the project ID
     * @param request   the create request
     * @param principal the authenticated user
     * @return the created sprint
     */
    @PostMapping
    @Operation(summary = "Create a new sprint")
    public ResponseEntity<SprintResponse> createSprint(
            @PathVariable Long projectId,
            @Valid @RequestBody SprintCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        SprintResponse response = sprintService.createSprint(projectId, request, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets all sprints for a project.
     *
     * @param projectId the project ID
     * @param principal the authenticated user
     * @return list of sprints
     */
    @GetMapping
    @Operation(summary = "Get all sprints for a project")
    public ResponseEntity<List<SprintResponse>> getSprints(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(sprintService.getSprintsForProject(projectId, principal.getUserId()));
    }

    /**
     * Gets a sprint by ID.
     *
     * @param projectId the project ID
     * @param sprintId  the sprint ID
     * @param principal the authenticated user
     * @return the sprint
     */
    @GetMapping("/{sprintId}")
    @Operation(summary = "Get a sprint by ID")
    public ResponseEntity<SprintResponse> getSprint(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(sprintService.getSprint(projectId, sprintId, principal.getUserId()));
    }

    /**
     * Updates a sprint.
     *
     * @param projectId the project ID
     * @param sprintId  the sprint ID
     * @param request   the update request
     * @param principal the authenticated user
     * @return the updated sprint
     */
    @PutMapping("/{sprintId}")
    @Operation(summary = "Update a sprint")
    public ResponseEntity<SprintResponse> updateSprint(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @Valid @RequestBody SprintUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                sprintService.updateSprint(projectId, sprintId, request, principal.getUserId()));
    }

    /**
     * Deletes a sprint (soft delete).
     *
     * @param projectId the project ID
     * @param sprintId  the sprint ID
     * @param principal the authenticated user
     * @return no content
     */
    @DeleteMapping("/{sprintId}")
    @Operation(summary = "Delete a sprint")
    public ResponseEntity<Void> deleteSprint(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @AuthenticationPrincipal UserPrincipal principal) {
        sprintService.deleteSprint(projectId, sprintId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Starts a sprint (changes status to ACTIVE).
     *
     * @param projectId the project ID
     * @param sprintId  the sprint ID
     * @param principal the authenticated user
     * @return the updated sprint
     */
    @PatchMapping("/{sprintId}/start")
    @Operation(summary = "Start a sprint")
    public ResponseEntity<SprintResponse> startSprint(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(sprintService.startSprint(projectId, sprintId, principal.getUserId()));
    }

    /**
     * Completes a sprint (changes status to COMPLETED).
     *
     * @param projectId the project ID
     * @param sprintId  the sprint ID
     * @param principal the authenticated user
     * @return the updated sprint
     */
    @PatchMapping("/{sprintId}/complete")
    @Operation(summary = "Complete a sprint")
    public ResponseEntity<SprintResponse> completeSprint(
            @PathVariable Long projectId,
            @PathVariable Long sprintId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(sprintService.completeSprint(projectId, sprintId, principal.getUserId()));
    }
}

package com.board.controller;

import com.board.dto.epic.EpicCreateRequest;
import com.board.dto.epic.EpicResponse;
import com.board.dto.epic.EpicUpdateRequest;
import com.board.security.UserPrincipal;
import com.board.service.EpicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for epic operations.
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/epics")
@RequiredArgsConstructor
@Tag(name = "Epics", description = "Epic management endpoints")
public class EpicController {

    private final EpicService epicService;

    /**
     * Creates a new epic.
     *
     * @param projectId the project ID
     * @param request   the create request
     * @param principal the authenticated user
     * @return the created epic
     */
    @PostMapping
    @Operation(summary = "Create a new epic")
    public ResponseEntity<EpicResponse> createEpic(
            @PathVariable Long projectId,
            @Valid @RequestBody EpicCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        EpicResponse response = epicService.createEpic(projectId, request, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets all epics for a project.
     *
     * @param projectId the project ID
     * @param principal the authenticated user
     * @return list of epics
     */
    @GetMapping
    @Operation(summary = "Get all epics for a project")
    public ResponseEntity<List<EpicResponse>> getEpics(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(epicService.getEpicsForProject(projectId, principal.getUserId()));
    }

    /**
     * Gets an epic by ID.
     *
     * @param projectId the project ID
     * @param epicId    the epic ID
     * @param principal the authenticated user
     * @return the epic
     */
    @GetMapping("/{epicId}")
    @Operation(summary = "Get an epic by ID")
    public ResponseEntity<EpicResponse> getEpic(
            @PathVariable Long projectId,
            @PathVariable Long epicId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(epicService.getEpic(projectId, epicId, principal.getUserId()));
    }

    /**
     * Updates an epic.
     *
     * @param projectId the project ID
     * @param epicId    the epic ID
     * @param request   the update request
     * @param principal the authenticated user
     * @return the updated epic
     */
    @PutMapping("/{epicId}")
    @Operation(summary = "Update an epic")
    public ResponseEntity<EpicResponse> updateEpic(
            @PathVariable Long projectId,
            @PathVariable Long epicId,
            @Valid @RequestBody EpicUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                epicService.updateEpic(projectId, epicId, request, principal.getUserId()));
    }

    /**
     * Deletes an epic (soft delete).
     *
     * @param projectId the project ID
     * @param epicId    the epic ID
     * @param principal the authenticated user
     * @return no content
     */
    @DeleteMapping("/{epicId}")
    @Operation(summary = "Delete an epic")
    public ResponseEntity<Void> deleteEpic(
            @PathVariable Long projectId,
            @PathVariable Long epicId,
            @AuthenticationPrincipal UserPrincipal principal) {
        epicService.deleteEpic(projectId, epicId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}

package com.board.controller;

import com.board.dto.project.ProjectCreateRequest;
import com.board.dto.project.ProjectMemberRequest;
import com.board.dto.project.ProjectMemberResponse;
import com.board.dto.project.ProjectResponse;
import com.board.dto.project.ProjectUpdateRequest;
import com.board.security.UserPrincipal;
import com.board.service.ProjectService;
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
 * REST controller for project operations.
 */
@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project management endpoints")
public class ProjectController {

    private final ProjectService projectService;

    /**
     * Creates a new project.
     *
     * @param request   the create request
     * @param principal the authenticated user
     * @return the created project
     */
    @PostMapping
    @Operation(summary = "Create a new project")
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody ProjectCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        ProjectResponse response = projectService.createProject(request, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets all projects for the authenticated user.
     *
     * @param principal the authenticated user
     * @return list of projects
     */
    @GetMapping
    @Operation(summary = "Get all projects for the authenticated user")
    public ResponseEntity<List<ProjectResponse>> getProjects(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(projectService.getProjectsForUser(principal.getUserId()));
    }

    /**
     * Gets a project by ID.
     *
     * @param id        the project ID
     * @param principal the authenticated user
     * @return the project
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a project by ID")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(projectService.getProject(id, principal.getUserId()));
    }

    /**
     * Updates a project.
     *
     * @param id        the project ID
     * @param request   the update request
     * @param principal the authenticated user
     * @return the updated project
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update a project")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(projectService.updateProject(id, request, principal.getUserId()));
    }

    /**
     * Deletes a project (soft delete).
     *
     * @param id        the project ID
     * @param principal the authenticated user
     * @return no content
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a project")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        projectService.deleteProject(id, principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Adds a member to a project.
     *
     * @param id        the project ID
     * @param request   the member request
     * @param principal the authenticated user
     * @return the added member
     */
    @PostMapping("/{id}/members")
    @Operation(summary = "Add a member to a project")
    public ResponseEntity<ProjectMemberResponse> addMember(
            @PathVariable Long id,
            @Valid @RequestBody ProjectMemberRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        ProjectMemberResponse response = projectService.addMember(id, request, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets all members of a project.
     *
     * @param id        the project ID
     * @param principal the authenticated user
     * @return list of members
     */
    @GetMapping("/{id}/members")
    @Operation(summary = "Get all members of a project")
    public ResponseEntity<List<ProjectMemberResponse>> getMembers(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(projectService.getMembers(id, principal.getUserId()));
    }

    /**
     * Removes a member from a project.
     *
     * @param id        the project ID
     * @param userId    the member's user ID
     * @param principal the authenticated user
     * @return no content
     */
    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "Remove a member from a project")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal principal) {
        projectService.removeMember(id, userId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}

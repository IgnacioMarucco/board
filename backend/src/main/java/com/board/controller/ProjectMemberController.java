package com.board.controller;

import com.board.dto.projectmember.ProjectMemberInviteRequest;
import com.board.dto.projectmember.ProjectMemberResponse;
import com.board.dto.projectmember.ProjectMemberUpdateRequest;
import com.board.security.UserPrincipal;
import com.board.service.ProjectMemberService;
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
 * REST controller for project member operations.
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/members")
@RequiredArgsConstructor
@Tag(name = "Project Members", description = "Project member management endpoints")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    /**
     * Invites a user to a project.
     *
     * @param projectId the project ID
     * @param request   the invite request
     * @param principal the authenticated user
     * @return the created project member
     */
    @PostMapping
    @Operation(summary = "Invite a user to a project")
    public ResponseEntity<ProjectMemberResponse> inviteMember(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectMemberInviteRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        ProjectMemberResponse response = projectMemberService.inviteMember(projectId, request,
                principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets all members of a project.
     *
     * @param projectId the project ID
     * @param principal the authenticated user
     * @return list of project members
     */
    @GetMapping
    @Operation(summary = "Get all members of a project")
    public ResponseEntity<List<ProjectMemberResponse>> getProjectMembers(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(projectMemberService.getProjectMembers(projectId,
                principal.getUserId()));
    }

    /**
     * Gets a specific project member.
     *
     * @param projectId the project ID
     * @param memberId  the member ID
     * @param principal the authenticated user
     * @return the project member
     */
    @GetMapping("/{memberId}")
    @Operation(summary = "Get a project member by ID")
    public ResponseEntity<ProjectMemberResponse> getProjectMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(projectMemberService.getProjectMember(projectId, memberId,
                principal.getUserId()));
    }

    /**
     * Updates a project member's role.
     *
     * @param projectId the project ID
     * @param memberId  the member ID
     * @param request   the update request
     * @param principal the authenticated user
     * @return the updated project member
     */
    @PutMapping("/{memberId}")
    @Operation(summary = "Update a project member's role")
    public ResponseEntity<ProjectMemberResponse> updateMemberRole(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @Valid @RequestBody ProjectMemberUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(projectMemberService.updateMemberRole(projectId, memberId,
                request,
                principal.getUserId()));
    }

    /**
     * Removes a member from a project.
     *
     * @param projectId the project ID
     * @param memberId  the member ID
     * @param principal the authenticated user
     * @return no content
     */
    @DeleteMapping("/{memberId}")
    @Operation(summary = "Remove a member from a project")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserPrincipal principal) {
        projectMemberService.removeMember(projectId, memberId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}

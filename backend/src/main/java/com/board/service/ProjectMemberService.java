package com.board.service;

import com.board.dto.projectmember.ProjectMemberInviteRequest;
import com.board.dto.projectmember.ProjectMemberResponse;
import com.board.dto.projectmember.ProjectMemberUpdateRequest;

import java.util.List;

/**
 * Service interface for project member operations.
 */
public interface ProjectMemberService {

    /**
     * Invites a user to a project.
     *
     * @param projectId the project ID
     * @param request   the invite request
     * @param userId    the requesting user's ID (must be project owner)
     * @return the created project member
     */
    ProjectMemberResponse inviteMember(Long projectId, ProjectMemberInviteRequest request, Long userId);

    /**
     * Gets all members of a project.
     *
     * @param projectId the project ID
     * @param userId    the requesting user's ID
     * @return list of project members
     */
    List<ProjectMemberResponse> getProjectMembers(Long projectId, Long userId);

    /**
     * Gets a specific project member.
     *
     * @param projectId the project ID
     * @param memberId  the member ID
     * @param userId    the requesting user's ID
     * @return the project member
     */
    ProjectMemberResponse getProjectMember(Long projectId, Long memberId, Long userId);

    /**
     * Updates a project member's role.
     *
     * @param projectId the project ID
     * @param memberId  the member ID
     * @param request   the update request
     * @param userId    the requesting user's ID (must be project owner)
     * @return the updated project member
     */
    ProjectMemberResponse updateMemberRole(Long projectId, Long memberId,
            ProjectMemberUpdateRequest request, Long userId);

    /**
     * Removes a member from a project.
     *
     * @param projectId the project ID
     * @param memberId  the member ID
     * @param userId    the requesting user's ID (must be project owner)
     */
    void removeMember(Long projectId, Long memberId, Long userId);
}

package com.board.service;

import com.board.dto.project.ProjectCreateRequest;
import com.board.dto.project.ProjectMemberRequest;
import com.board.dto.project.ProjectMemberResponse;
import com.board.dto.project.ProjectResponse;
import com.board.dto.project.ProjectUpdateRequest;

import java.util.List;

/**
 * Service interface for project operations.
 */
public interface ProjectService {

    /**
     * Creates a new project.
     *
     * @param request the create request
     * @param ownerId the owner's user ID
     * @return the created project
     */
    ProjectResponse createProject(ProjectCreateRequest request, Long ownerId);

    /**
     * Gets a project by ID.
     *
     * @param projectId the project ID
     * @param userId    the requesting user's ID
     * @return the project
     */
    ProjectResponse getProject(Long projectId, Long userId);

    /**
     * Gets all projects for a user.
     *
     * @param userId the user's ID
     * @return list of projects
     */
    List<ProjectResponse> getProjectsForUser(Long userId);

    /**
     * Updates a project.
     *
     * @param projectId the project ID
     * @param request   the update request
     * @param userId    the requesting user's ID
     * @return the updated project
     */
    ProjectResponse updateProject(Long projectId, ProjectUpdateRequest request, Long userId);

    /**
     * Deletes a project (soft delete).
     *
     * @param projectId the project ID
     * @param userId    the requesting user's ID
     */
    void deleteProject(Long projectId, Long userId);

    /**
     * Adds a member to a project.
     *
     * @param projectId the project ID
     * @param request   the member request
     * @param userId    the requesting user's ID
     * @return the added member
     */
    ProjectMemberResponse addMember(Long projectId, ProjectMemberRequest request, Long userId);

    /**
     * Gets all members of a project.
     *
     * @param projectId the project ID
     * @param userId    the requesting user's ID
     * @return list of members
     */
    List<ProjectMemberResponse> getMembers(Long projectId, Long userId);

    /**
     * Removes a member from a project.
     *
     * @param projectId the project ID
     * @param memberId  the member's user ID
     * @param userId    the requesting user's ID
     */
    void removeMember(Long projectId, Long memberId, Long userId);
}

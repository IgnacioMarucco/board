package com.board.service;

import com.board.dto.sprint.SprintCreateRequest;
import com.board.dto.sprint.SprintResponse;
import com.board.dto.sprint.SprintSummaryResponse;
import com.board.dto.sprint.SprintUpdateRequest;

import java.util.List;

/**
 * Service interface for sprint operations.
 */
public interface SprintService {

    /**
     * Creates a new sprint.
     *
     * @param projectId the project ID
     * @param request   the create request
     * @param userId    the requesting user's ID
     * @return the created sprint
     */
    SprintResponse createSprint(Long projectId, SprintCreateRequest request, Long userId);

    /**
     * Gets a sprint by ID.
     *
     * @param projectId the project ID
     * @param sprintId  the sprint ID
     * @param userId    the requesting user's ID
     * @return the sprint
     */
    SprintResponse getSprint(Long projectId, Long sprintId, Long userId);

    /**
     * Gets all sprints for a project.
     *
     * @param projectId the project ID
     * @param userId    the requesting user's ID
     * @return list of sprints
     */
    List<SprintResponse> getSprintsForProject(Long projectId, Long userId);

    /**
     * Updates a sprint.
     *
     * @param projectId the project ID
     * @param sprintId  the sprint ID
     * @param request   the update request
     * @param userId    the requesting user's ID
     * @return the updated sprint
     */
    SprintResponse updateSprint(Long projectId, Long sprintId, SprintUpdateRequest request, Long userId);

    /**
     * Deletes a sprint (soft delete).
     *
     * @param projectId the project ID
     * @param sprintId  the sprint ID
     * @param userId    the requesting user's ID
     */
    void deleteSprint(Long projectId, Long sprintId, Long userId);

    /**
     * Starts a sprint (changes status to ACTIVE).
     *
     * @param projectId the project ID
     * @param sprintId  the sprint ID
     * @param userId    the requesting user's ID
     * @return the updated sprint
     */
    SprintResponse startSprint(Long projectId, Long sprintId, Long userId);

    /**
     * Completes a sprint (changes status to COMPLETED).
     *
     * @param projectId the project ID
     * @param sprintId  the sprint ID
     * @param userId    the requesting user's ID
     * @return the updated sprint
     */
    SprintResponse completeSprint(Long projectId, Long sprintId, Long userId);

    /**
     * Gets a summary of sprint metrics.
     *
     * @param projectId the project ID
     * @param sprintId  the sprint ID
     * @param userId    the requesting user's ID
     * @return sprint summary metrics
     */
    SprintSummaryResponse getSprintSummary(Long projectId, Long sprintId, Long userId);
}

package com.board.service;

import com.board.dto.epic.EpicCreateRequest;
import com.board.dto.epic.EpicResponse;
import com.board.dto.epic.EpicUpdateRequest;

import java.util.List;

/**
 * Service interface for epic operations.
 */
public interface EpicService {

    /**
     * Creates a new epic.
     *
     * @param projectId the project ID
     * @param request   the create request
     * @param userId    the requesting user's ID
     * @return the created epic
     */
    EpicResponse createEpic(Long projectId, EpicCreateRequest request, Long userId);

    /**
     * Gets an epic by ID.
     *
     * @param projectId the project ID
     * @param epicId    the epic ID
     * @param userId    the requesting user's ID
     * @return the epic
     */
    EpicResponse getEpic(Long projectId, Long epicId, Long userId);

    /**
     * Gets all epics for a project.
     *
     * @param projectId the project ID
     * @param userId    the requesting user's ID
     * @return list of epics
     */
    List<EpicResponse> getEpicsForProject(Long projectId, Long userId);

    /**
     * Updates an epic.
     *
     * @param projectId the project ID
     * @param epicId    the epic ID
     * @param request   the update request
     * @param userId    the requesting user's ID
     * @return the updated epic
     */
    EpicResponse updateEpic(Long projectId, Long epicId, EpicUpdateRequest request, Long userId);

    /**
     * Deletes an epic (soft delete).
     *
     * @param projectId the project ID
     * @param epicId    the epic ID
     * @param userId    the requesting user's ID
     */
    void deleteEpic(Long projectId, Long epicId, Long userId);
}

package com.board.service;

import com.board.dto.metrics.BurndownResponse;
import com.board.dto.metrics.VelocityResponse;

/**
 * Service interface for metrics operations.
 */
public interface MetricsService {

    /**
     * Gets burndown data for a sprint.
     *
     * @param projectId the project ID
     * @param sprintId  the sprint ID
     * @param userId    the requesting user's ID
     * @return burndown response
     */
    BurndownResponse getBurndown(Long projectId, Long sprintId, Long userId);

    /**
     * Gets velocity data for a project.
     *
     * @param projectId the project ID
     * @param userId    the requesting user's ID
     * @param limit     optional max number of sprints
     * @return velocity response
     */
    VelocityResponse getVelocity(Long projectId, Long userId, Integer limit);

    /**
     * Captures daily snapshots for active sprints.
     */
    void captureDailySnapshots();

    /**
     * Captures a snapshot for a sprint on a given date.
     *
     * @param sprintId  the sprint ID
     * @param projectId the project ID
     */
    void captureSprintSnapshot(Long projectId, Long sprintId);
}

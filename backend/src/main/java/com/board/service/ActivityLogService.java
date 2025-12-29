package com.board.service;

import com.board.dto.activity.ActivityLogResponse;
import com.board.entity.Project;
import com.board.entity.User;

import java.util.List;

/**
 * Service interface for activity log operations.
 */
public interface ActivityLogService {

    /**
     * Records a new activity entry.
     *
     * @param project    the project
     * @param actor      the user who triggered the action
     * @param entityType the entity type (e.g., STORY, TASK)
     * @param entityId   the entity ID
     * @param action     the action label
     * @param details    extra details about the change
     */
    void recordActivity(Project project, User actor, String entityType,
            Long entityId, String action, String details);

    /**
     * Gets activity entries for a project.
     *
     * @param projectId the project ID
     * @param userId    the requesting user ID
     * @return list of activity entries
     */
    List<ActivityLogResponse> getActivityForProject(Long projectId, Long userId);
}

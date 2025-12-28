package com.board.service;

import com.board.dto.task.TaskCreateRequest;
import com.board.dto.task.TaskResponse;
import com.board.dto.task.TaskUpdateRequest;

import java.util.List;

/**
 * Service interface for task operations.
 */
public interface TaskService {

    /**
     * Creates a new task.
     *
     * @param projectId the project ID
     * @param storyId   the story ID
     * @param request   the create request
     * @param userId    the requesting user's ID
     * @return the created task
     */
    TaskResponse createTask(Long projectId, Long storyId, TaskCreateRequest request, Long userId);

    /**
     * Gets a task by ID.
     *
     * @param projectId the project ID
     * @param storyId   the story ID
     * @param taskId    the task ID
     * @param userId    the requesting user's ID
     * @return the task
     */
    TaskResponse getTask(Long projectId, Long storyId, Long taskId, Long userId);

    /**
     * Gets all tasks for a story.
     *
     * @param projectId the project ID
     * @param storyId   the story ID
     * @param userId    the requesting user's ID
     * @return list of tasks
     */
    List<TaskResponse> getTasksForStory(Long projectId, Long storyId, Long userId);

    /**
     * Updates a task.
     *
     * @param projectId the project ID
     * @param storyId   the story ID
     * @param taskId    the task ID
     * @param request   the update request
     * @param userId    the requesting user's ID
     * @return the updated task
     */
    TaskResponse updateTask(Long projectId, Long storyId, Long taskId,
            TaskUpdateRequest request, Long userId);

    /**
     * Deletes a task (soft delete).
     *
     * @param projectId the project ID
     * @param storyId   the story ID
     * @param taskId    the task ID
     * @param userId    the requesting user's ID
     */
    void deleteTask(Long projectId, Long storyId, Long taskId, Long userId);

    /**
     * Marks a task as complete.
     *
     * @param projectId the project ID
     * @param storyId   the story ID
     * @param taskId    the task ID
     * @param userId    the requesting user's ID
     * @return the updated task
     */
    TaskResponse completeTask(Long projectId, Long storyId, Long taskId, Long userId);
}

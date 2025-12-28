package com.board.controller;

import com.board.dto.task.TaskCreateRequest;
import com.board.dto.task.TaskResponse;
import com.board.dto.task.TaskUpdateRequest;
import com.board.security.UserPrincipal;
import com.board.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for task operations.
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/stories/{storyId}/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management endpoints")
public class TaskController {

    private final TaskService taskService;

    /**
     * Creates a new task.
     *
     * @param projectId the project ID
     * @param storyId   the story ID
     * @param request   the create request
     * @param principal the authenticated user
     * @return the created task
     */
    @PostMapping
    @Operation(summary = "Create a new task")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable Long projectId,
            @PathVariable Long storyId,
            @Valid @RequestBody TaskCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        TaskResponse response = taskService.createTask(projectId, storyId, request,
                principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets all tasks for a story.
     *
     * @param projectId the project ID
     * @param storyId   the story ID
     * @param principal the authenticated user
     * @return list of tasks
     */
    @GetMapping
    @Operation(summary = "Get all tasks for a story")
    public ResponseEntity<List<TaskResponse>> getTasks(
            @PathVariable Long projectId,
            @PathVariable Long storyId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(taskService.getTasksForStory(projectId, storyId,
                principal.getUserId()));
    }

    /**
     * Gets a task by ID.
     *
     * @param projectId the project ID
     * @param storyId   the story ID
     * @param taskId    the task ID
     * @param principal the authenticated user
     * @return the task
     */
    @GetMapping("/{taskId}")
    @Operation(summary = "Get a task by ID")
    public ResponseEntity<TaskResponse> getTask(
            @PathVariable Long projectId,
            @PathVariable Long storyId,
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(taskService.getTask(projectId, storyId, taskId,
                principal.getUserId()));
    }

    /**
     * Updates a task.
     *
     * @param projectId the project ID
     * @param storyId   the story ID
     * @param taskId    the task ID
     * @param request   the update request
     * @param principal the authenticated user
     * @return the updated task
     */
    @PutMapping("/{taskId}")
    @Operation(summary = "Update a task")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long projectId,
            @PathVariable Long storyId,
            @PathVariable Long taskId,
            @Valid @RequestBody TaskUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                taskService.updateTask(projectId, storyId, taskId, request,
                        principal.getUserId()));
    }

    /**
     * Deletes a task (soft delete).
     *
     * @param projectId the project ID
     * @param storyId   the story ID
     * @param taskId    the task ID
     * @param principal the authenticated user
     * @return no content
     */
    @DeleteMapping("/{taskId}")
    @Operation(summary = "Delete a task")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long projectId,
            @PathVariable Long storyId,
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserPrincipal principal) {
        taskService.deleteTask(projectId, storyId, taskId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Marks a task as complete.
     *
     * @param projectId the project ID
     * @param storyId   the story ID
     * @param taskId    the task ID
     * @param principal the authenticated user
     * @return the updated task
     */
    @PatchMapping("/{taskId}/complete")
    @Operation(summary = "Mark a task as complete")
    public ResponseEntity<TaskResponse> completeTask(
            @PathVariable Long projectId,
            @PathVariable Long storyId,
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                taskService.completeTask(projectId, storyId, taskId, principal.getUserId()));
    }
}

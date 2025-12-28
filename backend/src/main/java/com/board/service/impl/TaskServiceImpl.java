package com.board.service.impl;

import com.board.dto.task.TaskCreateRequest;
import com.board.dto.task.TaskResponse;
import com.board.dto.task.TaskUpdateRequest;
import com.board.entity.Project;
import com.board.entity.Story;
import com.board.entity.Task;
import com.board.entity.User;
import com.board.entity.enums.TaskStatus;
import com.board.exception.BadRequestException;
import com.board.exception.ForbiddenException;
import com.board.exception.NotFoundException;
import com.board.mapper.TaskMapper;
import com.board.repository.ProjectMemberRepository;
import com.board.repository.ProjectRepository;
import com.board.repository.StoryRepository;
import com.board.repository.TaskRepository;
import com.board.repository.UserRepository;
import com.board.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of TaskService.
 */
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final StoryRepository storyRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;

    @Override
    @Transactional
    public TaskResponse createTask(Long projectId, Long storyId,
            TaskCreateRequest request, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        Story story = findStoryById(storyId);
        validateStoryBelongsToProject(story, project);

        if (taskRepository.findByKey(request.getKey()).isPresent()) {
            throw new BadRequestException("Task key already exists");
        }

        Task task = Task.builder()
                .key(request.getKey().toUpperCase())
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : TaskStatus.TODO)
                .estimatedHours(request.getEstimatedHours())
                .story(story)
                .build();

        if (request.getAssigneeId() != null) {
            User assignee = findUserById(request.getAssigneeId());
            validateMembership(project, assignee.getId());
            task.setAssignee(assignee);
        }

        task = taskRepository.save(task);
        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTask(Long projectId, Long storyId, Long taskId, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        Story story = findStoryById(storyId);
        validateStoryBelongsToProject(story, project);

        Task task = findTaskById(taskId);
        validateTaskBelongsToStory(task, story);

        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksForStory(Long projectId, Long storyId, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        Story story = findStoryById(storyId);
        validateStoryBelongsToProject(story, project);

        List<Task> tasks = taskRepository.findByStoryOrderByPositionAsc(story);
        return taskMapper.toResponseList(tasks);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long projectId, Long storyId, Long taskId,
            TaskUpdateRequest request, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        Story story = findStoryById(storyId);
        validateStoryBelongsToProject(story, project);

        Task task = findTaskById(taskId);
        validateTaskBelongsToStory(task, story);

        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getEstimatedHours() != null) {
            task.setEstimatedHours(request.getEstimatedHours());
        }

        if (request.getAssigneeId() != null) {
            User assignee = findUserById(request.getAssigneeId());
            validateMembership(project, assignee.getId());
            task.setAssignee(assignee);
        }

        task = taskRepository.save(task);
        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional
    public void deleteTask(Long projectId, Long storyId, Long taskId, Long userId) {
        Project project = findProjectById(projectId);
        validateOwnership(project, userId);

        Story story = findStoryById(storyId);
        validateStoryBelongsToProject(story, project);

        Task task = findTaskById(taskId);
        validateTaskBelongsToStory(task, story);

        task.softDelete();
        taskRepository.save(task);
    }

    @Override
    @Transactional
    public TaskResponse completeTask(Long projectId, Long storyId, Long taskId, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        Story story = findStoryById(storyId);
        validateStoryBelongsToProject(story, project);

        Task task = findTaskById(taskId);
        validateTaskBelongsToStory(task, story);

        if (task.getStatus() == TaskStatus.DONE) {
            throw new BadRequestException("Task is already completed");
        }

        task.setStatus(TaskStatus.DONE);
        task = taskRepository.save(task);

        return taskMapper.toResponse(task);
    }

    private Project findProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Project not found"));
    }

    private Story findStoryById(Long storyId) {
        return storyRepository.findById(storyId)
                .filter(s -> s.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Story not found"));
    }

    private Task findTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Task not found"));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private void validateMembership(Project project, Long userId) {
        User user = findUserById(userId);
        if (!projectMemberRepository.existsByProjectAndUser(project, user)) {
            throw new ForbiddenException("You are not a member of this project");
        }
    }

    private void validateOwnership(Project project, Long userId) {
        if (!project.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Only the project owner can perform this action");
        }
    }

    private void validateStoryBelongsToProject(Story story, Project project) {
        if (story.getEpic() == null
                || !story.getEpic().getProject().getId().equals(project.getId())) {
            throw new NotFoundException("Story not found in this project");
        }
    }

    private void validateTaskBelongsToStory(Task task, Story story) {
        if (!task.getStory().getId().equals(story.getId())) {
            throw new NotFoundException("Task not found in this story");
        }
    }
}

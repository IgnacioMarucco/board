package com.board.service.impl;

import com.board.dto.task.TaskCreateRequest;
import com.board.dto.task.TaskResponse;
import com.board.dto.task.TaskUpdateRequest;
import com.board.entity.BaseEntity;
import com.board.entity.Project;
import com.board.entity.Story;
import com.board.entity.Task;
import com.board.entity.User;
import com.board.entity.enums.Role;
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
import com.board.service.ActivityLogService;
import com.board.service.NotificationService;
import com.board.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

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
    private final NotificationService notificationService;
    private final ActivityLogService activityLogService;

    @Override
    @Transactional
    public TaskResponse createTask(Long projectId, Long storyId,
            TaskCreateRequest request, Long userId) {
        Project project = findProjectById(projectId);
        User actor = findUserById(userId);
        validateRole(project, actor, Role.DEVELOPER);

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

        task.setPosition(calculateNextPosition(story));

        task = taskRepository.save(task);
        notifyAssignmentIfChanged(actor, task.getAssignee(), null, task);
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
        User actor = findUserById(userId);
        validateRole(project, actor, Role.DEVELOPER);

        Story story = findStoryById(storyId);
        validateStoryBelongsToProject(story, project);

        Task task = findTaskById(taskId);
        validateTaskBelongsToStory(task, story);

        TaskStatus oldStatus = task.getStatus();
        User oldAssignee = task.getAssignee();

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

        if (request.getPosition() != null) {
            reorderTasks(story, task, request.getPosition());
        }

        task = taskRepository.save(task);
        recordTaskActivityChanges(project, actor, task, oldStatus, oldAssignee);
        notifyAssignmentIfChanged(actor, task.getAssignee(), oldAssignee, task);
        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional
    public void deleteTask(Long projectId, Long storyId, Long taskId, Long userId) {
        Project project = findProjectById(projectId);
        User actor = findUserById(userId);
        validateRole(project, actor, Role.DEVELOPER);

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
        User actor = findUserById(userId);
        validateRole(project, actor, Role.DEVELOPER);

        Story story = findStoryById(storyId);
        validateStoryBelongsToProject(story, project);

        Task task = findTaskById(taskId);
        validateTaskBelongsToStory(task, story);

        if (task.getStatus() == TaskStatus.DONE) {
            throw new BadRequestException("Task is already completed");
        }

        TaskStatus oldStatus = task.getStatus();
        task.setStatus(TaskStatus.DONE);
        task = taskRepository.save(task);
        recordTaskStatusChange(project, actor, task, oldStatus);

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
        if (!projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(project, user)) {
            throw new ForbiddenException("You are not a member of this project");
        }
    }

    private void validateRole(Project project, User user, Role... allowedRoles) {
        Role role = projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(project, user)
                .map(com.board.entity.ProjectMember::getRole)
                .orElseThrow(() -> new ForbiddenException("You are not a member of this project"));

        for (Role allowed : allowedRoles) {
            if (allowed == role) {
                return;
            }
        }
        throw new ForbiddenException("You are not allowed to perform this action");
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

    private int calculateNextPosition(Story story) {
        return (int) taskRepository.findByStoryOrderByPositionAsc(story).stream()
                .filter(Task::isActive)
                .count();
    }

    private void reorderTasks(Story story, Task task, Integer position) {
        List<Task> tasks = taskRepository.findByStoryOrderByPositionAsc(story).stream()
                .filter(Task::isActive)
                .filter(existing -> !existing.getId().equals(task.getId()))
                .toList();

        List<Task> reordered = new java.util.ArrayList<>(tasks);
        if (position < 0 || position > reordered.size()) {
            reordered.add(task);
        } else {
            reordered.add(position, task);
        }

        for (int i = 0; i < reordered.size(); i++) {
            reordered.get(i).setPosition(i);
        }

        taskRepository.saveAll(reordered);
        task.setPosition(reordered.indexOf(task));
    }

    private void recordTaskActivityChanges(Project project, User actor, Task task,
            TaskStatus oldStatus, User oldAssignee) {
        if (oldStatus != task.getStatus()) {
            recordTaskStatusChange(project, actor, task, oldStatus);
        }
        if (!Objects.equals(idOf(oldAssignee), idOf(task.getAssignee()))) {
            String details = "from=" + valueOrEmpty(nameOrNull(oldAssignee))
                    + ",to=" + valueOrEmpty(nameOrNull(task.getAssignee()));
            activityLogService.recordActivity(project, actor, "TASK",
                    task.getId(), "ASSIGNEE_CHANGED", details);
        }
    }

    private void recordTaskStatusChange(Project project, User actor, Task task, TaskStatus oldStatus) {
        String details = "from=" + oldStatus + ",to=" + task.getStatus();
        activityLogService.recordActivity(project, actor, "TASK",
                task.getId(), "STATUS_CHANGED", details);
    }

    private void notifyAssignmentIfChanged(User actor, User assignee,
            User previousAssignee, Task task) {
        if (assignee == null || Objects.equals(idOf(assignee), idOf(previousAssignee))) {
            return;
        }
        if (actor != null && Objects.equals(idOf(actor), idOf(assignee))) {
            return;
        }
        notificationService.createNotification(
                assignee,
                actor,
                "ASSIGNMENT",
                "Assigned to Task " + task.getKey(),
                resolveDisplayName(actor) + " assigned you to task " + task.getKey() + ".",
                null);
    }

    private Long idOf(BaseEntity entity) {
        return entity == null ? null : entity.getId();
    }

    private String nameOrNull(User user) {
        return user == null ? null : resolveDisplayName(user);
    }

    private String resolveDisplayName(User user) {
        if (user == null) {
            return "";
        }
        if (user.getUsername() != null && !user.getUsername().isBlank()) {
            return user.getUsername();
        }
        return user.getEmail();
    }

    private String valueOrEmpty(Object value) {
        return value == null ? "" : value.toString();
    }
}

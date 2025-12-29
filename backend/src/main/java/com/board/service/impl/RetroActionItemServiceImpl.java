package com.board.service.impl;

import com.board.dto.retroactionitem.RetroActionItemConvertRequest;
import com.board.dto.retroactionitem.RetroActionItemCreateRequest;
import com.board.dto.retroactionitem.RetroActionItemResponse;
import com.board.dto.retroactionitem.RetroActionItemUpdateRequest;
import com.board.entity.Ceremony;
import com.board.entity.Project;
import com.board.entity.RetroActionItem;
import com.board.entity.RetroItem;
import com.board.entity.Story;
import com.board.entity.Task;
import com.board.entity.User;
import com.board.entity.enums.RetroActionItemStatus;
import com.board.entity.enums.Role;
import com.board.entity.enums.TaskStatus;
import com.board.exception.BadRequestException;
import com.board.exception.ForbiddenException;
import com.board.exception.NotFoundException;
import com.board.mapper.RetroActionItemMapper;
import com.board.repository.CeremonyRepository;
import com.board.repository.ProjectMemberRepository;
import com.board.repository.RetroActionItemRepository;
import com.board.repository.RetroItemRepository;
import com.board.repository.StoryRepository;
import com.board.repository.TaskRepository;
import com.board.repository.UserRepository;
import com.board.service.RetroActionItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of RetroActionItemService.
 */
@Service
@RequiredArgsConstructor
public class RetroActionItemServiceImpl implements RetroActionItemService {

    private final RetroActionItemRepository actionItemRepository;
    private final CeremonyRepository ceremonyRepository;
    private final RetroItemRepository retroItemRepository;
    private final StoryRepository storyRepository;
    private final TaskRepository taskRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final RetroActionItemMapper retroActionItemMapper;

    @Override
    @Transactional
    public RetroActionItemResponse createActionItem(Long ceremonyId,
            RetroActionItemCreateRequest request, Long userId) {
        Ceremony ceremony = findCeremonyById(ceremonyId);
        validateMembership(ceremony.getSprint().getProject(), userId);
        validateRetrospective(ceremony);

        RetroItem retroItem = null;
        if (request.getRetroItemId() != null) {
            retroItem = findRetroItemById(request.getRetroItemId());
            if (!retroItem.getCeremony().getId().equals(ceremony.getId())) {
                throw new BadRequestException("Retro item does not belong to this ceremony");
            }
        }

        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = findUserById(request.getAssigneeId());
            validateMembership(ceremony.getSprint().getProject(), assignee.getId());
        }

        RetroActionItem actionItem = RetroActionItem.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : RetroActionItemStatus.OPEN)
                .dueDate(request.getDueDate())
                .assignee(assignee)
                .ceremony(ceremony)
                .retroItem(retroItem)
                .build();

        actionItem = actionItemRepository.save(actionItem);
        return retroActionItemMapper.toResponse(actionItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RetroActionItemResponse> getActionItemsForCeremony(Long ceremonyId, Long userId) {
        Ceremony ceremony = findCeremonyById(ceremonyId);
        validateMembership(ceremony.getSprint().getProject(), userId);
        validateRetrospective(ceremony);

        List<RetroActionItem> items = actionItemRepository.findByCeremonyOrderByCreatedAtAsc(ceremony);
        return retroActionItemMapper.toResponseList(items);
    }

    @Override
    @Transactional
    public RetroActionItemResponse updateActionItem(Long actionItemId,
            RetroActionItemUpdateRequest request, Long userId) {
        RetroActionItem actionItem = findActionItemById(actionItemId);
        validateMembership(actionItem.getCeremony().getSprint().getProject(), userId);

        if (request.getTitle() != null) {
            actionItem.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            actionItem.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            actionItem.setStatus(request.getStatus());
        }
        if (request.getDueDate() != null) {
            actionItem.setDueDate(request.getDueDate());
        }
        if (request.getAssigneeId() != null) {
            User assignee = findUserById(request.getAssigneeId());
            validateMembership(actionItem.getCeremony().getSprint().getProject(), assignee.getId());
            actionItem.setAssignee(assignee);
        }

        actionItem = actionItemRepository.save(actionItem);
        return retroActionItemMapper.toResponse(actionItem);
    }

    @Override
    @Transactional
    public RetroActionItemResponse convertToTask(Long actionItemId,
            RetroActionItemConvertRequest request, Long userId) {
        RetroActionItem actionItem = findActionItemById(actionItemId);
        Project project = actionItem.getCeremony().getSprint().getProject();
        requireRole(project, userId, Role.DEVELOPER);

        if (actionItem.getTask() != null) {
            throw new BadRequestException("Action item is already linked to a task");
        }

        Story story = findStoryById(request.getStoryId());
        Project storyProject = resolveProject(story);
        if (!storyProject.getId().equals(project.getId())) {
            throw new BadRequestException("Story does not belong to the same project");
        }

        if (taskRepository.findByKey(request.getKey()).isPresent()) {
            throw new BadRequestException("Task key already exists");
        }

        Task task = Task.builder()
                .key(request.getKey().toUpperCase())
                .title(request.getTitle())
                .description(request.getDescription())
                .status(TaskStatus.TODO)
                .estimatedHours(request.getEstimatedHours())
                .story(story)
                .build();

        if (request.getAssigneeId() != null) {
            User assignee = findUserById(request.getAssigneeId());
            validateMembership(project, assignee.getId());
            task.setAssignee(assignee);
        }

        task.setPosition(calculateNextTaskPosition(story));
        task = taskRepository.save(task);

        actionItem.setTask(task);
        actionItem = actionItemRepository.save(actionItem);
        return retroActionItemMapper.toResponse(actionItem);
    }

    @Override
    @Transactional
    public void deleteActionItem(Long actionItemId, Long userId) {
        RetroActionItem actionItem = findActionItemById(actionItemId);
        validateMembership(actionItem.getCeremony().getSprint().getProject(), userId);

        actionItem.softDelete();
        actionItemRepository.save(actionItem);
    }

    private RetroActionItem findActionItemById(Long actionItemId) {
        return actionItemRepository.findById(actionItemId)
                .filter(item -> item.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Retro action item not found"));
    }

    private Ceremony findCeremonyById(Long ceremonyId) {
        return ceremonyRepository.findById(ceremonyId)
                .filter(ceremony -> ceremony.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Ceremony not found"));
    }

    private RetroItem findRetroItemById(Long retroItemId) {
        return retroItemRepository.findById(retroItemId)
                .filter(item -> item.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Retro item not found"));
    }

    private Story findStoryById(Long storyId) {
        return storyRepository.findById(storyId)
                .filter(story -> story.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Story not found"));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private Project resolveProject(Story story) {
        if (story.getEpic() != null) {
            return story.getEpic().getProject();
        }
        if (story.getSprint() != null) {
            return story.getSprint().getProject();
        }
        throw new NotFoundException("Story is not linked to a project");
    }

    private void validateRetrospective(Ceremony ceremony) {
        if (!ceremony.isRetrospective()) {
            throw new BadRequestException("Action items can only be used for retrospectives");
        }
    }

    private void validateMembership(Project project, Long userId) {
        User user = findUserById(userId);
        if (!projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(project, user)) {
            throw new ForbiddenException("You are not a member of this project");
        }
    }

    private void requireRole(Project project, Long userId, Role... allowedRoles) {
        User user = findUserById(userId);
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

    private int calculateNextTaskPosition(Story story) {
        return (int) taskRepository.findByStoryOrderByPositionAsc(story).stream()
                .filter(Task::isActive)
                .count();
    }
}

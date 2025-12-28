package com.board.service.impl;

import com.board.dto.story.StoryAssignRequest;
import com.board.dto.story.StoryCreateRequest;
import com.board.dto.story.StoryResponse;
import com.board.dto.story.StoryUpdateRequest;
import com.board.entity.Epic;
import com.board.entity.Project;
import com.board.entity.Sprint;
import com.board.entity.Story;
import com.board.entity.User;
import com.board.entity.enums.Priority;
import com.board.entity.enums.StoryStatus;
import com.board.exception.BadRequestException;
import com.board.exception.ForbiddenException;
import com.board.exception.NotFoundException;
import com.board.mapper.StoryMapper;
import com.board.repository.EpicRepository;
import com.board.repository.ProjectMemberRepository;
import com.board.repository.ProjectRepository;
import com.board.repository.SprintRepository;
import com.board.repository.StoryRepository;
import com.board.repository.UserRepository;
import com.board.service.StoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of StoryService.
 */
@Service
@RequiredArgsConstructor
public class StoryServiceImpl implements StoryService {

    private final StoryRepository storyRepository;
    private final ProjectRepository projectRepository;
    private final EpicRepository epicRepository;
    private final SprintRepository sprintRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final StoryMapper storyMapper;

    @Override
    @Transactional
    public StoryResponse createStory(Long projectId, StoryCreateRequest request, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        if (storyRepository.findByKey(request.getKey()).isPresent()) {
            throw new BadRequestException("Story key already exists");
        }

        Story story = Story.builder()
                .key(request.getKey().toUpperCase())
                .title(request.getTitle())
                .description(request.getDescription())
                .acceptanceCriteria(request.getAcceptanceCriteria())
                .status(request.getStatus() != null ? request.getStatus() : StoryStatus.BACKLOG)
                .priority(request.getPriority() != null ? request.getPriority() : Priority.MEDIUM)
                .storyPoints(request.getStoryPoints())
                .build();

        if (request.getEpicId() != null) {
            Epic epic = findEpicById(request.getEpicId());
            validateEpicBelongsToProject(epic, project);
            story.setEpic(epic);
        }

        if (request.getSprintId() != null) {
            Sprint sprint = findSprintById(request.getSprintId());
            validateSprintBelongsToProject(sprint, project);
            story.setSprint(sprint);
        }

        if (request.getAssigneeId() != null) {
            User assignee = findUserById(request.getAssigneeId());
            validateMembership(project, assignee.getId());
            story.setAssignee(assignee);
        }

        story = storyRepository.save(story);
        return storyMapper.toResponse(story);
    }

    @Override
    @Transactional(readOnly = true)
    public StoryResponse getStory(Long projectId, Long storyId, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        Story story = findStoryById(storyId);
        validateStoryBelongsToProject(story, project);

        return storyMapper.toResponse(story);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StoryResponse> getStoriesForProject(Long projectId, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        List<Story> stories = storyRepository.findByEpicProjectOrderByPositionAsc(project);
        return storyMapper.toResponseList(stories);
    }

    @Override
    @Transactional
    public StoryResponse updateStory(Long projectId, Long storyId,
            StoryUpdateRequest request, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        Story story = findStoryById(storyId);
        validateStoryBelongsToProject(story, project);

        if (request.getTitle() != null) {
            story.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            story.setDescription(request.getDescription());
        }
        if (request.getAcceptanceCriteria() != null) {
            story.setAcceptanceCriteria(request.getAcceptanceCriteria());
        }
        if (request.getStatus() != null) {
            story.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            story.setPriority(request.getPriority());
        }
        if (request.getStoryPoints() != null) {
            story.setStoryPoints(request.getStoryPoints());
        }

        if (request.getEpicId() != null) {
            Epic epic = findEpicById(request.getEpicId());
            validateEpicBelongsToProject(epic, project);
            story.setEpic(epic);
        }

        if (request.getSprintId() != null) {
            Sprint sprint = findSprintById(request.getSprintId());
            validateSprintBelongsToProject(sprint, project);
            story.setSprint(sprint);
        }

        if (request.getAssigneeId() != null) {
            User assignee = findUserById(request.getAssigneeId());
            validateMembership(project, assignee.getId());
            story.setAssignee(assignee);
        }

        story = storyRepository.save(story);
        return storyMapper.toResponse(story);
    }

    @Override
    @Transactional
    public void deleteStory(Long projectId, Long storyId, Long userId) {
        Project project = findProjectById(projectId);
        validateOwnership(project, userId);

        Story story = findStoryById(storyId);
        validateStoryBelongsToProject(story, project);

        story.softDelete();
        storyRepository.save(story);
    }

    @Override
    @Transactional
    public StoryResponse assignStory(Long projectId, Long storyId,
            StoryAssignRequest request, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        Story story = findStoryById(storyId);
        validateStoryBelongsToProject(story, project);

        User assignee = findUserById(request.getAssigneeId());
        validateMembership(project, assignee.getId());

        story.setAssignee(assignee);
        story = storyRepository.save(story);

        return storyMapper.toResponse(story);
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

    private Epic findEpicById(Long epicId) {
        return epicRepository.findById(epicId)
                .filter(e -> e.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Epic not found"));
    }

    private Sprint findSprintById(Long sprintId) {
        return sprintRepository.findById(sprintId)
                .filter(s -> s.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Sprint not found"));
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

    private void validateEpicBelongsToProject(Epic epic, Project project) {
        if (!epic.getProject().getId().equals(project.getId())) {
            throw new NotFoundException("Epic not found in this project");
        }
    }

    private void validateSprintBelongsToProject(Sprint sprint, Project project) {
        if (!sprint.getProject().getId().equals(project.getId())) {
            throw new NotFoundException("Sprint not found in this project");
        }
    }
}

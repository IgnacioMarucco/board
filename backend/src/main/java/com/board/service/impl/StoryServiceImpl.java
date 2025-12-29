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
import com.board.entity.BoardColumn;
import com.board.entity.enums.Priority;
import com.board.entity.enums.Role;
import com.board.entity.enums.StoryStatus;
import com.board.entity.enums.SprintStatus;
import com.board.exception.BadRequestException;
import com.board.exception.ForbiddenException;
import com.board.exception.NotFoundException;
import com.board.mapper.StoryMapper;
import com.board.repository.BoardColumnRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Implementation of StoryService.
 */
@Service
@RequiredArgsConstructor
public class StoryServiceImpl implements StoryService {

    private static final Set<Integer> FIBONACCI_POINTS = Set.of(1, 2, 3, 5, 8, 13, 21, 40, 100);

    private final StoryRepository storyRepository;
    private final ProjectRepository projectRepository;
    private final EpicRepository epicRepository;
    private final SprintRepository sprintRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final StoryMapper storyMapper;

    @Override
    @Transactional
    public StoryResponse createStory(Long projectId, StoryCreateRequest request, Long userId) {
        Project project = findProjectById(projectId);
        requireRole(getRoleForProject(project, userId), Role.PRODUCT_OWNER);

        if (storyRepository.findByKey(request.getKey()).isPresent()) {
            throw new BadRequestException("Story key already exists");
        }

        if (request.getStoryPoints() != null && !FIBONACCI_POINTS.contains(request.getStoryPoints())) {
            throw new BadRequestException("Story points must follow the Fibonacci scale");
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
            validateBelongsToProject(project, epic.getProject(), "Epic not found in this project");
            story.setEpic(epic);
        }

        applySprintAssignmentForCreate(project, story, request);

        if (request.getAssigneeId() != null) {
            User assignee = findUserById(request.getAssigneeId());
            validateMembership(project, assignee.getId());
            story.setAssignee(assignee);
        }

        story.setPosition(calculateInitialPosition(project, story.getSprint(), story.getBoardColumn()));

        story = storyRepository.save(story);
        return storyMapper.toResponse(story);
    }

    @Override
    @Transactional(readOnly = true)
    public StoryResponse getStory(Long projectId, Long storyId, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        Story story = findStoryById(storyId);
        validateBelongsToProject(project,
                story.getEpic() != null ? story.getEpic().getProject() : null,
                "Story not found in this project");

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
        Role role = getRoleForProject(project, userId);

        Story story = findStoryById(storyId);
        validateBelongsToProject(project,
                story.getEpic() != null ? story.getEpic().getProject() : null,
                "Story not found in this project");

        applyContentUpdates(request, story, role, project, userId);
        applyScopeUpdates(request, story, role, project);

        story = storyRepository.save(story);
        return storyMapper.toResponse(story);
    }

    @Override
    @Transactional
    public void deleteStory(Long projectId, Long storyId, Long userId) {
        Project project = findProjectById(projectId);
        requireRole(getRoleForProject(project, userId), Role.PRODUCT_OWNER);

        Story story = findStoryById(storyId);
        validateBelongsToProject(project,
                story.getEpic() != null ? story.getEpic().getProject() : null,
                "Story not found in this project");

        story.softDelete();
        storyRepository.save(story);
    }

    @Override
    @Transactional
    public StoryResponse assignStory(Long projectId, Long storyId,
            StoryAssignRequest request, Long userId) {
        Project project = findProjectById(projectId);
        Role role = getRoleForProject(project, userId);

        Story story = findStoryById(storyId);
        validateBelongsToProject(project,
                story.getEpic() != null ? story.getEpic().getProject() : null,
                "Story not found in this project");

        if (role == Role.DEVELOPER && !request.getAssigneeId().equals(userId)) {
            throw new ForbiddenException("Developers can only assign themselves");
        }

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
        if (!projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(project, user)) {
            throw new ForbiddenException("You are not a member of this project");
        }
    }

    private Role getRoleForProject(Project project, Long userId) {
        User user = findUserById(userId);
        return projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(project, user)
                .map(com.board.entity.ProjectMember::getRole)
                .orElseThrow(() -> new ForbiddenException("You are not a member of this project"));
    }

    private void validateBelongsToProject(Project project, Project candidateProject, String message) {
        if (candidateProject == null || !candidateProject.getId().equals(project.getId())) {
            throw new NotFoundException(message);
        }
    }

    private void requireRole(Role role, Role... allowedRoles) {
        for (Role allowed : allowedRoles) {
            if (allowed == role) {
                return;
            }
        }
        throw new ForbiddenException("You are not allowed to perform this action");
    }

    private void applyContentUpdates(StoryUpdateRequest request, Story story, Role role,
            Project project, Long userId) {
        if (request.getTitle() != null || request.getDescription() != null
                || request.getAcceptanceCriteria() != null || request.getPriority() != null) {
            requireRole(role, Role.PRODUCT_OWNER);
        }
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
            validateBoardMovePermissions(story, role);
            story.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            story.setPriority(request.getPriority());
        }
        if (request.getStoryPoints() != null) {
            requireRole(role, Role.PRODUCT_OWNER, Role.DEVELOPER);
            if (!FIBONACCI_POINTS.contains(request.getStoryPoints())) {
                throw new BadRequestException("Story points must follow the Fibonacci scale");
            }
            story.setStoryPoints(request.getStoryPoints());
            if (story.getSprint() != null) {
                validateSprintCapacity(story.getSprint(), story);
            }
        }
        if (request.getAssigneeId() != null) {
            if (role == Role.DEVELOPER && !request.getAssigneeId().equals(userId)) {
                throw new ForbiddenException("Developers can only assign themselves");
            }
            User assignee = findUserById(request.getAssigneeId());
            validateMembership(project, assignee.getId());
            story.setAssignee(assignee);
        }
    }

    private void applyScopeUpdates(StoryUpdateRequest request, Story story, Role role,
            Project project) {
        if (request.getEpicId() != null || request.getSprintId() != null) {
            requireRole(role, Role.PRODUCT_OWNER);
        }
        if (request.getEpicId() != null) {
            Epic epic = findEpicById(request.getEpicId());
            validateBelongsToProject(project, epic.getProject(), "Epic not found in this project");
            story.setEpic(epic);
        }
        if (request.getSprintId() != null) {
            Sprint sprint = findSprintById(request.getSprintId());
            validateBelongsToProject(project, sprint.getProject(), "Sprint not found in this project");
            handleSprintChange(project, story, sprint, request.getSprintChangeReason());
        }
        if (request.getBoardColumnId() != null || request.getPosition() != null) {
            validateBoardMovePermissions(story, role);
            if (story.getSprint() != null) {
                handleBoardMove(project, story, request.getBoardColumnId(), request.getPosition());
                return;
            }
            if (request.getBoardColumnId() != null) {
                throw new BadRequestException("Board columns are only available for sprint stories");
            }
            handleBacklogReorder(project, story, request.getPosition());
        }
    }

    private void validateBoardMovePermissions(Story story, Role role) {
        if (story.getSprint() == null) {
            requireRole(role, Role.PRODUCT_OWNER);
            return;
        }
        requireRole(role, Role.DEVELOPER, Role.PRODUCT_OWNER);
    }

    private BoardColumn findFirstColumnForProject(Project project) {
        return boardColumnRepository.findByProjectOrderByPositionAsc(project).stream()
                .filter(BoardColumn::isActive)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Board columns not found for project"));
    }

    private int calculateInitialPosition(Project project, Sprint sprint, BoardColumn column) {
        List<Story> scopeStories;
        if (sprint == null) {
            scopeStories = storyRepository.findByEpicProjectAndSprintIsNullOrderByPositionAsc(project);
        } else {
            scopeStories = storyRepository.findBySprintAndBoardColumnOrderByPositionAsc(sprint, column);
        }
        return (int) scopeStories.stream().filter(Story::isActive).count();
    }

    private void applySprintAssignmentForCreate(Project project, Story story, StoryCreateRequest request) {
        if (request.getSprintId() == null) {
            return;
        }

        Sprint sprint = findSprintById(request.getSprintId());
        validateBelongsToProject(project, sprint.getProject(), "Sprint not found in this project");
        if (sprint.getStatus() == SprintStatus.COMPLETED) {
            throw new BadRequestException("Cannot add stories to a completed sprint");
        }
        if (sprint.getStatus() == SprintStatus.ACTIVE
                && (request.getSprintChangeReason() == null
                || request.getSprintChangeReason().isBlank())) {
            throw new BadRequestException("Sprint scope is locked; provide a reason to add stories");
        }
        story.setSprint(sprint);
        story.setBoardColumn(findFirstColumnForProject(project));
        validateSprintCapacity(sprint, story);
        story.setSprintAddedAt(LocalDateTime.now());
        if (sprint.getStatus() == SprintStatus.ACTIVE) {
            sprint.setScopeChangeCount(sprint.getScopeChangeCount() + 1);
        }
    }

    private void handleSprintChange(Project project, Story story, Sprint newSprint, String reason) {
        Sprint oldSprint = story.getSprint();
        BoardColumn oldColumn = story.getBoardColumn();

        if (oldSprint != null && oldSprint.getId().equals(newSprint.getId())) {
            return;
        }

        if (oldSprint != null && oldColumn != null) {
            reindexStoriesAfterRemoval(oldSprint, oldColumn, story);
        } else if (oldSprint == null) {
            reindexBacklogAfterRemoval(project, story);
        }

        story.setSprint(newSprint);
        if (newSprint.getStatus() == SprintStatus.COMPLETED) {
            throw new BadRequestException("Cannot add stories to a completed sprint");
        }
        if (newSprint.getStatus() == SprintStatus.ACTIVE
                && (reason == null || reason.isBlank())) {
            throw new BadRequestException("Sprint scope is locked; provide a reason to add stories");
        }
        BoardColumn firstColumn = findFirstColumnForProject(project);
        validateWipLimit(newSprint, firstColumn, story);
        validateSprintCapacity(newSprint, story);
        story.setBoardColumn(firstColumn);
        story.setPosition(calculateInitialPosition(project, newSprint, firstColumn));
        story.setSprintAddedAt(LocalDateTime.now());
        if (newSprint.getStatus() == SprintStatus.ACTIVE) {
            newSprint.setScopeChangeCount(newSprint.getScopeChangeCount() + 1);
        }
    }

    private void handleBoardMove(Project project, Story story, Long boardColumnId,
            Integer position) {
        BoardColumn targetColumn = story.getBoardColumn();
        if (boardColumnId != null) {
            targetColumn = findBoardColumn(boardColumnId, project);
        }

        if (targetColumn == null) {
            throw new NotFoundException("Board column not found");
        }

        validateWipLimit(story.getSprint(), targetColumn, story);

        BoardColumn currentColumn = story.getBoardColumn();
        if (currentColumn != null && !currentColumn.getId().equals(targetColumn.getId())) {
            reindexStoriesAfterRemoval(story.getSprint(), currentColumn, story);
        }

        story.setBoardColumn(targetColumn);
        reorderStories(story.getSprint(), targetColumn, story, position);
    }

    private void handleBacklogReorder(Project project, Story story, Integer position) {
        List<Story> backlog = storyRepository.findByEpicProjectAndSprintIsNullOrderByPositionAsc(project)
                .stream()
                .filter(Story::isActive)
                .filter(existing -> !existing.getId().equals(story.getId()))
                .toList();

        List<Story> reordered = new java.util.ArrayList<>(backlog);
        if (position == null || position < 0 || position > reordered.size()) {
            reordered.add(story);
        } else {
            reordered.add(position, story);
        }

        for (int i = 0; i < reordered.size(); i++) {
            reordered.get(i).setPosition(i);
        }

        storyRepository.saveAll(reordered);
        story.setPosition(reordered.indexOf(story));
    }

    private void reindexBacklogAfterRemoval(Project project, Story story) {
        List<Story> backlog = storyRepository.findByEpicProjectAndSprintIsNullOrderByPositionAsc(project)
                .stream()
                .filter(Story::isActive)
                .filter(existing -> !existing.getId().equals(story.getId()))
                .toList();

        for (int i = 0; i < backlog.size(); i++) {
            backlog.get(i).setPosition(i);
        }

        storyRepository.saveAll(backlog);
    }

    private void reorderStories(Sprint sprint, BoardColumn column, Story story, Integer position) {
        List<Story> stories = storyRepository.findBySprintAndBoardColumnOrderByPositionAsc(sprint, column)
                .stream()
                .filter(Story::isActive)
                .filter(existing -> !existing.getId().equals(story.getId()))
                .toList();

        List<Story> reordered = new java.util.ArrayList<>(stories);
        if (position == null || position < 0 || position > reordered.size()) {
            reordered.add(story);
        } else {
            reordered.add(position, story);
        }

        for (int i = 0; i < reordered.size(); i++) {
            reordered.get(i).setPosition(i);
        }

        storyRepository.saveAll(reordered);
        story.setPosition(reordered.indexOf(story));
    }

    private void reindexStoriesAfterRemoval(Sprint sprint, BoardColumn column, Story story) {
        List<Story> stories = storyRepository.findBySprintAndBoardColumnOrderByPositionAsc(sprint, column)
                .stream()
                .filter(Story::isActive)
                .filter(existing -> !existing.getId().equals(story.getId()))
                .toList();

        for (int i = 0; i < stories.size(); i++) {
            stories.get(i).setPosition(i);
        }

        storyRepository.saveAll(stories);
    }

    private void validateWipLimit(Sprint sprint, BoardColumn targetColumn, Story story) {
        if (targetColumn.getWipLimit() == null || targetColumn.getWipLimit() <= 0) {
            return;
        }
        if (story.getBoardColumn() != null
                && targetColumn.getId().equals(story.getBoardColumn().getId())) {
            return;
        }
        int count = storyRepository.countBySprintAndBoardColumnAndDeletedAtIsNull(
                sprint, targetColumn);
        if (count >= targetColumn.getWipLimit()) {
            throw new BadRequestException("WIP limit exceeded for column");
        }
    }

    private void validateSprintCapacity(Sprint sprint, Story story) {
        Integer capacityPoints = sprint.getCapacityPoints();
        if (capacityPoints == null || capacityPoints <= 0) {
            return;
        }
        int totalPoints = storyRepository.findBySprint(sprint).stream()
                .filter(Story::isActive)
                .filter(existing -> !existing.getId().equals(story.getId()))
                .map(Story::getStoryPoints)
                .filter(points -> points != null)
                .mapToInt(Integer::intValue)
                .sum();
        int storyPoints = story.getStoryPoints() != null ? story.getStoryPoints() : 0;
        if (totalPoints + storyPoints > capacityPoints) {
            throw new BadRequestException("Sprint capacity exceeded");
        }
    }

    private BoardColumn findBoardColumn(Long columnId, Project project) {
        BoardColumn column = boardColumnRepository.findById(columnId)
                .filter(BoardColumn::isActive)
                .orElseThrow(() -> new NotFoundException("Board column not found"));
        if (!column.getProject().getId().equals(project.getId())) {
            throw new NotFoundException("Board column not found in this project");
        }
        return column;
    }
}

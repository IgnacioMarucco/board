package com.board.service.impl;

import com.board.dto.sprint.SprintCreateRequest;
import com.board.dto.sprint.SprintResponse;
import com.board.dto.sprint.SprintSummaryResponse;
import com.board.dto.sprint.SprintUpdateRequest;
import com.board.entity.Ceremony;
import com.board.entity.Project;
import com.board.entity.Sprint;
import com.board.entity.Story;
import com.board.entity.User;
import com.board.entity.enums.CeremonyStatus;
import com.board.entity.enums.CeremonyType;
import com.board.entity.enums.Role;
import com.board.entity.enums.SprintStatus;
import com.board.exception.BadRequestException;
import com.board.exception.ForbiddenException;
import com.board.exception.NotFoundException;
import com.board.mapper.SprintMapper;
import com.board.repository.CeremonyRepository;
import com.board.repository.ProjectMemberRepository;
import com.board.repository.ProjectRepository;
import com.board.repository.SprintRepository;
import com.board.repository.StoryRepository;
import com.board.repository.UserRepository;
import com.board.service.MetricsService;
import com.board.service.SprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of SprintService.
 */
@Service
@RequiredArgsConstructor
public class SprintServiceImpl implements SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final StoryRepository storyRepository;
    private final CeremonyRepository ceremonyRepository;
    private final SprintMapper sprintMapper;
    private final MetricsService metricsService;

    @Override
    @Transactional
    public SprintResponse createSprint(Long projectId, SprintCreateRequest request, Long userId) {
        Project project = findProjectById(projectId);
        validateRole(project, userId, Role.SCRUM_MASTER);

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        Sprint sprint = Sprint.builder()
                .name(request.getName())
                .goal(request.getGoal())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .capacityPoints(request.getCapacityPoints())
                .status(SprintStatus.PLANNING)
                .project(project)
                .build();

        sprint = sprintRepository.save(sprint);
        createDefaultCeremonies(sprint);
        return sprintMapper.toResponse(sprint);
    }

    @Override
    @Transactional(readOnly = true)
    public SprintResponse getSprint(Long projectId, Long sprintId, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        Sprint sprint = findSprintById(sprintId);
        validateSprintBelongsToProject(sprint, project);

        return sprintMapper.toResponse(sprint);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SprintResponse> getSprintsForProject(Long projectId, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        List<Sprint> sprints = sprintRepository.findByProjectOrderByStartDateDesc(project);
        return sprintMapper.toResponseList(sprints);
    }

    @Override
    @Transactional
    public SprintResponse updateSprint(Long projectId, Long sprintId,
            SprintUpdateRequest request, Long userId) {
        Project project = findProjectById(projectId);
        Role role = getRoleForProject(project, userId);

        Sprint sprint = findSprintById(sprintId);
        validateSprintBelongsToProject(sprint, project);

        if (sprint.getStatus() == SprintStatus.COMPLETED) {
            throw new BadRequestException("Cannot update a completed sprint");
        }

        boolean editingSchedule = request.getName() != null
                || request.getStartDate() != null
                || request.getEndDate() != null;
        if (editingSchedule) {
            requireRole(role, Role.SCRUM_MASTER);
        } else if (request.getGoal() != null) {
            requireRole(role, Role.SCRUM_MASTER, Role.PRODUCT_OWNER);
        }

        if (request.getName() != null) {
            sprint.setName(request.getName());
        }
        if (request.getGoal() != null) {
            sprint.setGoal(request.getGoal());
        }
        if (request.getStartDate() != null) {
            sprint.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            if (request.getStartDate() != null
                    && request.getEndDate().isBefore(request.getStartDate())) {
                throw new BadRequestException("End date must be after start date");
            }
            sprint.setEndDate(request.getEndDate());
        }
        if (request.getCapacityPoints() != null) {
            sprint.setCapacityPoints(request.getCapacityPoints());
        }

        sprint = sprintRepository.save(sprint);
        return sprintMapper.toResponse(sprint);
    }

    @Override
    @Transactional
    public void deleteSprint(Long projectId, Long sprintId, Long userId) {
        Project project = findProjectById(projectId);
        validateRole(project, userId, Role.SCRUM_MASTER);

        Sprint sprint = findSprintById(sprintId);
        validateSprintBelongsToProject(sprint, project);

        if (sprint.getStatus() == SprintStatus.ACTIVE) {
            throw new BadRequestException("Cannot delete an active sprint");
        }

        sprint.softDelete();
        sprintRepository.save(sprint);
    }

    @Override
    @Transactional
    public SprintResponse startSprint(Long projectId, Long sprintId, Long userId) {
        Project project = findProjectById(projectId);
        validateRole(project, userId, Role.SCRUM_MASTER);

        Sprint sprint = findSprintById(sprintId);
        validateSprintBelongsToProject(sprint, project);

        if (sprint.getStatus() != SprintStatus.PLANNING) {
            throw new BadRequestException("Only sprints in PLANNING status can be started");
        }

        // Check if there's already an active sprint
        Optional<Sprint> activeSprint = sprintRepository
                .findByProjectAndStatus(project, SprintStatus.ACTIVE);
        if (activeSprint.isPresent()) {
            throw new BadRequestException("Cannot start a new sprint while another is active");
        }

        applyCommittedScope(sprint);
        validateSprintCapacity(sprint);
        sprint.setStatus(SprintStatus.ACTIVE);
        sprint.setStartedAt(LocalDateTime.now());
        sprint = sprintRepository.save(sprint);
        metricsService.captureSprintSnapshot(projectId, sprintId);
        return sprintMapper.toResponse(sprint);
    }

    @Override
    @Transactional
    public SprintResponse completeSprint(Long projectId, Long sprintId, Long userId) {
        Project project = findProjectById(projectId);
        validateRole(project, userId, Role.SCRUM_MASTER);

        Sprint sprint = findSprintById(sprintId);
        validateSprintBelongsToProject(sprint, project);

        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new BadRequestException("Only active sprints can be completed");
        }

        applyCompletionSummary(sprint);
        moveIncompleteStoriesToBacklog(sprint);
        sprint.setStatus(SprintStatus.COMPLETED);
        sprint.setCompletedAt(LocalDateTime.now());
        sprint = sprintRepository.save(sprint);
        metricsService.captureSprintSnapshot(projectId, sprintId);
        return sprintMapper.toResponse(sprint);
    }

    @Override
    @Transactional(readOnly = true)
    public SprintSummaryResponse getSprintSummary(Long projectId, Long sprintId, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        Sprint sprint = findSprintById(sprintId);
        validateSprintBelongsToProject(sprint, project);

        SprintSummaryResponse summary = buildSummary(sprint);
        return summary;
    }

    private Project findProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Project not found"));
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

    private void validateSprintBelongsToProject(Sprint sprint, Project project) {
        if (!sprint.getProject().getId().equals(project.getId())) {
            throw new NotFoundException("Sprint not found in this project");
        }
    }

    private void validateRole(Project project, Long userId, Role... allowedRoles) {
        Role role = getRoleForProject(project, userId);
        requireRole(role, allowedRoles);
    }

    private void requireRole(Role role, Role... allowedRoles) {
        for (Role allowed : allowedRoles) {
            if (allowed == role) {
                return;
            }
        }
        throw new ForbiddenException("You are not allowed to perform this action");
    }

    private void createDefaultCeremonies(Sprint sprint) {
        List<Ceremony> ceremonies = new ArrayList<>();
        ceremonies.add(createCeremony(sprint, CeremonyType.PLANNING,
                sprint.getStartDate().atTime(LocalTime.of(9, 0)), 120));
        ceremonies.add(createCeremony(sprint, CeremonyType.REVIEW,
                sprint.getEndDate().atTime(LocalTime.of(16, 0)), 60));
        ceremonies.add(createCeremony(sprint, CeremonyType.RETROSPECTIVE,
                sprint.getEndDate().atTime(LocalTime.of(17, 0)), 60));
        ceremonyRepository.saveAll(ceremonies);
    }

    private Ceremony createCeremony(Sprint sprint, CeremonyType type,
            java.time.LocalDateTime scheduledAt, Integer durationMinutes) {
        return Ceremony.builder()
                .type(type)
                .scheduledAt(scheduledAt)
                .durationMinutes(durationMinutes)
                .status(CeremonyStatus.SCHEDULED)
                .sprint(sprint)
                .build();
    }

    private void applyCommittedScope(Sprint sprint) {
        List<Story> stories = storyRepository.findBySprint(sprint).stream()
                .filter(Story::isActive)
                .collect(Collectors.toList());
        int committedPoints = stories.stream()
                .map(Story::getStoryPoints)
                .filter(points -> points != null)
                .mapToInt(Integer::intValue)
                .sum();
        sprint.setCommittedPoints(committedPoints);
        sprint.setCommittedStoryCount(stories.size());
    }

    private void validateSprintCapacity(Sprint sprint) {
        Integer capacityPoints = sprint.getCapacityPoints();
        if (capacityPoints == null || capacityPoints <= 0) {
            return;
        }
        if (sprint.getCommittedPoints() > capacityPoints) {
            throw new BadRequestException("Sprint capacity exceeded by committed scope");
        }
    }

    private void applyCompletionSummary(Sprint sprint) {
        List<Story> stories = storyRepository.findBySprint(sprint).stream()
                .filter(Story::isActive)
                .collect(Collectors.toList());
        List<Story> completed = stories.stream()
                .filter(story -> story.getStatus() == com.board.entity.enums.StoryStatus.DONE)
                .collect(Collectors.toList());
        int completedPoints = completed.stream()
                .map(Story::getStoryPoints)
                .filter(points -> points != null)
                .mapToInt(Integer::intValue)
                .sum();
        int spilloverCount = (int) stories.stream()
                .filter(story -> story.getStatus() != com.board.entity.enums.StoryStatus.DONE)
                .count();

        sprint.setCompletedPoints(completedPoints);
        sprint.setCompletedStoryCount(completed.size());
        sprint.setSpilloverCount(spilloverCount);
    }

    private void moveIncompleteStoriesToBacklog(Sprint sprint) {
        List<Story> incomplete = storyRepository.findBySprint(sprint).stream()
                .filter(Story::isActive)
                .filter(story -> story.getStatus() != com.board.entity.enums.StoryStatus.DONE)
                .collect(Collectors.toList());

        if (incomplete.isEmpty()) {
            return;
        }

        Project project = sprint.getProject();
        int startPosition = (int) storyRepository
                .findByEpicProjectAndSprintIsNullOrderByPositionAsc(project)
                .stream()
                .filter(Story::isActive)
                .count();

        int position = startPosition;
        for (Story story : incomplete) {
            story.setSprint(null);
            story.setBoardColumn(null);
            story.setSprintAddedAt(null);
            story.setPosition(position++);
        }

        storyRepository.saveAll(incomplete);
    }

    private SprintSummaryResponse buildSummary(Sprint sprint) {
        SprintSummaryResponse.SprintSummaryResponseBuilder builder = SprintSummaryResponse.builder()
                .sprintId(sprint.getId())
                .sprintName(sprint.getName())
                .status(sprint.getStatus())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .startedAt(sprint.getStartedAt())
                .completedAt(sprint.getCompletedAt())
                .committedPoints(sprint.getCommittedPoints())
                .committedStoryCount(sprint.getCommittedStoryCount())
                .completedPoints(sprint.getCompletedPoints())
                .completedStoryCount(sprint.getCompletedStoryCount())
                .spilloverCount(sprint.getSpilloverCount())
                .scopeChangeCount(sprint.getScopeChangeCount());

        double completionRate = 0.0d;
        if (sprint.getCommittedPoints() != null && sprint.getCommittedPoints() > 0) {
            completionRate = (double) sprint.getCompletedPoints() / sprint.getCommittedPoints();
        }
        return builder.completionRate(completionRate).build();
    }
}

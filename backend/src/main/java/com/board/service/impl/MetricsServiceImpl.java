package com.board.service.impl;

import com.board.dto.metrics.BurndownPointResponse;
import com.board.dto.metrics.BurndownResponse;
import com.board.dto.metrics.VelocityEntryResponse;
import com.board.dto.metrics.VelocityResponse;
import com.board.entity.Project;
import com.board.entity.Sprint;
import com.board.entity.SprintSnapshot;
import com.board.entity.Story;
import com.board.entity.User;
import com.board.entity.enums.SprintStatus;
import com.board.entity.enums.StoryStatus;
import com.board.exception.ForbiddenException;
import com.board.exception.NotFoundException;
import com.board.repository.ProjectMemberRepository;
import com.board.repository.ProjectRepository;
import com.board.repository.SprintRepository;
import com.board.repository.SprintSnapshotRepository;
import com.board.repository.StoryRepository;
import com.board.repository.UserRepository;
import com.board.service.MetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of MetricsService.
 */
@Service
@RequiredArgsConstructor
public class MetricsServiceImpl implements MetricsService {

    private static final int DEFAULT_VELOCITY_LIMIT = 10;

    private final SprintSnapshotRepository sprintSnapshotRepository;
    private final SprintRepository sprintRepository;
    private final StoryRepository storyRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    private Clock clock = Clock.systemUTC();

    @Override
    @Transactional(readOnly = true)
    public BurndownResponse getBurndown(Long projectId, Long sprintId, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        Sprint sprint = findSprintById(sprintId);
        validateSprintBelongsToProject(sprint, project);

        List<BurndownPointResponse> points = sprintSnapshotRepository
                .findBySprintOrderBySnapshotDateAsc(sprint)
                .stream()
                .map(snapshot -> BurndownPointResponse.builder()
                        .date(snapshot.getSnapshotDate())
                        .remainingPoints(snapshot.getRemainingPoints())
                        .build())
                .toList();

        return BurndownResponse.builder()
                .sprintId(sprint.getId())
                .sprintName(sprint.getName())
                .committedPoints(sprint.getCommittedPoints())
                .points(points)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public VelocityResponse getVelocity(Long projectId, Long userId, Integer limit) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        int maxItems = limit != null && limit > 0 ? limit : DEFAULT_VELOCITY_LIMIT;
        List<VelocityEntryResponse> entries = sprintRepository
                .findByProjectAndStatusIn(project, List.of(SprintStatus.COMPLETED))
                .stream()
                .filter(Sprint::isActive)
                .sorted((left, right) -> right.getEndDate().compareTo(left.getEndDate()))
                .limit(maxItems)
                .map(sprint -> VelocityEntryResponse.builder()
                        .sprintId(sprint.getId())
                        .sprintName(sprint.getName())
                        .completedPoints(sprint.getCompletedPoints())
                        .endDate(sprint.getEndDate())
                        .build())
                .toList();

        return VelocityResponse.builder()
                .projectId(project.getId())
                .sprints(entries)
                .build();
    }

    @Override
    @Transactional
    public void captureDailySnapshots() {
        List<Sprint> activeSprints = sprintRepository.findByStatus(SprintStatus.ACTIVE)
                .stream()
                .filter(Sprint::isActive)
                .toList();

        for (Sprint sprint : activeSprints) {
            LocalDate snapshotDate = resolveSnapshotDate(sprint.getProject());
            if (sprintSnapshotRepository.existsBySprintAndSnapshotDate(sprint, snapshotDate)) {
                continue;
            }
            captureSnapshotForSprint(sprint, snapshotDate);
        }
    }

    @Override
    @Transactional
    public void captureSprintSnapshot(Long projectId, Long sprintId) {
        Sprint sprint = findSprintById(sprintId);
        Project project = findProjectById(projectId);
        validateSprintBelongsToProject(sprint, project);
        captureSnapshotForSprint(sprint, resolveSnapshotDate(project));
    }

    void setClock(Clock clock) {
        this.clock = clock;
    }

    private void captureSnapshotForSprint(Sprint sprint, LocalDate snapshotDate) {
        List<Story> stories = storyRepository.findBySprint(sprint).stream()
                .filter(Story::isActive)
                .toList();

        int remainingPoints = stories.stream()
                .filter(story -> story.getStatus() != StoryStatus.DONE)
                .map(Story::getStoryPoints)
                .filter(points -> points != null)
                .mapToInt(Integer::intValue)
                .sum();

        Map<String, Integer> columnCounts = new HashMap<>();
        for (Story story : stories) {
            String columnName = story.getBoardColumn() != null
                    ? story.getBoardColumn().getName()
                    : "UNASSIGNED";
            columnCounts.put(columnName, columnCounts.getOrDefault(columnName, 0) + 1);
        }

        SprintSnapshot snapshot = SprintSnapshot.builder()
                .sprint(sprint)
                .project(sprint.getProject())
                .snapshotDate(snapshotDate)
                .remainingPoints(remainingPoints)
                .totalStoryCount(stories.size())
                .columnCounts(columnCounts)
                .build();

        sprintSnapshotRepository.save(snapshot);
    }

    private LocalDate resolveSnapshotDate(Project project) {
        String timeZone = project.getTimeZone();
        ZoneId zoneId = ZoneId.of(
                timeZone == null || timeZone.isBlank() ? "UTC" : timeZone);
        return LocalDate.now(clock.withZone(zoneId));
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

    private void validateSprintBelongsToProject(Sprint sprint, Project project) {
        if (!sprint.getProject().getId().equals(project.getId())) {
            throw new NotFoundException("Sprint not found in this project");
        }
    }

    private void validateMembership(Project project, Long userId) {
        User user = findUserById(userId);
        if (!projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(project, user)) {
            throw new ForbiddenException("You are not a member of this project");
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}

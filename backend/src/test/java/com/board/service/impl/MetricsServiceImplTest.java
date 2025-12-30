package com.board.service.impl;

import com.board.dto.metrics.BurndownResponse;
import com.board.dto.metrics.VelocityResponse;
import com.board.entity.BoardColumn;
import com.board.entity.Project;
import com.board.entity.Sprint;
import com.board.entity.SprintSnapshot;
import com.board.entity.Story;
import com.board.entity.User;
import com.board.entity.enums.SprintStatus;
import com.board.entity.enums.StoryStatus;
import com.board.repository.ProjectMemberRepository;
import com.board.repository.ProjectRepository;
import com.board.repository.SprintRepository;
import com.board.repository.SprintSnapshotRepository;
import com.board.repository.StoryRepository;
import com.board.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for MetricsServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class MetricsServiceImplTest {

    @Mock
    private SprintSnapshotRepository sprintSnapshotRepository;

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private StoryRepository storyRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MetricsServiceImpl metricsService;

    @Test
    @DisplayName("getBurndown should return snapshot points")
    void getBurndownShouldReturnSnapshotPoints() {
        Project project = Project.builder().id(1L).key("TEST").name("Test").timeZone("UTC").build();
        User user = User.builder().id(2L).email("user@test.com").build();
        Sprint sprint = Sprint.builder().id(3L).name("Sprint 1").project(project).committedPoints(10).build();
        SprintSnapshot snapshot = SprintSnapshot.builder()
                .id(5L)
                .sprint(sprint)
                .project(project)
                .snapshotDate(LocalDate.of(2025, 1, 1))
                .remainingPoints(7)
                .totalStoryCount(2)
                .build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(project, user))
                .thenReturn(true);
        when(sprintRepository.findById(3L)).thenReturn(Optional.of(sprint));
        when(sprintSnapshotRepository.findBySprintOrderBySnapshotDateAsc(sprint))
                .thenReturn(List.of(snapshot));

        BurndownResponse response = metricsService.getBurndown(1L, 3L, 2L);

        assertThat(response.getSprintId()).isEqualTo(3L);
        assertThat(response.getCommittedPoints()).isEqualTo(10);
        assertThat(response.getPoints()).hasSize(1);
        assertThat(response.getPoints().get(0).getRemainingPoints()).isEqualTo(7);
    }

    @Test
    @DisplayName("getVelocity should return completed sprints sorted and limited")
    void getVelocityShouldReturnSortedLimited() {
        Project project = Project.builder().id(1L).key("TEST").name("Test").build();
        User user = User.builder().id(2L).email("user@test.com").build();
        Sprint sprint1 = Sprint.builder()
                .id(10L)
                .name("Sprint 1")
                .status(SprintStatus.COMPLETED)
                .completedPoints(5)
                .endDate(LocalDate.of(2025, 1, 10))
                .build();
        Sprint sprint2 = Sprint.builder()
                .id(11L)
                .name("Sprint 2")
                .status(SprintStatus.COMPLETED)
                .completedPoints(8)
                .endDate(LocalDate.of(2025, 1, 20))
                .build();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(project, user))
                .thenReturn(true);
        when(sprintRepository.findByProjectAndStatusIn(project, List.of(SprintStatus.COMPLETED)))
                .thenReturn(List.of(sprint1, sprint2));

        VelocityResponse response = metricsService.getVelocity(1L, 2L, 1);

        assertThat(response.getSprints()).hasSize(1);
        assertThat(response.getSprints().get(0).getSprintId()).isEqualTo(11L);
    }

    @Test
    @DisplayName("captureDailySnapshots should persist snapshot for active sprint")
    void captureDailySnapshotsShouldPersist() {
        Project project = Project.builder().id(1L).key("TEST").name("Test").timeZone("UTC").build();
        Sprint sprint = Sprint.builder()
                .id(2L)
                .project(project)
                .status(SprintStatus.ACTIVE)
                .build();
        Story todoStory = Story.builder()
                .id(10L)
                .status(StoryStatus.IN_PROGRESS)
                .storyPoints(3)
                .boardColumn(BoardColumn.builder().id(1L).name("Doing").build())
                .build();
        Story doneStory = Story.builder()
                .id(11L)
                .status(StoryStatus.DONE)
                .storyPoints(5)
                .boardColumn(BoardColumn.builder().id(2L).name("Done").build())
                .build();

        metricsService.setClock(Clock.fixed(Instant.parse("2025-01-01T10:00:00Z"), ZoneOffset.UTC));

        when(sprintRepository.findByStatus(SprintStatus.ACTIVE)).thenReturn(List.of(sprint));
        when(sprintSnapshotRepository.existsBySprintAndSnapshotDate(sprint, LocalDate.of(2025, 1, 1)))
                .thenReturn(false);
        when(storyRepository.findBySprint(sprint)).thenReturn(List.of(todoStory, doneStory));

        metricsService.captureDailySnapshots();

        ArgumentCaptor<SprintSnapshot> captor = ArgumentCaptor.forClass(SprintSnapshot.class);
        verify(sprintSnapshotRepository).save(captor.capture());

        SprintSnapshot saved = captor.getValue();
        assertThat(saved.getRemainingPoints()).isEqualTo(3);
        assertThat(saved.getTotalStoryCount()).isEqualTo(2);
        assertThat(saved.getColumnCounts().get("Doing")).isEqualTo(1);
        assertThat(saved.getColumnCounts().get("Done")).isEqualTo(1);
    }

    @Test
    @DisplayName("captureSprintSnapshot should persist snapshot for sprint")
    void captureSprintSnapshotShouldPersist() {
        Project project = Project.builder().id(1L).key("TEST").name("Test").timeZone("UTC").build();
        Sprint sprint = Sprint.builder().id(2L).project(project).status(SprintStatus.ACTIVE).build();
        Story story = Story.builder()
                .id(10L)
                .status(StoryStatus.IN_PROGRESS)
                .storyPoints(3)
                .build();

        metricsService.setClock(Clock.fixed(Instant.parse("2025-01-02T10:00:00Z"), ZoneOffset.UTC));

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(sprintRepository.findById(2L)).thenReturn(Optional.of(sprint));
        when(storyRepository.findBySprint(sprint)).thenReturn(List.of(story));

        metricsService.captureSprintSnapshot(1L, 2L);

        verify(sprintSnapshotRepository).save(any(SprintSnapshot.class));
    }
}

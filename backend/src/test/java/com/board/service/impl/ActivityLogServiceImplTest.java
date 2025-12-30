package com.board.service.impl;

import com.board.dto.activity.ActivityLogResponse;
import com.board.entity.ActivityLog;
import com.board.entity.Project;
import com.board.entity.User;
import com.board.exception.ForbiddenException;
import com.board.mapper.ActivityLogMapper;
import com.board.repository.ActivityLogRepository;
import com.board.repository.ProjectMemberRepository;
import com.board.repository.ProjectRepository;
import com.board.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ActivityLogServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class ActivityLogServiceImplTest {

    @Mock
    private ActivityLogRepository activityLogRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivityLogMapper activityLogMapper;

    @InjectMocks
    private ActivityLogServiceImpl activityLogService;

    @Test
    @DisplayName("recordActivity should persist activity log")
    void recordActivityShouldPersist() {
        Project project = Project.builder().id(1L).key("TEST").name("Test").build();
        User actor = User.builder().id(2L).email("actor@test.com").build();

        activityLogService.recordActivity(project, actor, "STORY", 10L,
                "STATUS_CHANGED", "from=BACKLOG,to=IN_PROGRESS");

        ArgumentCaptor<ActivityLog> captor = ArgumentCaptor.forClass(ActivityLog.class);
        verify(activityLogRepository).save(captor.capture());

        ActivityLog saved = captor.getValue();
        assertThat(saved.getProject()).isEqualTo(project);
        assertThat(saved.getActor()).isEqualTo(actor);
        assertThat(saved.getEntityType()).isEqualTo("STORY");
        assertThat(saved.getEntityId()).isEqualTo(10L);
        assertThat(saved.getAction()).isEqualTo("STATUS_CHANGED");
    }

    @Nested
    @DisplayName("getActivityForProject")
    class GetActivityForProject {

        @Test
        @DisplayName("should return activity for member")
        void shouldReturnActivityForMember() {
            Project project = Project.builder().id(1L).key("TEST").name("Test").build();
            User user = User.builder().id(2L).email("user@test.com").build();
            ActivityLog activityLog = ActivityLog.builder().id(1L).project(project).actor(user).build();
            ActivityLogResponse response = ActivityLogResponse.builder().id(1L).build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
            when(userRepository.findById(2L)).thenReturn(Optional.of(user));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(project, user))
                    .thenReturn(true);
            when(activityLogRepository.findByProjectAndDeletedAtIsNullOrderByCreatedAtDesc(project))
                    .thenReturn(List.of(activityLog));
            when(activityLogMapper.toResponseList(any())).thenReturn(List.of(response));

            List<ActivityLogResponse> responses = activityLogService.getActivityForProject(1L, 2L);

            assertThat(responses).hasSize(1);
        }

        @Test
        @DisplayName("should throw ForbiddenException when not member")
        void shouldThrowWhenNotMember() {
            Project project = Project.builder().id(1L).key("TEST").name("Test").build();
            User user = User.builder().id(2L).email("user@test.com").build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
            when(userRepository.findById(2L)).thenReturn(Optional.of(user));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(project, user))
                    .thenReturn(false);

            assertThatThrownBy(() -> activityLogService.getActivityForProject(1L, 2L))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessage("You are not a member of this project");
        }
    }
}

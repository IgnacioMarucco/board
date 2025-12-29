package com.board.service.impl;

import com.board.dto.sprint.SprintCreateRequest;
import com.board.dto.sprint.SprintResponse;
import com.board.dto.sprint.SprintUpdateRequest;
import com.board.entity.Project;
import com.board.entity.ProjectMember;
import com.board.entity.Sprint;
import com.board.entity.User;
import com.board.entity.enums.SprintStatus;
import com.board.exception.BadRequestException;
import com.board.exception.NotFoundException;
import com.board.mapper.SprintMapper;
import com.board.repository.ProjectMemberRepository;
import com.board.repository.ProjectRepository;
import com.board.repository.SprintRepository;
import com.board.repository.StoryRepository;
import com.board.repository.UserRepository;
import com.board.repository.CeremonyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SprintServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class SprintServiceImplTest {

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoryRepository storyRepository;

    @Mock
    private CeremonyRepository ceremonyRepository;

    @Mock
    private SprintMapper sprintMapper;

    @InjectMocks
    private SprintServiceImpl sprintService;

    private User testUser;
    private Project testProject;
    private Sprint testSprint;
    private SprintResponse testSprintResponse;
    private ProjectMember scrumMasterMember;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .build();

        testProject = Project.builder()
                .id(1L)
                .key("TEST")
                .name("Test Project")
                .owner(testUser)
                .build();

        testSprint = Sprint.builder()
                .id(1L)
                .name("Sprint 1")
                .goal("Sprint Goal")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusWeeks(2))
                .status(SprintStatus.PLANNING)
                .project(testProject)
                .build();

        testSprintResponse = SprintResponse.builder()
                .id(1L)
                .name("Sprint 1")
                .status(SprintStatus.PLANNING)
                .build();

        scrumMasterMember = ProjectMember.builder()
                .id(10L)
                .user(testUser)
                .project(testProject)
                .role(com.board.entity.enums.Role.SCRUM_MASTER)
                .build();
    }

    @Nested
    @DisplayName("createSprint")
    class CreateSprint {

        @Test
        @DisplayName("should create sprint successfully")
        void shouldCreateSprintSuccessfully() {
            // Given
            SprintCreateRequest request = SprintCreateRequest.builder()
                    .name("Sprint 1")
                    .goal("Goal")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().plusWeeks(2))
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(Optional.of(scrumMasterMember));
            when(sprintRepository.save(any(Sprint.class))).thenReturn(testSprint);
            when(sprintMapper.toResponse(any(Sprint.class))).thenReturn(testSprintResponse);

            // When
            SprintResponse response = sprintService.createSprint(1L, request, 1L);

            // Then
            assertThat(response).isNotNull();
            verify(sprintRepository).save(any(Sprint.class));
        }

        @Test
        @DisplayName("should throw BadRequestException when end date is before start date")
        void shouldThrowWhenEndDateBeforeStartDate() {
            // Given
            SprintCreateRequest request = SprintCreateRequest.builder()
                    .name("Sprint 1")
                    .startDate(LocalDate.now())
                    .endDate(LocalDate.now().minusDays(1))
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(Optional.of(scrumMasterMember));

            // When/Then
            assertThatThrownBy(() -> sprintService.createSprint(1L, request, 1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("End date must be after start date");
        }
    }

    @Nested
    @DisplayName("getSprint")
    class GetSprint {

        @Test
        @DisplayName("should return sprint when user is member")
        void shouldReturnSprintWhenUserIsMember() {
            // Given
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(true);
            when(sprintRepository.findById(1L)).thenReturn(Optional.of(testSprint));
            when(sprintMapper.toResponse(testSprint)).thenReturn(testSprintResponse);

            // When
            SprintResponse response = sprintService.getSprint(1L, 1L, 1L);

            // Then
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("should throw NotFoundException when sprint not found")
        void shouldThrowWhenSprintNotFound() {
            // Given
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(true);
            when(sprintRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> sprintService.getSprint(1L, 999L, 1L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Sprint not found");
        }
    }

    @Nested
    @DisplayName("updateSprint")
    class UpdateSprint {

        @Test
        @DisplayName("should update sprint successfully")
        void shouldUpdateSprintSuccessfully() {
            // Given
            SprintUpdateRequest request = SprintUpdateRequest.builder()
                    .name("Updated Sprint")
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(Optional.of(scrumMasterMember));
            when(sprintRepository.findById(1L)).thenReturn(Optional.of(testSprint));
            when(sprintRepository.save(any(Sprint.class))).thenReturn(testSprint);
            when(sprintMapper.toResponse(any(Sprint.class))).thenReturn(testSprintResponse);

            // When
            SprintResponse response = sprintService.updateSprint(1L, 1L, request, 1L);

            // Then
            assertThat(response).isNotNull();
            verify(sprintRepository).save(testSprint);
        }

        @Test
        @DisplayName("should throw BadRequestException when updating completed sprint")
        void shouldThrowWhenUpdatingCompletedSprint() {
            // Given
            testSprint.setStatus(SprintStatus.COMPLETED);
            SprintUpdateRequest request = SprintUpdateRequest.builder()
                    .name("Updated")
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(Optional.of(scrumMasterMember));
            when(sprintRepository.findById(1L)).thenReturn(Optional.of(testSprint));

            // When/Then
            assertThatThrownBy(() -> sprintService.updateSprint(1L, 1L, request, 1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Cannot update a completed sprint");
        }
    }

    @Nested
    @DisplayName("startSprint")
    class StartSprint {

        @Test
        @DisplayName("should start sprint successfully")
        void shouldStartSprintSuccessfully() {
            // Given
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(Optional.of(scrumMasterMember));
            when(sprintRepository.findById(1L)).thenReturn(Optional.of(testSprint));
            when(sprintRepository.findByProjectAndStatus(testProject, SprintStatus.ACTIVE))
                    .thenReturn(Optional.empty());
            when(storyRepository.findBySprint(testSprint)).thenReturn(List.of());
            when(sprintRepository.save(any(Sprint.class))).thenReturn(testSprint);
            when(sprintMapper.toResponse(any(Sprint.class))).thenReturn(testSprintResponse);

            // When
            SprintResponse response = sprintService.startSprint(1L, 1L, 1L);

            // Then
            assertThat(response).isNotNull();
            verify(sprintRepository).save(testSprint);
        }

        @Test
        @DisplayName("should throw BadRequestException when another sprint is active")
        void shouldThrowWhenAnotherSprintActive() {
            // Given
            Sprint activeSprint = Sprint.builder()
                    .id(2L)
                    .status(SprintStatus.ACTIVE)
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(Optional.of(scrumMasterMember));
            when(sprintRepository.findById(1L)).thenReturn(Optional.of(testSprint));
            when(sprintRepository.findByProjectAndStatus(testProject, SprintStatus.ACTIVE))
                    .thenReturn(Optional.of(activeSprint));

            // When/Then
            assertThatThrownBy(() -> sprintService.startSprint(1L, 1L, 1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Cannot start a new sprint while another is active");
        }
    }

    @Nested
    @DisplayName("completeSprint")
    class CompleteSprint {

        @Test
        @DisplayName("should complete sprint successfully")
        void shouldCompleteSprintSuccessfully() {
            // Given
            testSprint.setStatus(SprintStatus.ACTIVE);

            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(Optional.of(scrumMasterMember));
            when(sprintRepository.findById(1L)).thenReturn(Optional.of(testSprint));
            when(sprintRepository.save(any(Sprint.class))).thenReturn(testSprint);
            when(sprintMapper.toResponse(any(Sprint.class))).thenReturn(testSprintResponse);
            when(storyRepository.findBySprint(testSprint)).thenReturn(List.of());

            // When
            SprintResponse response = sprintService.completeSprint(1L, 1L, 1L);

            // Then
            assertThat(response).isNotNull();
            verify(sprintRepository).save(testSprint);
        }

        @Test
        @DisplayName("should throw BadRequestException when sprint is not active")
        void shouldThrowWhenSprintNotActive() {
            // Given
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(Optional.of(scrumMasterMember));
            when(sprintRepository.findById(1L)).thenReturn(Optional.of(testSprint));

            // When/Then
            assertThatThrownBy(() -> sprintService.completeSprint(1L, 1L, 1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Only active sprints can be completed");
        }
    }

    @Nested
    @DisplayName("deleteSprint")
    class DeleteSprint {

        @Test
        @DisplayName("should soft delete sprint successfully")
        void shouldSoftDeleteSprintSuccessfully() {
            // Given
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(Optional.of(scrumMasterMember));
            when(sprintRepository.findById(1L)).thenReturn(Optional.of(testSprint));

            // When
            sprintService.deleteSprint(1L, 1L, 1L);

            // Then
            verify(sprintRepository).save(testSprint);
            assertThat(testSprint.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw BadRequestException when deleting active sprint")
        void shouldThrowWhenDeletingActiveSprint() {
            // Given
            testSprint.setStatus(SprintStatus.ACTIVE);

            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(Optional.of(scrumMasterMember));
            when(sprintRepository.findById(1L)).thenReturn(Optional.of(testSprint));

            // When/Then
            assertThatThrownBy(() -> sprintService.deleteSprint(1L, 1L, 1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Cannot delete an active sprint");
        }
    }
}

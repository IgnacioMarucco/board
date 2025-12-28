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
import com.board.exception.NotFoundException;
import com.board.mapper.StoryMapper;
import com.board.repository.EpicRepository;
import com.board.repository.ProjectMemberRepository;
import com.board.repository.ProjectRepository;
import com.board.repository.SprintRepository;
import com.board.repository.StoryRepository;
import com.board.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
 * Unit tests for StoryServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class StoryServiceImplTest {

    @Mock
    private StoryRepository storyRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private EpicRepository epicRepository;

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoryMapper storyMapper;

    @InjectMocks
    private StoryServiceImpl storyService;

    private User testUser;
    private Project testProject;
    private Epic testEpic;
    private Sprint testSprint;
    private Story testStory;
    private StoryResponse testStoryResponse;

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

        testEpic = Epic.builder()
                .id(1L)
                .key("EPIC-1")
                .title("Epic Title")
                .project(testProject)
                .build();

        testSprint = Sprint.builder()
                .id(1L)
                .name("Sprint 1")
                .project(testProject)
                .build();

        testStory = Story.builder()
                .id(1L)
                .key("STORY-1")
                .title("Story Title")
                .description("Description")
                .status(StoryStatus.BACKLOG)
                .priority(Priority.MEDIUM)
                .epic(testEpic)
                .build();

        testStoryResponse = StoryResponse.builder()
                .id(1L)
                .key("STORY-1")
                .title("Story Title")
                .status(StoryStatus.BACKLOG)
                .build();
    }

    @Nested
    @DisplayName("createStory")
    class CreateStory {

        @Test
        @DisplayName("should create story successfully")
        void shouldCreateStorySuccessfully() {
            // Given
            StoryCreateRequest request = StoryCreateRequest.builder()
                    .key("story-1")
                    .title("Story Title")
                    .description("Description")
                    .epicId(1L)
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUser(testProject, testUser)).thenReturn(true);
            when(storyRepository.findByKey("story-1")).thenReturn(Optional.empty());
            when(epicRepository.findById(1L)).thenReturn(Optional.of(testEpic));
            when(storyRepository.save(any(Story.class))).thenReturn(testStory);
            when(storyMapper.toResponse(any(Story.class))).thenReturn(testStoryResponse);

            // When
            StoryResponse response = storyService.createStory(1L, request, 1L);

            // Then
            assertThat(response).isNotNull();
            verify(storyRepository).save(any(Story.class));
        }

        @Test
        @DisplayName("should throw BadRequestException when key already exists")
        void shouldThrowWhenKeyExists() {
            // Given
            StoryCreateRequest request = StoryCreateRequest.builder()
                    .key("STORY-1")
                    .title("Title")
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUser(testProject, testUser)).thenReturn(true);
            when(storyRepository.findByKey("STORY-1")).thenReturn(Optional.of(testStory));

            // When/Then
            assertThatThrownBy(() -> storyService.createStory(1L, request, 1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Story key already exists");
        }
    }

    @Nested
    @DisplayName("getStory")
    class GetStory {

        @Test
        @DisplayName("should return story when user is member")
        void shouldReturnStoryWhenUserIsMember() {
            // Given
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUser(testProject, testUser)).thenReturn(true);
            when(storyRepository.findById(1L)).thenReturn(Optional.of(testStory));
            when(storyMapper.toResponse(testStory)).thenReturn(testStoryResponse);

            // When
            StoryResponse response = storyService.getStory(1L, 1L, 1L);

            // Then
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("should throw NotFoundException when story not found")
        void shouldThrowWhenStoryNotFound() {
            // Given
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUser(testProject, testUser)).thenReturn(true);
            when(storyRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> storyService.getStory(1L, 999L, 1L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Story not found");
        }
    }

    @Nested
    @DisplayName("updateStory")
    class UpdateStory {

        @Test
        @DisplayName("should update story successfully")
        void shouldUpdateStorySuccessfully() {
            // Given
            StoryUpdateRequest request = StoryUpdateRequest.builder()
                    .title("Updated Title")
                    .status(StoryStatus.IN_PROGRESS)
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUser(testProject, testUser)).thenReturn(true);
            when(storyRepository.findById(1L)).thenReturn(Optional.of(testStory));
            when(storyRepository.save(any(Story.class))).thenReturn(testStory);
            when(storyMapper.toResponse(any(Story.class))).thenReturn(testStoryResponse);

            // When
            StoryResponse response = storyService.updateStory(1L, 1L, request, 1L);

            // Then
            assertThat(response).isNotNull();
            verify(storyRepository).save(testStory);
        }
    }

    @Nested
    @DisplayName("deleteStory")
    class DeleteStory {

        @Test
        @DisplayName("should soft delete story successfully")
        void shouldSoftDeleteStorySuccessfully() {
            // Given
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(storyRepository.findById(1L)).thenReturn(Optional.of(testStory));

            // When
            storyService.deleteStory(1L, 1L, 1L);

            // Then
            verify(storyRepository).save(testStory);
            assertThat(testStory.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("assignStory")
    class AssignStory {

        @Test
        @DisplayName("should assign story successfully")
        void shouldAssignStorySuccessfully() {
            // Given
            StoryAssignRequest request = StoryAssignRequest.builder()
                    .assigneeId(2L)
                    .build();

            User assignee = User.builder()
                    .id(2L)
                    .email("assignee@example.com")
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));
            when(projectMemberRepository.existsByProjectAndUser(testProject, testUser)).thenReturn(true);
            when(projectMemberRepository.existsByProjectAndUser(testProject, assignee)).thenReturn(true);
            when(storyRepository.findById(1L)).thenReturn(Optional.of(testStory));
            when(storyRepository.save(any(Story.class))).thenReturn(testStory);
            when(storyMapper.toResponse(any(Story.class))).thenReturn(testStoryResponse);

            // When
            StoryResponse response = storyService.assignStory(1L, 1L, request, 1L);

            // Then
            assertThat(response).isNotNull();
            verify(storyRepository).save(testStory);
        }
    }
}

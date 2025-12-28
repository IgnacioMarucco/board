package com.board.service.impl;

import com.board.dto.task.TaskCreateRequest;
import com.board.dto.task.TaskResponse;
import com.board.dto.task.TaskUpdateRequest;
import com.board.entity.Epic;
import com.board.entity.Project;
import com.board.entity.Story;
import com.board.entity.Task;
import com.board.entity.User;
import com.board.entity.enums.TaskStatus;
import com.board.exception.BadRequestException;
import com.board.exception.NotFoundException;
import com.board.mapper.TaskMapper;
import com.board.repository.ProjectMemberRepository;
import com.board.repository.ProjectRepository;
import com.board.repository.StoryRepository;
import com.board.repository.TaskRepository;
import com.board.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for TaskServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private StoryRepository storyRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskServiceImpl taskService;

    private User testUser;
    private Project testProject;
    private Epic testEpic;
    private Story testStory;
    private Task testTask;
    private TaskResponse testTaskResponse;

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

        testStory = Story.builder()
                .id(1L)
                .key("STORY-1")
                .title("Story Title")
                .epic(testEpic)
                .build();

        testTask = Task.builder()
                .id(1L)
                .key("TASK-1")
                .title("Task Title")
                .description("Description")
                .status(TaskStatus.TODO)
                .estimatedHours(BigDecimal.valueOf(5))
                .story(testStory)
                .build();

        testTaskResponse = TaskResponse.builder()
                .id(1L)
                .key("TASK-1")
                .title("Task Title")
                .status(TaskStatus.TODO)
                .build();
    }

    @Nested
    @DisplayName("createTask")
    class CreateTask {

        @Test
        @DisplayName("should create task successfully")
        void shouldCreateTaskSuccessfully() {
            // Given
            TaskCreateRequest request = TaskCreateRequest.builder()
                    .key("task-1")
                    .title("Task Title")
                    .description("Description")
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUser(testProject, testUser)).thenReturn(true);
            when(storyRepository.findById(1L)).thenReturn(Optional.of(testStory));
            when(taskRepository.findByKey("task-1")).thenReturn(Optional.empty());
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);
            when(taskMapper.toResponse(any(Task.class))).thenReturn(testTaskResponse);

            // When
            TaskResponse response = taskService.createTask(1L, 1L, request, 1L);

            // Then
            assertThat(response).isNotNull();
            verify(taskRepository).save(any(Task.class));
        }

        @Test
        @DisplayName("should throw BadRequestException when key already exists")
        void shouldThrowWhenKeyExists() {
            // Given
            TaskCreateRequest request = TaskCreateRequest.builder()
                    .key("TASK-1")
                    .title("Title")
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUser(testProject, testUser)).thenReturn(true);
            when(storyRepository.findById(1L)).thenReturn(Optional.of(testStory));
            when(taskRepository.findByKey("TASK-1")).thenReturn(Optional.of(testTask));

            // When/Then
            assertThatThrownBy(() -> taskService.createTask(1L, 1L, request, 1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Task key already exists");
        }
    }

    @Nested
    @DisplayName("getTask")
    class GetTask {

        @Test
        @DisplayName("should return task when user is member")
        void shouldReturnTaskWhenUserIsMember() {
            // Given
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUser(testProject, testUser)).thenReturn(true);
            when(storyRepository.findById(1L)).thenReturn(Optional.of(testStory));
            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
            when(taskMapper.toResponse(testTask)).thenReturn(testTaskResponse);

            // When
            TaskResponse response = taskService.getTask(1L, 1L, 1L, 1L);

            // Then
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("should throw NotFoundException when task not found")
        void shouldThrowWhenTaskNotFound() {
            // Given
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUser(testProject, testUser)).thenReturn(true);
            when(storyRepository.findById(1L)).thenReturn(Optional.of(testStory));
            when(taskRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> taskService.getTask(1L, 1L, 999L, 1L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Task not found");
        }
    }

    @Nested
    @DisplayName("updateTask")
    class UpdateTask {

        @Test
        @DisplayName("should update task successfully")
        void shouldUpdateTaskSuccessfully() {
            // Given
            TaskUpdateRequest request = TaskUpdateRequest.builder()
                    .title("Updated Title")
                    .status(TaskStatus.IN_PROGRESS)
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUser(testProject, testUser)).thenReturn(true);
            when(storyRepository.findById(1L)).thenReturn(Optional.of(testStory));
            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);
            when(taskMapper.toResponse(any(Task.class))).thenReturn(testTaskResponse);

            // When
            TaskResponse response = taskService.updateTask(1L, 1L, 1L, request, 1L);

            // Then
            assertThat(response).isNotNull();
            verify(taskRepository).save(testTask);
        }
    }

    @Nested
    @DisplayName("deleteTask")
    class DeleteTask {

        @Test
        @DisplayName("should soft delete task successfully")
        void shouldSoftDeleteTaskSuccessfully() {
            // Given
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(storyRepository.findById(1L)).thenReturn(Optional.of(testStory));
            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

            // When
            taskService.deleteTask(1L, 1L, 1L, 1L);

            // Then
            verify(taskRepository).save(testTask);
            assertThat(testTask.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("completeTask")
    class CompleteTask {

        @Test
        @DisplayName("should complete task successfully")
        void shouldCompleteTaskSuccessfully() {
            // Given
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUser(testProject, testUser)).thenReturn(true);
            when(storyRepository.findById(1L)).thenReturn(Optional.of(testStory));
            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
            when(taskRepository.save(any(Task.class))).thenReturn(testTask);
            when(taskMapper.toResponse(any(Task.class))).thenReturn(testTaskResponse);

            // When
            TaskResponse response = taskService.completeTask(1L, 1L, 1L, 1L);

            // Then
            assertThat(response).isNotNull();
            assertThat(testTask.getStatus()).isEqualTo(TaskStatus.DONE);
            verify(taskRepository).save(testTask);
        }

        @Test
        @DisplayName("should throw BadRequestException when task already completed")
        void shouldThrowWhenAlreadyCompleted() {
            // Given
            testTask.setStatus(TaskStatus.DONE);

            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUser(testProject, testUser)).thenReturn(true);
            when(storyRepository.findById(1L)).thenReturn(Optional.of(testStory));
            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

            // When/Then
            assertThatThrownBy(() -> taskService.completeTask(1L, 1L, 1L, 1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Task is already completed");
        }
    }
}

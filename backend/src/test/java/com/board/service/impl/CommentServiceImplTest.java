package com.board.service.impl;

import com.board.dto.comment.CommentCreateRequest;
import com.board.dto.comment.CommentResponse;
import com.board.dto.comment.CommentUpdateRequest;
import com.board.entity.Comment;
import com.board.entity.Epic;
import com.board.entity.Project;
import com.board.entity.Story;
import com.board.entity.Task;
import com.board.entity.User;
import com.board.exception.ForbiddenException;
import com.board.exception.NotFoundException;
import com.board.mapper.CommentMapper;
import com.board.repository.CommentRepository;
import com.board.repository.EpicRepository;
import com.board.repository.ProjectMemberRepository;
import com.board.repository.StoryRepository;
import com.board.repository.TaskRepository;
import com.board.repository.UserRepository;
import com.board.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for CommentServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private EpicRepository epicRepository;

    @Mock
    private StoryRepository storyRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CommentServiceImpl commentService;

    private User testUser;
    private Project testProject;
    private Epic testEpic;
    private Story testStory;
    private Task testTask;
    private Comment testComment;
    private CommentResponse testCommentResponse;

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
                .story(testStory)
                .build();

        testComment = Comment.builder()
                .id(1L)
                .content("Test comment")
                .author(testUser)
                .epic(testEpic)
                .build();

        testCommentResponse = CommentResponse.builder()
                .id(1L)
                .content("Test comment")
                .authorId(1L)
                .build();
    }

    @Nested
    @DisplayName("createCommentOnEpic")
    class CreateCommentOnEpic {

        @Test
        @DisplayName("should create comment on epic successfully")
        void shouldCreateCommentOnEpicSuccessfully() {
            // Given
            CommentCreateRequest request = CommentCreateRequest.builder()
                    .content("Test comment")
                    .build();

            when(epicRepository.findById(1L)).thenReturn(Optional.of(testEpic));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(true);
            when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
            when(commentMapper.toResponse(any(Comment.class))).thenReturn(testCommentResponse);

            // When
            CommentResponse response = commentService.createCommentOnEpic(1L, request, 1L);

            // Then
            assertThat(response).isNotNull();
            verify(commentRepository).save(any(Comment.class));
        }
    }

    @Nested
    @DisplayName("createCommentOnStory")
    class CreateCommentOnStory {

        @Test
        @DisplayName("should create comment on story successfully")
        void shouldCreateCommentOnStorySuccessfully() {
            // Given
            CommentCreateRequest request = CommentCreateRequest.builder()
                    .content("Test comment")
                    .build();

            when(storyRepository.findById(1L)).thenReturn(Optional.of(testStory));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(true);
            when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
            when(commentMapper.toResponse(any(Comment.class))).thenReturn(testCommentResponse);

            // When
            CommentResponse response = commentService.createCommentOnStory(1L, request, 1L);

            // Then
            assertThat(response).isNotNull();
            verify(commentRepository).save(any(Comment.class));
        }
    }

    @Nested
    @DisplayName("createCommentOnTask")
    class CreateCommentOnTask {

        @Test
        @DisplayName("should create comment on task successfully")
        void shouldCreateCommentOnTaskSuccessfully() {
            // Given
            CommentCreateRequest request = CommentCreateRequest.builder()
                    .content("Test comment")
                    .build();

            when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(true);
            when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
            when(commentMapper.toResponse(any(Comment.class))).thenReturn(testCommentResponse);

            // When
            CommentResponse response = commentService.createCommentOnTask(1L, request, 1L);

            // Then
            assertThat(response).isNotNull();
            verify(commentRepository).save(any(Comment.class));
        }
    }

    @Nested
    @DisplayName("getComment")
    class GetComment {

        @Test
        @DisplayName("should return comment when user is member")
        void shouldReturnCommentWhenUserIsMember() {
            // Given
            when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(true);
            when(commentMapper.toResponse(testComment)).thenReturn(testCommentResponse);

            // When
            CommentResponse response = commentService.getComment(1L, 1L);

            // Then
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("should throw NotFoundException when comment not found")
        void shouldThrowWhenCommentNotFound() {
            // Given
            when(commentRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> commentService.getComment(999L, 1L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Comment not found");
        }
    }

    @Nested
    @DisplayName("updateComment")
    class UpdateComment {

        @Test
        @DisplayName("should update comment successfully")
        void shouldUpdateCommentSuccessfully() {
            // Given
            CommentUpdateRequest request = CommentUpdateRequest.builder()
                    .content("Updated content")
                    .build();

            when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
            when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
            when(commentMapper.toResponse(any(Comment.class))).thenReturn(testCommentResponse);

            // When
            CommentResponse response = commentService.updateComment(1L, request, 1L);

            // Then
            assertThat(response).isNotNull();
            verify(commentRepository).save(testComment);
        }

        @Test
        @DisplayName("should throw ForbiddenException when not author")
        void shouldThrowWhenNotAuthor() {
            // Given
            CommentUpdateRequest request = CommentUpdateRequest.builder()
                    .content("Updated content")
                    .build();

            when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

            // When/Then
            assertThatThrownBy(() -> commentService.updateComment(1L, request, 2L))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessage("You can only edit your own comments");
        }
    }

    @Nested
    @DisplayName("deleteComment")
    class DeleteComment {

        @Test
        @DisplayName("should soft delete comment successfully")
        void shouldSoftDeleteCommentSuccessfully() {
            // Given
            when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

            // When
            commentService.deleteComment(1L, 1L);

            // Then
            verify(commentRepository).save(testComment);
            assertThat(testComment.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("should throw ForbiddenException when not author")
        void shouldThrowWhenNotAuthor() {
            // Given
            when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

            // When/Then
            assertThatThrownBy(() -> commentService.deleteComment(1L, 2L))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessage("You can only delete your own comments");
        }
    }
}

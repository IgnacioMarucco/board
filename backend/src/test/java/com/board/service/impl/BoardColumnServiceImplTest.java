package com.board.service.impl;

import com.board.dto.boardcolumn.BoardColumnCreateRequest;
import com.board.dto.boardcolumn.BoardColumnResponse;
import com.board.dto.boardcolumn.BoardColumnUpdateRequest;
import com.board.entity.BoardColumn;
import com.board.entity.Project;
import com.board.entity.User;
import com.board.exception.NotFoundException;
import com.board.mapper.BoardColumnMapper;
import com.board.repository.BoardColumnRepository;
import com.board.repository.ProjectMemberRepository;
import com.board.repository.ProjectRepository;
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
 * Unit tests for BoardColumnServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class BoardColumnServiceImplTest {

    @Mock
    private BoardColumnRepository boardColumnRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BoardColumnMapper boardColumnMapper;

    @InjectMocks
    private BoardColumnServiceImpl boardColumnService;

    private User testUser;
    private Project testProject;
    private BoardColumn testColumn;
    private BoardColumnResponse testColumnResponse;

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

        testColumn = BoardColumn.builder()
                .id(1L)
                .name("To Do")
                .position(0)
                .wipLimit(5)
                .project(testProject)
                .build();

        testColumnResponse = BoardColumnResponse.builder()
                .id(1L)
                .name("To Do")
                .position(0)
                .wipLimit(5)
                .build();
    }

    @Nested
    @DisplayName("createColumn")
    class CreateColumn {

        @Test
        @DisplayName("should create column successfully")
        void shouldCreateColumnSuccessfully() {
            // Given
            BoardColumnCreateRequest request = BoardColumnCreateRequest.builder()
                    .name("To Do")
                    .position(0)
                    .wipLimit(5)
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUser(testProject, testUser)).thenReturn(true);
            when(boardColumnRepository.save(any(BoardColumn.class))).thenReturn(testColumn);
            when(boardColumnMapper.toResponse(any(BoardColumn.class))).thenReturn(testColumnResponse);

            // When
            BoardColumnResponse response = boardColumnService.createColumn(1L, request, 1L);

            // Then
            assertThat(response).isNotNull();
            verify(boardColumnRepository).save(any(BoardColumn.class));
        }
    }

    @Nested
    @DisplayName("getColumn")
    class GetColumn {

        @Test
        @DisplayName("should return column when user is member")
        void shouldReturnColumnWhenUserIsMember() {
            // Given
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUser(testProject, testUser)).thenReturn(true);
            when(boardColumnRepository.findById(1L)).thenReturn(Optional.of(testColumn));
            when(boardColumnMapper.toResponse(testColumn)).thenReturn(testColumnResponse);

            // When
            BoardColumnResponse response = boardColumnService.getColumn(1L, 1L, 1L);

            // Then
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("should throw NotFoundException when column not found")
        void shouldThrowWhenColumnNotFound() {
            // Given
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUser(testProject, testUser)).thenReturn(true);
            when(boardColumnRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> boardColumnService.getColumn(1L, 999L, 1L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Board column not found");
        }
    }

    @Nested
    @DisplayName("getColumnsForProject")
    class GetColumnsForProject {

        @Test
        @DisplayName("should return columns ordered by position")
        void shouldReturnColumnsOrderedByPosition() {
            // Given
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUser(testProject, testUser)).thenReturn(true);
            when(boardColumnRepository.findByProjectOrderByPositionAsc(testProject))
                    .thenReturn(List.of(testColumn));
            when(boardColumnMapper.toResponseList(any())).thenReturn(List.of(testColumnResponse));

            // When
            List<BoardColumnResponse> response = boardColumnService.getColumnsForProject(1L, 1L);

            // Then
            assertThat(response).hasSize(1);
        }
    }

    @Nested
    @DisplayName("updateColumn")
    class UpdateColumn {

        @Test
        @DisplayName("should update column successfully")
        void shouldUpdateColumnSuccessfully() {
            // Given
            BoardColumnUpdateRequest request = BoardColumnUpdateRequest.builder()
                    .name("In Progress")
                    .position(1)
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUser(testProject, testUser)).thenReturn(true);
            when(boardColumnRepository.findById(1L)).thenReturn(Optional.of(testColumn));
            when(boardColumnRepository.save(any(BoardColumn.class))).thenReturn(testColumn);
            when(boardColumnMapper.toResponse(any(BoardColumn.class))).thenReturn(testColumnResponse);

            // When
            BoardColumnResponse response = boardColumnService.updateColumn(1L, 1L, request, 1L);

            // Then
            assertThat(response).isNotNull();
            verify(boardColumnRepository).save(testColumn);
        }
    }

    @Nested
    @DisplayName("deleteColumn")
    class DeleteColumn {

        @Test
        @DisplayName("should soft delete column successfully")
        void shouldSoftDeleteColumnSuccessfully() {
            // Given
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(boardColumnRepository.findById(1L)).thenReturn(Optional.of(testColumn));

            // When
            boardColumnService.deleteColumn(1L, 1L, 1L);

            // Then
            verify(boardColumnRepository).save(testColumn);
            assertThat(testColumn.getDeletedAt()).isNotNull();
        }
    }
}

package com.board.service.impl;

import com.board.dto.epic.EpicCreateRequest;
import com.board.dto.epic.EpicResponse;
import com.board.dto.epic.EpicUpdateRequest;
import com.board.entity.Epic;
import com.board.entity.Project;
import com.board.entity.User;
import com.board.entity.enums.EpicStatus;
import com.board.exception.BadRequestException;
import com.board.exception.NotFoundException;
import com.board.mapper.EpicMapper;
import com.board.repository.EpicRepository;
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
 * Unit tests for EpicServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class EpicServiceImplTest {

    @Mock
    private EpicRepository epicRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EpicMapper epicMapper;

    @InjectMocks
    private EpicServiceImpl epicService;

    private User testUser;
    private Project testProject;
    private Epic testEpic;
    private EpicResponse testEpicResponse;
    private com.board.entity.ProjectMember ownerMember;

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

        ownerMember = com.board.entity.ProjectMember.builder()
                .id(10L)
                .project(testProject)
                .user(testUser)
                .role(com.board.entity.enums.Role.PRODUCT_OWNER)
                .build();

        testEpic = Epic.builder()
                .id(1L)
                .key("EPIC-1")
                .title("Epic Title")
                .description("Epic Description")
                .status(EpicStatus.BACKLOG)
                .project(testProject)
                .build();

        testEpicResponse = EpicResponse.builder()
                .id(1L)
                .key("EPIC-1")
                .title("Epic Title")
                .status(EpicStatus.BACKLOG)
                .build();
    }

    @Nested
    @DisplayName("createEpic")
    class CreateEpic {

        @Test
        @DisplayName("should create epic successfully")
        void shouldCreateEpicSuccessfully() {
            // Given
            EpicCreateRequest request = EpicCreateRequest.builder()
                    .key("epic-1")
                    .title("Epic Title")
                    .description("Description")
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(Optional.of(ownerMember));
            when(epicRepository.findByKey("epic-1")).thenReturn(Optional.empty());
            when(epicRepository.save(any(Epic.class))).thenReturn(testEpic);
            when(epicMapper.toResponse(any(Epic.class))).thenReturn(testEpicResponse);

            // When
            EpicResponse response = epicService.createEpic(1L, request, 1L);

            // Then
            assertThat(response).isNotNull();
            verify(epicRepository).save(any(Epic.class));
        }

        @Test
        @DisplayName("should throw BadRequestException when key already exists")
        void shouldThrowWhenKeyExists() {
            // Given
            EpicCreateRequest request = EpicCreateRequest.builder()
                    .key("EPIC-1")
                    .title("Title")
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(Optional.of(ownerMember));
            when(epicRepository.findByKey("EPIC-1")).thenReturn(Optional.of(testEpic));

            // When/Then
            assertThatThrownBy(() -> epicService.createEpic(1L, request, 1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Epic key already exists");
        }
    }

    @Nested
    @DisplayName("getEpic")
    class GetEpic {

        @Test
        @DisplayName("should return epic when user is member")
        void shouldReturnEpicWhenUserIsMember() {
            // Given
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(true);
            when(epicRepository.findById(1L)).thenReturn(Optional.of(testEpic));
            when(epicMapper.toResponse(testEpic)).thenReturn(testEpicResponse);

            // When
            EpicResponse response = epicService.getEpic(1L, 1L, 1L);

            // Then
            assertThat(response).isNotNull();
        }

        @Test
        @DisplayName("should throw NotFoundException when epic not found")
        void shouldThrowWhenEpicNotFound() {
            // Given
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(true);
            when(epicRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> epicService.getEpic(1L, 999L, 1L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Epic not found");
        }
    }

    @Nested
    @DisplayName("updateEpic")
    class UpdateEpic {

        @Test
        @DisplayName("should update epic successfully")
        void shouldUpdateEpicSuccessfully() {
            // Given
            EpicUpdateRequest request = EpicUpdateRequest.builder()
                    .title("Updated Title")
                    .status(EpicStatus.IN_PROGRESS)
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(Optional.of(ownerMember));
            when(epicRepository.findById(1L)).thenReturn(Optional.of(testEpic));
            when(epicRepository.save(any(Epic.class))).thenReturn(testEpic);
            when(epicMapper.toResponse(any(Epic.class))).thenReturn(testEpicResponse);

            // When
            EpicResponse response = epicService.updateEpic(1L, 1L, request, 1L);

            // Then
            assertThat(response).isNotNull();
            verify(epicRepository).save(testEpic);
        }
    }

    @Nested
    @DisplayName("deleteEpic")
    class DeleteEpic {

        @Test
        @DisplayName("should soft delete epic successfully")
        void shouldSoftDeleteEpicSuccessfully() {
            // Given
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(Optional.of(ownerMember));
            when(epicRepository.findById(1L)).thenReturn(Optional.of(testEpic));

            // When
            epicService.deleteEpic(1L, 1L, 1L);

            // Then
            verify(epicRepository).save(testEpic);
            assertThat(testEpic.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("getEpicsForProject")
    class GetEpicsForProject {

        @Test
        @DisplayName("should return list of epics")
        void shouldReturnListOfEpics() {
            // Given
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(true);
            when(epicRepository.findByProject(testProject)).thenReturn(List.of(testEpic));
            when(epicMapper.toResponseList(any())).thenReturn(List.of(testEpicResponse));

            // When
            List<EpicResponse> response = epicService.getEpicsForProject(1L, 1L);

            // Then
            assertThat(response).hasSize(1);
        }
    }
}

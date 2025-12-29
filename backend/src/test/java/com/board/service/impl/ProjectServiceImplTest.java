package com.board.service.impl;

import com.board.dto.project.ProjectCreateRequest;
import com.board.dto.project.ProjectMemberRequest;
import com.board.dto.project.ProjectMemberResponse;
import com.board.dto.project.ProjectResponse;
import com.board.dto.project.ProjectUpdateRequest;
import com.board.entity.Project;
import com.board.entity.ProjectMember;
import com.board.entity.User;
import com.board.entity.enums.BoardTemplate;
import com.board.entity.enums.Role;
import com.board.exception.BadRequestException;
import com.board.exception.ForbiddenException;
import com.board.exception.NotFoundException;
import com.board.mapper.ProjectMapper;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ProjectServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private BoardColumnRepository boardColumnRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private User testUser;
    private Project testProject;
    private ProjectMember testMember;
    private ProjectResponse testProjectResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("owner@example.com")
                .firstName("Test")
                .lastName("Owner")
                .build();

        testProject = Project.builder()
                .id(1L)
                .key("TEST")
                .name("Test Project")
                .description("Test Description")
                .boardTemplate(BoardTemplate.SCRUM_BASIC)
                .sprintDurationWeeks(2)
                .owner(testUser)
                .build();

        testMember = ProjectMember.builder()
                .id(1L)
                .project(testProject)
                .user(testUser)
                .role(Role.PRODUCT_OWNER)
                .joinedAt(LocalDateTime.now())
                .build();

        testProjectResponse = ProjectResponse.builder()
                .id(1L)
                .key("TEST")
                .name("Test Project")
                .build();
    }

    @Nested
    @DisplayName("createProject")
    class CreateProject {

        @Test
        @DisplayName("should create project successfully")
        void shouldCreateProjectSuccessfully() {
            // Given
            ProjectCreateRequest request = ProjectCreateRequest.builder()
                    .key("NEW")
                    .name("New Project")
                    .description("Description")
                    .build();

            when(projectRepository.existsByKey("NEW")).thenReturn(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectRepository.save(any(Project.class))).thenReturn(testProject);
            when(boardColumnRepository.countByProject(testProject)).thenReturn(0);
            when(boardColumnRepository.saveAll(any())).thenReturn(List.of());
            when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(testMember);
            when(projectMapper.toResponse(any(Project.class))).thenReturn(testProjectResponse);

            // When
            ProjectResponse response = projectService.createProject(request, 1L);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getKey()).isEqualTo("TEST");
            verify(projectRepository).save(any(Project.class));
            verify(boardColumnRepository).saveAll(any());
            verify(projectMemberRepository).save(any(ProjectMember.class));
        }

        @Test
        @DisplayName("should throw BadRequestException when key already exists")
        void shouldThrowWhenKeyExists() {
            // Given
            ProjectCreateRequest request = ProjectCreateRequest.builder()
                    .key("EXISTING")
                    .name("Project")
                    .build();

            when(projectRepository.existsByKey("EXISTING")).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> projectService.createProject(request, 1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Project key already exists");
        }
    }

    @Nested
    @DisplayName("getProject")
    class GetProject {

        @Test
        @DisplayName("should return project when user is member")
        void shouldReturnProjectWhenUserIsMember() {
            // Given
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(true);
            when(projectMapper.toResponse(testProject)).thenReturn(testProjectResponse);

            // When
            ProjectResponse response = projectService.getProject(1L, 1L);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getKey()).isEqualTo("TEST");
        }

        @Test
        @DisplayName("should throw NotFoundException when project not found")
        void shouldThrowWhenProjectNotFound() {
            // Given
            when(projectRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> projectService.getProject(999L, 1L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Project not found");
        }

        @Test
        @DisplayName("should throw ForbiddenException when user is not member")
        void shouldThrowWhenUserNotMember() {
            // Given
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(any(), any()))
                    .thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> projectService.getProject(1L, 2L))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessage("You are not a member of this project");
        }
    }

    @Nested
    @DisplayName("updateProject")
    class UpdateProject {

        @Test
        @DisplayName("should update project successfully")
        void shouldUpdateProjectSuccessfully() {
            // Given
            ProjectUpdateRequest request = ProjectUpdateRequest.builder()
                    .name("Updated Name")
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(Optional.of(testMember));
            when(projectRepository.save(any(Project.class))).thenReturn(testProject);
            when(projectMapper.toResponse(any(Project.class))).thenReturn(testProjectResponse);

            // When
            ProjectResponse response = projectService.updateProject(1L, request, 1L);

            // Then
            assertThat(response).isNotNull();
            verify(projectRepository).save(testProject);
        }

        @Test
        @DisplayName("should throw ForbiddenException when user is not owner")
        void shouldThrowWhenUserNotAllowed() {
            // Given
            ProjectUpdateRequest request = ProjectUpdateRequest.builder()
                    .name("Updated")
                    .build();

            User devUser = User.builder().id(2L).email("dev@example.com").build();
            ProjectMember devMember = ProjectMember.builder()
                    .id(2L)
                    .project(testProject)
                    .user(devUser)
                    .role(Role.DEVELOPER)
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(2L)).thenReturn(Optional.of(devUser));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(testProject, devUser))
                    .thenReturn(Optional.of(devMember));

            // When/Then
            assertThatThrownBy(() -> projectService.updateProject(1L, request, 2L))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessage("You are not allowed to perform this action");
        }
    }

    @Nested
    @DisplayName("deleteProject")
    class DeleteProject {

        @Test
        @DisplayName("should soft delete project successfully")
        void shouldSoftDeleteProjectSuccessfully() {
            // Given
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

            // When
            projectService.deleteProject(1L, 1L);

            // Then
            verify(projectRepository).save(testProject);
            assertThat(testProject.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("addMember")
    class AddMember {

        @Test
        @DisplayName("should add member successfully")
        void shouldAddMemberSuccessfully() {
            // Given
            User newUser = User.builder().id(2L).email("new@example.com").build();
            ProjectMemberRequest request = ProjectMemberRequest.builder()
                    .userId(2L)
                    .role(Role.DEVELOPER)
                    .build();

            ProjectMemberResponse memberResponse = ProjectMemberResponse.builder()
                    .userId(2L)
                    .role(Role.DEVELOPER)
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(Optional.of(testMember));
            when(userRepository.findById(2L)).thenReturn(Optional.of(newUser));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(testProject, newUser))
                    .thenReturn(false);
            when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(testMember);
            when(projectMapper.toMemberResponse(any(ProjectMember.class))).thenReturn(memberResponse);

            // When
            ProjectMemberResponse response = projectService.addMember(1L, request, 1L);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getRole()).isEqualTo(Role.DEVELOPER);
        }

        @Test
        @DisplayName("should throw BadRequestException when user already member")
        void shouldThrowWhenUserAlreadyMember() {
            // Given
            User existingUser = User.builder().id(2L).build();
            ProjectMemberRequest request = ProjectMemberRequest.builder()
                    .userId(2L)
                    .role(Role.DEVELOPER)
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(Optional.of(testMember));
            when(userRepository.findById(2L)).thenReturn(Optional.of(existingUser));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(testProject, existingUser))
                    .thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> projectService.addMember(1L, request, 1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("User is already a member of this project");
        }
    }

    @Nested
    @DisplayName("removeMember")
    class RemoveMember {

        @Test
        @DisplayName("should remove member successfully")
        void shouldRemoveMemberSuccessfully() {
            // Given
            User memberUser = User.builder().id(2L).build();
            ProjectMember member = ProjectMember.builder()
                    .id(2L)
                    .project(testProject)
                    .user(memberUser)
                    .role(Role.DEVELOPER)
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(Optional.of(testMember));
            when(userRepository.findById(2L)).thenReturn(Optional.of(memberUser));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(testProject, memberUser))
                    .thenReturn(Optional.of(member));
            when(projectMemberRepository.findByProjectAndRoleAndDeletedAtIsNull(testProject, Role.PRODUCT_OWNER))
                    .thenReturn(List.of(testMember));
            when(projectMemberRepository.findByProjectAndRoleAndDeletedAtIsNull(testProject, Role.SCRUM_MASTER))
                    .thenReturn(List.of(ProjectMember.builder().id(3L).role(Role.SCRUM_MASTER).build()));
            when(projectMemberRepository.findByProjectAndRoleAndDeletedAtIsNull(testProject, Role.DEVELOPER))
                    .thenReturn(List.of(member, ProjectMember.builder().id(4L).role(Role.DEVELOPER).build()));

            // When
            projectService.removeMember(1L, 2L, 1L);

            // Then
            verify(projectMemberRepository).save(member);
        }

        @Test
        @DisplayName("should throw BadRequestException when trying to remove owner")
        void shouldThrowWhenRemovingOwner() {
            // Given
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(Optional.of(testMember));

            // When/Then
            assertThatThrownBy(() -> projectService.removeMember(1L, 1L, 1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Cannot remove the project owner");
        }
    }

    @Nested
    @DisplayName("getProjectsForUser")
    class GetProjectsForUser {

        @Test
        @DisplayName("should return projects for user")
        void shouldReturnProjectsForUser() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.findByUserAndDeletedAtIsNull(testUser))
                    .thenReturn(List.of(testMember));
            when(projectMapper.toResponseList(any())).thenReturn(List.of(testProjectResponse));

            // When
            List<ProjectResponse> response = projectService.getProjectsForUser(1L);

            // Then
            assertThat(response).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getMembers")
    class GetMembers {

        @Test
        @DisplayName("should return all members of project")
        void shouldReturnAllMembers() {
            // Given
            User member2 = User.builder().id(2L).email("member@example.com").build();
            ProjectMember member2Entity = ProjectMember.builder()
                    .id(2L)
                    .project(testProject)
                    .user(member2)
                    .role(Role.DEVELOPER)
                    .build();

            ProjectMemberResponse memberResponse2 = ProjectMemberResponse.builder()
                    .userId(2L)
                    .role(Role.DEVELOPER)
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(true);
            when(projectMemberRepository.findByProjectAndDeletedAtIsNull(testProject))
                    .thenReturn(List.of(testMember, member2Entity));
            when(projectMapper.toMemberResponseList(any())).thenReturn(List.of(
                    ProjectMemberResponse.builder().userId(1L).role(Role.PRODUCT_OWNER).build(),
                    memberResponse2
            ));

            // When
            List<ProjectMemberResponse> response = projectService.getMembers(1L, 1L);

            // Then
            assertThat(response).hasSize(2);
            verify(projectMemberRepository).findByProjectAndDeletedAtIsNull(testProject);
        }

        @Test
        @DisplayName("should throw ForbiddenException when user is not member")
        void shouldThrowWhenUserNotMember() {
            // Given
            when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
            when(userRepository.findById(2L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(any(), any()))
                    .thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> projectService.getMembers(1L, 2L))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessage("You are not a member of this project");
        }
    }
}

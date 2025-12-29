package com.board.service.impl;

import com.board.dto.projectmember.ProjectMemberInviteRequest;
import com.board.dto.projectmember.ProjectMemberResponse;
import com.board.dto.projectmember.ProjectMemberUpdateRequest;
import com.board.entity.Project;
import com.board.entity.ProjectMember;
import com.board.entity.User;
import com.board.entity.enums.Role;
import com.board.exception.ConflictException;
import com.board.exception.ForbiddenException;
import com.board.mapper.ProjectMemberMapper;
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
 * Unit tests for ProjectMemberServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class ProjectMemberServiceImplTest {

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMemberMapper projectMemberMapper;

    @InjectMocks
    private ProjectMemberServiceImpl projectMemberService;

    private User owner;
    private User memberUser;
    private Project project;
    private ProjectMember projectMember;
    private ProjectMember ownerMember;
    private ProjectMemberResponse memberResponse;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .id(1L)
                .email("owner@example.com")
                .firstName("Owner")
                .lastName("User")
                .build();

        memberUser = User.builder()
                .id(2L)
                .email("member@example.com")
                .firstName("Member")
                .lastName("User")
                .build();

        project = Project.builder()
                .id(1L)
                .key("TEST")
                .name("Test Project")
                .owner(owner)
                .build();

        projectMember = ProjectMember.builder()
                .id(1L)
                .user(memberUser)
                .project(project)
                .role(Role.DEVELOPER)
                .build();

        ownerMember = ProjectMember.builder()
                .id(10L)
                .user(owner)
                .project(project)
                .role(Role.PRODUCT_OWNER)
                .build();

        memberResponse = ProjectMemberResponse.builder()
                .id(1L)
                .userId(2L)
                .userEmail("member@example.com")
                .role(Role.DEVELOPER)
                .build();
    }

    @Nested
    @DisplayName("inviteMember")
    class InviteMember {

        @Test
        @DisplayName("should invite member successfully")
        void shouldInviteMemberSuccessfully() {
            // Given
            ProjectMemberInviteRequest request = ProjectMemberInviteRequest.builder()
                    .userEmail("member@example.com")
                    .role(Role.DEVELOPER)
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
            when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(project, owner))
                    .thenReturn(Optional.of(ownerMember));
            when(userRepository.findByEmail("member@example.com"))
                    .thenReturn(Optional.of(memberUser));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(project, memberUser))
                    .thenReturn(false);
            when(projectMemberRepository.save(any(ProjectMember.class))).thenReturn(projectMember);
            when(projectMemberMapper.toResponse(any(ProjectMember.class)))
                    .thenReturn(memberResponse);

            // When
            ProjectMemberResponse response = projectMemberService.inviteMember(1L, request, 1L);

            // Then
            assertThat(response).isNotNull();
            verify(projectMemberRepository).save(any(ProjectMember.class));
        }

        @Test
        @DisplayName("should throw ConflictException when user already member")
        void shouldThrowWhenUserAlreadyMember() {
            // Given
            ProjectMemberInviteRequest request = ProjectMemberInviteRequest.builder()
                    .userEmail("member@example.com")
                    .role(Role.DEVELOPER)
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
            when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(project, owner))
                    .thenReturn(Optional.of(ownerMember));
            when(userRepository.findByEmail("member@example.com"))
                    .thenReturn(Optional.of(memberUser));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(project, memberUser))
                    .thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> projectMemberService.inviteMember(1L, request, 1L))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("User is already a member of this project");
        }

        @Test
        @DisplayName("should throw ForbiddenException when not owner")
        void shouldThrowWhenNotOwner() {
            // Given
            ProjectMemberInviteRequest request = ProjectMemberInviteRequest.builder()
                    .userEmail("member@example.com")
                    .role(Role.DEVELOPER)
                    .build();

            User devUser = User.builder().id(3L).email("dev@example.com").build();
            ProjectMember devMember = ProjectMember.builder()
                    .id(3L)
                    .user(devUser)
                    .project(project)
                    .role(Role.DEVELOPER)
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
            when(userRepository.findById(3L)).thenReturn(Optional.of(devUser));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(project, devUser))
                    .thenReturn(Optional.of(devMember));

            // When/Then
            assertThatThrownBy(() -> projectMemberService.inviteMember(1L, request, 3L))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessage("You are not allowed to perform this action");
        }
    }

    @Nested
    @DisplayName("getProjectMembers")
    class GetProjectMembers {

        @Test
        @DisplayName("should return all project members")
        void shouldReturnAllProjectMembers() {
            // Given
            when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
            when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(project, owner))
                    .thenReturn(true);
            when(projectMemberRepository.findByProjectAndDeletedAtIsNull(project))
                    .thenReturn(List.of(projectMember));
            when(projectMemberMapper.toResponseList(any())).thenReturn(List.of(memberResponse));

            // When
            List<ProjectMemberResponse> response = projectMemberService.getProjectMembers(1L, 1L);

            // Then
            assertThat(response).hasSize(1);
        }
    }

    @Nested
    @DisplayName("updateMemberRole")
    class UpdateMemberRole {

        @Test
        @DisplayName("should update member role successfully")
        void shouldUpdateMemberRoleSuccessfully() {
            // Given
            ProjectMemberUpdateRequest request = ProjectMemberUpdateRequest.builder()
                    .role(Role.SCRUM_MASTER)
                    .build();

            when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
            when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(project, owner))
                    .thenReturn(Optional.of(ownerMember));
            when(projectMemberRepository.findById(1L)).thenReturn(Optional.of(projectMember));
            when(projectMemberRepository.findByProjectAndRoleAndDeletedAtIsNull(project, Role.SCRUM_MASTER))
                    .thenReturn(List.of());
            when(projectMemberRepository.findByProjectAndRoleAndDeletedAtIsNull(project, Role.PRODUCT_OWNER))
                    .thenReturn(List.of(ownerMember));
            when(projectMemberRepository.findByProjectAndRoleAndDeletedAtIsNull(project, Role.DEVELOPER))
                    .thenReturn(List.of(projectMember,
                            ProjectMember.builder().id(5L).role(Role.DEVELOPER).build()));
            when(projectMemberRepository.save(any(ProjectMember.class)))
                    .thenReturn(projectMember);
            when(projectMemberMapper.toResponse(any(ProjectMember.class)))
                    .thenReturn(memberResponse);

            // When
            ProjectMemberResponse response = projectMemberService.updateMemberRole(1L, 1L,
                    request, 1L);

            // Then
            assertThat(response).isNotNull();
            verify(projectMemberRepository).save(projectMember);
        }
    }

    @Nested
    @DisplayName("removeMember")
    class RemoveMember {

        @Test
        @DisplayName("should soft delete member successfully")
        void shouldSoftDeleteMemberSuccessfully() {
            // Given
            when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
            when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
            when(projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(project, owner))
                    .thenReturn(Optional.of(ownerMember));
            when(projectMemberRepository.findById(1L)).thenReturn(Optional.of(projectMember));
            when(projectMemberRepository.findByProjectAndRoleAndDeletedAtIsNull(project, Role.PRODUCT_OWNER))
                    .thenReturn(List.of(ownerMember, ProjectMember.builder().id(4L).role(Role.PRODUCT_OWNER).build()));
            when(projectMemberRepository.findByProjectAndRoleAndDeletedAtIsNull(project, Role.SCRUM_MASTER))
                    .thenReturn(List.of(ProjectMember.builder().id(3L).role(Role.SCRUM_MASTER).build()));
            when(projectMemberRepository.findByProjectAndRoleAndDeletedAtIsNull(project, Role.DEVELOPER))
                    .thenReturn(List.of(projectMember, ProjectMember.builder().id(5L).role(Role.DEVELOPER).build()));

            // When
            projectMemberService.removeMember(1L, 1L, 1L);

            // Then
            verify(projectMemberRepository).save(projectMember);
            assertThat(projectMember.getDeletedAt()).isNotNull();
        }
    }
}

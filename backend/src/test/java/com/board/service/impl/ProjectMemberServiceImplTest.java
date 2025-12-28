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
import com.board.exception.NotFoundException;
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
            when(userRepository.findByEmail("member@example.com"))
                    .thenReturn(Optional.of(memberUser));
            when(projectMemberRepository.existsByProjectAndUser(project, memberUser))
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
            when(userRepository.findByEmail("member@example.com"))
                    .thenReturn(Optional.of(memberUser));
            when(projectMemberRepository.existsByProjectAndUser(project, memberUser))
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

            when(projectRepository.findById(1L)).thenReturn(Optional.of(project));

            // When/Then
            assertThatThrownBy(() -> projectMemberService.inviteMember(1L, request, 999L))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessage("Only the project owner can perform this action");
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
            when(projectMemberRepository.findByProject(project))
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
            when(projectMemberRepository.findById(1L)).thenReturn(Optional.of(projectMember));
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
            when(projectMemberRepository.findById(1L)).thenReturn(Optional.of(projectMember));

            // When
            projectMemberService.removeMember(1L, 1L, 1L);

            // Then
            verify(projectMemberRepository).save(projectMember);
            assertThat(projectMember.getDeletedAt()).isNotNull();
        }
    }
}

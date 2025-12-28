package com.board.service.impl;

import com.board.dto.projectmember.ProjectMemberInviteRequest;
import com.board.dto.projectmember.ProjectMemberResponse;
import com.board.dto.projectmember.ProjectMemberUpdateRequest;
import com.board.entity.Project;
import com.board.entity.ProjectMember;
import com.board.entity.User;
import com.board.exception.ConflictException;
import com.board.exception.ForbiddenException;
import com.board.exception.NotFoundException;
import com.board.mapper.ProjectMemberMapper;
import com.board.repository.ProjectMemberRepository;
import com.board.repository.ProjectRepository;
import com.board.repository.UserRepository;
import com.board.service.ProjectMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of ProjectMemberService.
 */
@Service
@RequiredArgsConstructor
public class ProjectMemberServiceImpl implements ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberMapper projectMemberMapper;

    @Override
    @Transactional
    public ProjectMemberResponse inviteMember(Long projectId,
            ProjectMemberInviteRequest request, Long userId) {
        Project project = findProjectById(projectId);
        validateOwnership(project, userId);

        User userToInvite = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new NotFoundException("User not found with email: "
                        + request.getUserEmail()));

        // Check if user is already a member
        if (projectMemberRepository.existsByProjectAndUser(project, userToInvite)) {
            throw new ConflictException("User is already a member of this project");
        }

        // Don't allow inviting the owner as a member
        if (project.getOwner().getId().equals(userToInvite.getId())) {
            throw new ConflictException("Project owner is already part of the project");
        }

        ProjectMember member = ProjectMember.builder()
                .user(userToInvite)
                .project(project)
                .role(request.getRole())
                .joinedAt(LocalDateTime.now())
                .build();

        member = projectMemberRepository.save(member);
        return projectMemberMapper.toResponse(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getProjectMembers(Long projectId, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        List<ProjectMember> members = projectMemberRepository.findByProject(project);
        return projectMemberMapper.toResponseList(members);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectMemberResponse getProjectMember(Long projectId, Long memberId, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        ProjectMember member = findMemberById(memberId);
        validateMemberBelongsToProject(member, project);

        return projectMemberMapper.toResponse(member);
    }

    @Override
    @Transactional
    public ProjectMemberResponse updateMemberRole(Long projectId, Long memberId,
            ProjectMemberUpdateRequest request, Long userId) {
        Project project = findProjectById(projectId);
        validateOwnership(project, userId);

        ProjectMember member = findMemberById(memberId);
        validateMemberBelongsToProject(member, project);

        member.setRole(request.getRole());
        member = projectMemberRepository.save(member);

        return projectMemberMapper.toResponse(member);
    }

    @Override
    @Transactional
    public void removeMember(Long projectId, Long memberId, Long userId) {
        Project project = findProjectById(projectId);
        validateOwnership(project, userId);

        ProjectMember member = findMemberById(memberId);
        validateMemberBelongsToProject(member, project);

        member.softDelete();
        projectMemberRepository.save(member);
    }

    private Project findProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Project not found"));
    }

    private ProjectMember findMemberById(Long memberId) {
        return projectMemberRepository.findById(memberId)
                .filter(m -> m.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Project member not found"));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private void validateOwnership(Project project, Long userId) {
        if (!project.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Only the project owner can perform this action");
        }
    }

    private void validateMembership(Project project, Long userId) {
        User user = findUserById(userId);
        if (!project.getOwner().getId().equals(userId)
                && !projectMemberRepository.existsByProjectAndUser(project, user)) {
            throw new ForbiddenException("You are not a member of this project");
        }
    }

    private void validateMemberBelongsToProject(ProjectMember member, Project project) {
        if (!member.getProject().getId().equals(project.getId())) {
            throw new NotFoundException("Project member not found in this project");
        }
    }
}

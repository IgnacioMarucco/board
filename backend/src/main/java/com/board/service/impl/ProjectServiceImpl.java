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
import com.board.repository.ProjectMemberRepository;
import com.board.repository.ProjectRepository;
import com.board.repository.UserRepository;
import com.board.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of ProjectService.
 */
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;

    @Override
    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest request, Long ownerId) {
        if (projectRepository.existsByKey(request.getKey().toUpperCase())) {
            throw new BadRequestException("Project key already exists");
        }

        User owner = findUserById(ownerId);

        Project project = Project.builder()
                .key(request.getKey().toUpperCase())
                .name(request.getName())
                .description(request.getDescription())
                .boardTemplate(request.getBoardTemplate() != null
                        ? request.getBoardTemplate()
                        : BoardTemplate.SCRUM_BASIC)
                .sprintDurationWeeks(request.getSprintDurationWeeks() != null
                        ? request.getSprintDurationWeeks()
                        : 2)
                .owner(owner)
                .build();

        project = projectRepository.save(project);

        // Add owner as PRODUCT_OWNER member
        ProjectMember ownerMember = ProjectMember.builder()
                .project(project)
                .user(owner)
                .role(Role.PRODUCT_OWNER)
                .joinedAt(LocalDateTime.now())
                .build();
        projectMemberRepository.save(ownerMember);

        return projectMapper.toResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProject(Long projectId, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);
        return projectMapper.toResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsForUser(Long userId) {
        User user = findUserById(userId);
        List<ProjectMember> memberships = projectMemberRepository.findByUser(user);
        List<Project> projects = memberships.stream()
                .map(ProjectMember::getProject)
                .filter(p -> p.getDeletedAt() == null)
                .toList();
        return projectMapper.toResponseList(projects);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(Long projectId, ProjectUpdateRequest request, Long userId) {
        Project project = findProjectById(projectId);
        validateOwnership(project, userId);

        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getBoardTemplate() != null) {
            project.setBoardTemplate(request.getBoardTemplate());
        }
        if (request.getSprintDurationWeeks() != null) {
            project.setSprintDurationWeeks(request.getSprintDurationWeeks());
        }

        project = projectRepository.save(project);
        return projectMapper.toResponse(project);
    }

    @Override
    @Transactional
    public void deleteProject(Long projectId, Long userId) {
        Project project = findProjectById(projectId);
        validateOwnership(project, userId);
        project.softDelete();
        projectRepository.save(project);
    }

    @Override
    @Transactional
    public ProjectMemberResponse addMember(Long projectId, ProjectMemberRequest request, Long userId) {
        Project project = findProjectById(projectId);
        validateOwnership(project, userId);

        User newMember = findUserById(request.getUserId());

        if (projectMemberRepository.existsByProjectAndUser(project, newMember)) {
            throw new BadRequestException("User is already a member of this project");
        }

        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(newMember)
                .role(request.getRole())
                .joinedAt(LocalDateTime.now())
                .build();

        member = projectMemberRepository.save(member);
        return projectMapper.toMemberResponse(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getMembers(Long projectId, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);
        List<ProjectMember> members = projectMemberRepository.findByProject(project);
        return projectMapper.toMemberResponseList(members);
    }

    @Override
    @Transactional
    public void removeMember(Long projectId, Long memberId, Long userId) {
        Project project = findProjectById(projectId);
        validateOwnership(project, userId);

        if (project.getOwner().getId().equals(memberId)) {
            throw new BadRequestException("Cannot remove the project owner");
        }

        User memberUser = findUserById(memberId);
        ProjectMember member = projectMemberRepository.findByProjectAndUser(project, memberUser)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        projectMemberRepository.delete(member);
    }

    private Project findProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Project not found"));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private void validateMembership(Project project, Long userId) {
        User user = findUserById(userId);
        if (!projectMemberRepository.existsByProjectAndUser(project, user)) {
            throw new ForbiddenException("You are not a member of this project");
        }
    }

    private void validateOwnership(Project project, Long userId) {
        if (!project.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Only the project owner can perform this action");
        }
    }
}

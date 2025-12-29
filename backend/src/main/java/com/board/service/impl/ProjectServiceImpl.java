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
import com.board.entity.BoardColumn;
import com.board.exception.BadRequestException;
import com.board.exception.ForbiddenException;
import com.board.exception.NotFoundException;
import com.board.mapper.ProjectMapper;
import com.board.repository.BoardColumnRepository;
import com.board.repository.ProjectMemberRepository;
import com.board.repository.ProjectRepository;
import com.board.repository.UserRepository;
import com.board.service.ProjectService;
import com.board.util.BoardTemplateDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    private final BoardColumnRepository boardColumnRepository;

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

        initializeBoardColumns(project);

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
        List<ProjectMember> memberships = projectMemberRepository.findByUserAndDeletedAtIsNull(user);
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
        Role role = getRoleForProject(project, userId);

        if (request.getBoardTemplate() != null
                && request.getBoardTemplate() != project.getBoardTemplate()) {
            throw new BadRequestException("Board template cannot be changed in v1.0");
        }

        if (request.getName() != null) {
            requireRole(role, Role.PRODUCT_OWNER, Role.SCRUM_MASTER);
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            requireRole(role, Role.PRODUCT_OWNER, Role.SCRUM_MASTER);
            project.setDescription(request.getDescription());
        }
        if (request.getSprintDurationWeeks() != null) {
            requireRole(role, Role.SCRUM_MASTER);
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
        validateRole(project, userId, Role.PRODUCT_OWNER);

        User newMember = findUserById(request.getUserId());

        if (projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(project, newMember)) {
            throw new BadRequestException("User is already a member of this project");
        }

        validateRoleAvailability(project, request.getRole());

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
        List<ProjectMember> members = projectMemberRepository.findByProjectAndDeletedAtIsNull(project);
        return projectMapper.toMemberResponseList(members);
    }

    @Override
    @Transactional
    public void removeMember(Long projectId, Long memberId, Long userId) {
        Project project = findProjectById(projectId);
        validateRole(project, userId, Role.PRODUCT_OWNER);

        if (project.getOwner().getId().equals(memberId)) {
            throw new BadRequestException("Cannot remove the project owner");
        }

        User memberUser = findUserById(memberId);
        ProjectMember member = projectMemberRepository
                .findByProjectAndUserAndDeletedAtIsNull(project, memberUser)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        validateRoleCountsAfterRemoval(project, member);
        member.softDelete();
        projectMemberRepository.save(member);
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
        if (!projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(project, user)) {
            throw new ForbiddenException("You are not a member of this project");
        }
    }

    private void validateOwnership(Project project, Long userId) {
        if (!project.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Only the project owner can perform this action");
        }
    }

    private ProjectMember getMember(Project project, Long userId) {
        User user = findUserById(userId);
        return projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(project, user)
                .orElseThrow(() -> new ForbiddenException("You are not a member of this project"));
    }

    private Role getRoleForProject(Project project, Long userId) {
        return getMember(project, userId).getRole();
    }

    private void validateRole(Project project, Long userId, Role... allowedRoles) {
        Role role = getRoleForProject(project, userId);
        requireRole(role, allowedRoles);
    }

    private void requireRole(Role role, Role... allowedRoles) {
        for (Role allowed : allowedRoles) {
            if (allowed == role) {
                return;
            }
        }
        throw new ForbiddenException("You are not allowed to perform this action");
    }

    private void validateRoleAvailability(Project project, Role role) {
        if (role == Role.SCRUM_MASTER) {
            List<ProjectMember> scrumMasters = projectMemberRepository
                    .findByProjectAndRoleAndDeletedAtIsNull(project, Role.SCRUM_MASTER);
            if (!scrumMasters.isEmpty()) {
                throw new BadRequestException("Project already has a Scrum Master");
            }
        }
    }

    private void validateRoleCountsAfterRemoval(Project project, ProjectMember member) {
        List<ProjectMember> productOwners = projectMemberRepository
                .findByProjectAndRoleAndDeletedAtIsNull(project, Role.PRODUCT_OWNER);
        List<ProjectMember> scrumMasters = projectMemberRepository
                .findByProjectAndRoleAndDeletedAtIsNull(project, Role.SCRUM_MASTER);
        List<ProjectMember> developers = projectMemberRepository
                .findByProjectAndRoleAndDeletedAtIsNull(project, Role.DEVELOPER);

        if (member.getRole() == Role.PRODUCT_OWNER && productOwners.size() <= 1) {
            throw new BadRequestException("Project must have at least one Product Owner");
        }
        if (member.getRole() == Role.SCRUM_MASTER && scrumMasters.size() <= 1) {
            throw new BadRequestException("Project must have exactly one Scrum Master");
        }
        if (member.getRole() == Role.DEVELOPER && developers.size() <= 1) {
            throw new BadRequestException("Project must have at least one Developer");
        }
    }

    private void initializeBoardColumns(Project project) {
        if (boardColumnRepository.countByProject(project) > 0) {
            return;
        }

        List<String> columnNames = BoardTemplateDefaults.getColumns(project.getBoardTemplate());
        List<BoardColumn> columns = new ArrayList<>();
        int position = 0;
        for (String name : columnNames) {
            columns.add(BoardColumn.builder()
                    .name(name)
                    .position(position++)
                    .project(project)
                    .build());
        }
        boardColumnRepository.saveAll(columns);
    }
}

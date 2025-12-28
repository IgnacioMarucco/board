package com.board.service.impl;

import com.board.dto.sprint.SprintCreateRequest;
import com.board.dto.sprint.SprintResponse;
import com.board.dto.sprint.SprintUpdateRequest;
import com.board.entity.Project;
import com.board.entity.Sprint;
import com.board.entity.User;
import com.board.entity.enums.SprintStatus;
import com.board.exception.BadRequestException;
import com.board.exception.ForbiddenException;
import com.board.exception.NotFoundException;
import com.board.mapper.SprintMapper;
import com.board.repository.ProjectMemberRepository;
import com.board.repository.ProjectRepository;
import com.board.repository.SprintRepository;
import com.board.repository.UserRepository;
import com.board.service.SprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of SprintService.
 */
@Service
@RequiredArgsConstructor
public class SprintServiceImpl implements SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final SprintMapper sprintMapper;

    @Override
    @Transactional
    public SprintResponse createSprint(Long projectId, SprintCreateRequest request, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        Sprint sprint = Sprint.builder()
                .name(request.getName())
                .goal(request.getGoal())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .status(SprintStatus.PLANNING)
                .project(project)
                .build();

        sprint = sprintRepository.save(sprint);
        return sprintMapper.toResponse(sprint);
    }

    @Override
    @Transactional(readOnly = true)
    public SprintResponse getSprint(Long projectId, Long sprintId, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        Sprint sprint = findSprintById(sprintId);
        validateSprintBelongsToProject(sprint, project);

        return sprintMapper.toResponse(sprint);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SprintResponse> getSprintsForProject(Long projectId, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        List<Sprint> sprints = sprintRepository.findByProjectOrderByStartDateDesc(project);
        return sprintMapper.toResponseList(sprints);
    }

    @Override
    @Transactional
    public SprintResponse updateSprint(Long projectId, Long sprintId,
            SprintUpdateRequest request, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        Sprint sprint = findSprintById(sprintId);
        validateSprintBelongsToProject(sprint, project);

        if (sprint.getStatus() == SprintStatus.COMPLETED) {
            throw new BadRequestException("Cannot update a completed sprint");
        }

        if (request.getName() != null) {
            sprint.setName(request.getName());
        }
        if (request.getGoal() != null) {
            sprint.setGoal(request.getGoal());
        }
        if (request.getStartDate() != null) {
            sprint.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            if (request.getStartDate() != null
                    && request.getEndDate().isBefore(request.getStartDate())) {
                throw new BadRequestException("End date must be after start date");
            }
            sprint.setEndDate(request.getEndDate());
        }

        sprint = sprintRepository.save(sprint);
        return sprintMapper.toResponse(sprint);
    }

    @Override
    @Transactional
    public void deleteSprint(Long projectId, Long sprintId, Long userId) {
        Project project = findProjectById(projectId);
        validateOwnership(project, userId);

        Sprint sprint = findSprintById(sprintId);
        validateSprintBelongsToProject(sprint, project);

        if (sprint.getStatus() == SprintStatus.ACTIVE) {
            throw new BadRequestException("Cannot delete an active sprint");
        }

        sprint.softDelete();
        sprintRepository.save(sprint);
    }

    @Override
    @Transactional
    public SprintResponse startSprint(Long projectId, Long sprintId, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        Sprint sprint = findSprintById(sprintId);
        validateSprintBelongsToProject(sprint, project);

        if (sprint.getStatus() != SprintStatus.PLANNING) {
            throw new BadRequestException("Only sprints in PLANNING status can be started");
        }

        // Check if there's already an active sprint
        Optional<Sprint> activeSprint = sprintRepository
                .findByProjectAndStatus(project, SprintStatus.ACTIVE);
        if (activeSprint.isPresent()) {
            throw new BadRequestException("Cannot start a new sprint while another is active");
        }

        sprint.setStatus(SprintStatus.ACTIVE);
        sprint = sprintRepository.save(sprint);
        return sprintMapper.toResponse(sprint);
    }

    @Override
    @Transactional
    public SprintResponse completeSprint(Long projectId, Long sprintId, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        Sprint sprint = findSprintById(sprintId);
        validateSprintBelongsToProject(sprint, project);

        if (sprint.getStatus() != SprintStatus.ACTIVE) {
            throw new BadRequestException("Only active sprints can be completed");
        }

        sprint.setStatus(SprintStatus.COMPLETED);
        sprint = sprintRepository.save(sprint);
        return sprintMapper.toResponse(sprint);
    }

    private Project findProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Project not found"));
    }

    private Sprint findSprintById(Long sprintId) {
        return sprintRepository.findById(sprintId)
                .filter(s -> s.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Sprint not found"));
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

    private void validateSprintBelongsToProject(Sprint sprint, Project project) {
        if (!sprint.getProject().getId().equals(project.getId())) {
            throw new NotFoundException("Sprint not found in this project");
        }
    }
}

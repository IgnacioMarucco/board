package com.board.service.impl;

import com.board.dto.epic.EpicCreateRequest;
import com.board.dto.epic.EpicResponse;
import com.board.dto.epic.EpicUpdateRequest;
import com.board.entity.Epic;
import com.board.entity.Project;
import com.board.entity.User;
import com.board.entity.enums.EpicStatus;
import com.board.exception.BadRequestException;
import com.board.exception.ForbiddenException;
import com.board.exception.NotFoundException;
import com.board.mapper.EpicMapper;
import com.board.repository.EpicRepository;
import com.board.repository.ProjectMemberRepository;
import com.board.repository.ProjectRepository;
import com.board.repository.UserRepository;
import com.board.service.EpicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of EpicService.
 */
@Service
@RequiredArgsConstructor
public class EpicServiceImpl implements EpicService {

    private final EpicRepository epicRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final EpicMapper epicMapper;

    @Override
    @Transactional
    public EpicResponse createEpic(Long projectId, EpicCreateRequest request, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        if (epicRepository.findByKey(request.getKey()).isPresent()) {
            throw new BadRequestException("Epic key already exists");
        }

        Epic epic = Epic.builder()
                .key(request.getKey().toUpperCase())
                .title(request.getTitle())
                .description(request.getDescription())
                .status(request.getStatus() != null ? request.getStatus() : EpicStatus.BACKLOG)
                .project(project)
                .build();

        epic = epicRepository.save(epic);
        return epicMapper.toResponse(epic);
    }

    @Override
    @Transactional(readOnly = true)
    public EpicResponse getEpic(Long projectId, Long epicId, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        Epic epic = findEpicById(epicId);
        validateEpicBelongsToProject(epic, project);

        return epicMapper.toResponse(epic);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EpicResponse> getEpicsForProject(Long projectId, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        List<Epic> epics = epicRepository.findByProject(project);
        return epicMapper.toResponseList(epics);
    }

    @Override
    @Transactional
    public EpicResponse updateEpic(Long projectId, Long epicId,
            EpicUpdateRequest request, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        Epic epic = findEpicById(epicId);
        validateEpicBelongsToProject(epic, project);

        if (request.getTitle() != null) {
            epic.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            epic.setDescription(request.getDescription());
        }
        if (request.getStatus() != null) {
            epic.setStatus(request.getStatus());
        }

        epic = epicRepository.save(epic);
        return epicMapper.toResponse(epic);
    }

    @Override
    @Transactional
    public void deleteEpic(Long projectId, Long epicId, Long userId) {
        Project project = findProjectById(projectId);
        validateOwnership(project, userId);

        Epic epic = findEpicById(epicId);
        validateEpicBelongsToProject(epic, project);

        epic.softDelete();
        epicRepository.save(epic);
    }

    private Project findProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Project not found"));
    }

    private Epic findEpicById(Long epicId) {
        return epicRepository.findById(epicId)
                .filter(e -> e.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Epic not found"));
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

    private void validateEpicBelongsToProject(Epic epic, Project project) {
        if (!epic.getProject().getId().equals(project.getId())) {
            throw new NotFoundException("Epic not found in this project");
        }
    }
}

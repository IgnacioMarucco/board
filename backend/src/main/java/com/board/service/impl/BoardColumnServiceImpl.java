package com.board.service.impl;

import com.board.dto.boardcolumn.BoardColumnCreateRequest;
import com.board.dto.boardcolumn.BoardColumnResponse;
import com.board.dto.boardcolumn.BoardColumnUpdateRequest;
import com.board.entity.BoardColumn;
import com.board.entity.Project;
import com.board.entity.User;
import com.board.entity.enums.Role;
import com.board.exception.BadRequestException;
import com.board.exception.ForbiddenException;
import com.board.exception.NotFoundException;
import com.board.mapper.BoardColumnMapper;
import com.board.repository.BoardColumnRepository;
import com.board.repository.ProjectMemberRepository;
import com.board.repository.ProjectRepository;
import com.board.repository.UserRepository;
import com.board.service.BoardColumnService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of BoardColumnService.
 */
@Service
@RequiredArgsConstructor
public class BoardColumnServiceImpl implements BoardColumnService {

    private final BoardColumnRepository boardColumnRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final BoardColumnMapper boardColumnMapper;

    @Override
    @Transactional
    public BoardColumnResponse createColumn(Long projectId,
            BoardColumnCreateRequest request, Long userId) {
        throw new BadRequestException("Board columns are fixed by template in v1.0");
    }

    @Override
    @Transactional(readOnly = true)
    public BoardColumnResponse getColumn(Long projectId, Long columnId, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        BoardColumn column = findColumnById(columnId);
        validateColumnBelongsToProject(column, project);

        return boardColumnMapper.toResponse(column);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardColumnResponse> getColumnsForProject(Long projectId, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        List<BoardColumn> columns = boardColumnRepository.findByProjectOrderByPositionAsc(project);
        return boardColumnMapper.toResponseList(columns);
    }

    @Override
    @Transactional
    public BoardColumnResponse updateColumn(Long projectId, Long columnId,
            BoardColumnUpdateRequest request, Long userId) {
        Project project = findProjectById(projectId);
        validateRole(project, userId, Role.SCRUM_MASTER);

        BoardColumn column = findColumnById(columnId);
        validateColumnBelongsToProject(column, project);

        if (request.getName() != null || request.getPosition() != null) {
            throw new BadRequestException("Board columns are fixed by template in v1.0");
        }

        if (request.getWipLimit() == null) {
            throw new BadRequestException("WIP limit is required to update a column");
        }

        column.setWipLimit(request.getWipLimit());

        column = boardColumnRepository.save(column);
        return boardColumnMapper.toResponse(column);
    }

    @Override
    @Transactional
    public void deleteColumn(Long projectId, Long columnId, Long userId) {
        throw new BadRequestException("Board columns are fixed by template in v1.0");
    }

    private Project findProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Project not found"));
    }

    private BoardColumn findColumnById(Long columnId) {
        return boardColumnRepository.findById(columnId)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Board column not found"));
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

    private void validateRole(Project project, Long userId, Role... allowedRoles) {
        User user = findUserById(userId);
        Role role = projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(project, user)
                .map(com.board.entity.ProjectMember::getRole)
                .orElseThrow(() -> new ForbiddenException("You are not a member of this project"));

        for (Role allowed : allowedRoles) {
            if (allowed == role) {
                return;
            }
        }
        throw new ForbiddenException("You are not allowed to perform this action");
    }

    private void validateColumnBelongsToProject(BoardColumn column, Project project) {
        if (!column.getProject().getId().equals(project.getId())) {
            throw new NotFoundException("Board column not found in this project");
        }
    }
}

package com.board.service.impl;

import com.board.dto.activity.ActivityLogResponse;
import com.board.entity.ActivityLog;
import com.board.entity.Project;
import com.board.entity.User;
import com.board.exception.ForbiddenException;
import com.board.exception.NotFoundException;
import com.board.mapper.ActivityLogMapper;
import com.board.repository.ActivityLogRepository;
import com.board.repository.ProjectMemberRepository;
import com.board.repository.ProjectRepository;
import com.board.repository.UserRepository;
import com.board.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of ActivityLogService.
 */
@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;
    private final ActivityLogMapper activityLogMapper;

    @Override
    @Transactional
    public void recordActivity(Project project, User actor, String entityType,
            Long entityId, String action, String details) {
        ActivityLog activityLog = ActivityLog.builder()
                .project(project)
                .actor(actor)
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .details(details)
                .build();
        activityLogRepository.save(activityLog);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getActivityForProject(Long projectId, Long userId) {
        Project project = findProjectById(projectId);
        validateMembership(project, userId);

        List<ActivityLog> logs = activityLogRepository
                .findByProjectAndDeletedAtIsNullOrderByCreatedAtDesc(project);
        return activityLogMapper.toResponseList(logs);
    }

    private Project findProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .filter(p -> p.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Project not found"));
    }

    private void validateMembership(Project project, Long userId) {
        User user = findUserById(userId);
        if (!projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(project, user)) {
            throw new ForbiddenException("You are not a member of this project");
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }
}

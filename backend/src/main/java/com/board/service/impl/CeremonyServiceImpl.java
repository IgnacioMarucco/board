package com.board.service.impl;

import com.board.dto.ceremony.CeremonyCreateRequest;
import com.board.dto.ceremony.CeremonyResponse;
import com.board.dto.ceremony.CeremonyUpdateRequest;
import com.board.entity.Ceremony;
import com.board.entity.Project;
import com.board.entity.Sprint;
import com.board.entity.User;
import com.board.entity.enums.CeremonyType;
import com.board.entity.enums.Role;
import com.board.entity.enums.RetroTemplate;
import com.board.exception.BadRequestException;
import com.board.exception.ForbiddenException;
import com.board.exception.NotFoundException;
import com.board.mapper.CeremonyMapper;
import com.board.repository.CeremonyRepository;
import com.board.repository.ProjectMemberRepository;
import com.board.repository.SprintRepository;
import com.board.repository.UserRepository;
import com.board.service.CeremonyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of CeremonyService.
 */
@Service
@RequiredArgsConstructor
public class CeremonyServiceImpl implements CeremonyService {

    private final CeremonyRepository ceremonyRepository;
    private final SprintRepository sprintRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final CeremonyMapper ceremonyMapper;

    @Override
    @Transactional
    public CeremonyResponse createCeremony(Long sprintId,
            CeremonyCreateRequest request, Long userId) {
        Sprint sprint = findSprintById(sprintId);
        validateRole(sprint.getProject(), userId, Role.SCRUM_MASTER);

        Set<User> participants = new HashSet<>();
        if (request.getParticipantIds() != null && !request.getParticipantIds().isEmpty()) {
            participants = findUsersByIds(request.getParticipantIds());
            validateParticipantsMembership(sprint.getProject(), participants);
        }

        RetroTemplate retroTemplate = request.getRetroTemplate();
        if (request.getType() != null && request.getType() != CeremonyType.RETROSPECTIVE
                && retroTemplate != null) {
            throw new BadRequestException("Retro template can only be set for retrospectives");
        }
        if (request.getType() == CeremonyType.RETROSPECTIVE
                && retroTemplate == null) {
            retroTemplate = RetroTemplate.START_STOP_CONTINUE;
        }

        Ceremony ceremony = Ceremony.builder()
                .type(request.getType())
                .scheduledAt(request.getScheduledAt())
                .durationMinutes(request.getDurationMinutes())
                .notes(request.getNotes())
                .retroTemplate(retroTemplate)
                .sprint(sprint)
                .participants(participants)
                .build();

        ceremony = ceremonyRepository.save(ceremony);
        return ceremonyMapper.toResponse(ceremony);
    }

    @Override
    @Transactional(readOnly = true)
    public CeremonyResponse getCeremony(Long ceremonyId, Long userId) {
        Ceremony ceremony = findCeremonyById(ceremonyId);
        validateMembership(ceremony.getSprint().getProject(), userId);
        return ceremonyMapper.toResponse(ceremony);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CeremonyResponse> getCeremoniesForSprint(Long sprintId, Long userId) {
        Sprint sprint = findSprintById(sprintId);
        validateMembership(sprint.getProject(), userId);

        List<Ceremony> ceremonies = ceremonyRepository.findBySprintOrderByScheduledAtAsc(sprint);
        return ceremonyMapper.toResponseList(ceremonies);
    }

    @Override
    @Transactional
    public CeremonyResponse updateCeremony(Long ceremonyId,
            CeremonyUpdateRequest request, Long userId) {
        Ceremony ceremony = findCeremonyById(ceremonyId);
        validateRole(ceremony.getSprint().getProject(), userId, Role.SCRUM_MASTER);

        if (request.getScheduledAt() != null) {
            ceremony.setScheduledAt(request.getScheduledAt());
        }
        if (request.getDurationMinutes() != null) {
            ceremony.setDurationMinutes(request.getDurationMinutes());
        }
        if (request.getStatus() != null) {
            ceremony.setStatus(request.getStatus());
        }
        if (request.getNotes() != null) {
            ceremony.setNotes(request.getNotes());
        }
        if (request.getRetroTemplate() != null) {
            if (!ceremony.isRetrospective()) {
                throw new BadRequestException("Retro template can only be set for retrospectives");
            }
            ceremony.setRetroTemplate(request.getRetroTemplate());
        }
        if (request.getParticipantIds() != null) {
            Set<User> participants = findUsersByIds(request.getParticipantIds());
            validateParticipantsMembership(ceremony.getSprint().getProject(), participants);
            ceremony.setParticipants(participants);
        }

        ceremony = ceremonyRepository.save(ceremony);
        return ceremonyMapper.toResponse(ceremony);
    }

    @Override
    @Transactional
    public void deleteCeremony(Long ceremonyId, Long userId) {
        Ceremony ceremony = findCeremonyById(ceremonyId);
        validateRole(ceremony.getSprint().getProject(), userId, Role.SCRUM_MASTER);

        ceremony.softDelete();
        ceremonyRepository.save(ceremony);
    }

    private Ceremony findCeremonyById(Long ceremonyId) {
        return ceremonyRepository.findById(ceremonyId)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Ceremony not found"));
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

    private Set<User> findUsersByIds(Set<Long> userIds) {
        List<User> users = userRepository.findAllById(userIds);
        if (users.size() != userIds.size()) {
            throw new NotFoundException("One or more users not found");
        }
        return new HashSet<>(users);
    }

    private void validateMembership(Project project, Long userId) {
        User user = findUserById(userId);
        if (!projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(project, user)) {
            throw new ForbiddenException("You are not a member of this project");
        }
    }

    private void validateParticipantsMembership(Project project, Set<User> participants) {
        for (User participant : participants) {
            if (!projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(project, participant)) {
                throw new BadRequestException("All participants must be project members");
            }
        }
    }

    private Role getRoleForProject(Project project, Long userId) {
        User user = findUserById(userId);
        return projectMemberRepository.findByProjectAndUserAndDeletedAtIsNull(project, user)
                .map(com.board.entity.ProjectMember::getRole)
                .orElseThrow(() -> new ForbiddenException("You are not a member of this project"));
    }

    private void validateRole(Project project, Long userId, Role... allowedRoles) {
        Role role = getRoleForProject(project, userId);
        for (Role allowed : allowedRoles) {
            if (allowed == role) {
                return;
            }
        }
        throw new ForbiddenException("You are not allowed to perform this action");
    }
}

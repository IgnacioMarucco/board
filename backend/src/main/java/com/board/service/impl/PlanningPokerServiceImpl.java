package com.board.service.impl;

import com.board.dto.planningpoker.PlanningPokerFinalizeRequest;
import com.board.dto.planningpoker.PlanningPokerSessionCreateRequest;
import com.board.dto.planningpoker.PlanningPokerSessionResponse;
import com.board.dto.planningpoker.PlanningPokerVoteRequest;
import com.board.dto.planningpoker.PlanningPokerVoteResponse;
import com.board.entity.Ceremony;
import com.board.entity.PlanningPokerSession;
import com.board.entity.PlanningPokerVote;
import com.board.entity.Project;
import com.board.entity.Story;
import com.board.entity.User;
import com.board.entity.enums.CeremonyType;
import com.board.entity.enums.PlanningPokerStatus;
import com.board.entity.enums.Role;
import com.board.exception.BadRequestException;
import com.board.exception.ForbiddenException;
import com.board.exception.NotFoundException;
import com.board.repository.CeremonyRepository;
import com.board.repository.PlanningPokerSessionRepository;
import com.board.repository.PlanningPokerVoteRepository;
import com.board.repository.ProjectMemberRepository;
import com.board.repository.StoryRepository;
import com.board.repository.UserRepository;
import com.board.service.PlanningPokerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of PlanningPokerService.
 */
@Service
@RequiredArgsConstructor
public class PlanningPokerServiceImpl implements PlanningPokerService {

    private static final Set<Integer> FIBONACCI_POINTS = Set.of(1, 2, 3, 5, 8, 13, 21, 40, 100);

    private final PlanningPokerSessionRepository sessionRepository;
    private final PlanningPokerVoteRepository voteRepository;
    private final StoryRepository storyRepository;
    private final CeremonyRepository ceremonyRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public PlanningPokerSessionResponse createSession(Long storyId,
            PlanningPokerSessionCreateRequest request, Long userId) {
        Story story = findStoryById(storyId);
        Project project = resolveProject(story);
        validateMembership(project, userId);

        sessionRepository.findByStoryAndStatusIn(story,
                List.of(PlanningPokerStatus.OPEN, PlanningPokerStatus.REVEALED))
                .ifPresent(session -> {
                    throw new BadRequestException("A planning poker session is already active");
                });

        Ceremony ceremony = null;
        if (request.getCeremonyId() != null) {
            ceremony = findCeremonyById(request.getCeremonyId());
            if (ceremony.getType() != CeremonyType.REFINEMENT) {
                throw new BadRequestException("Planning poker can only be linked to refinement ceremonies");
            }
            Project ceremonyProject = ceremony.getSprint().getProject();
            if (!ceremonyProject.getId().equals(project.getId())) {
                throw new BadRequestException("Ceremony does not belong to the story project");
            }
        }

        User creator = findUserById(userId);
        PlanningPokerSession session = PlanningPokerSession.builder()
                .story(story)
                .ceremony(ceremony)
                .createdBy(creator)
                .status(PlanningPokerStatus.OPEN)
                .build();

        session = sessionRepository.save(session);
        return buildResponse(session);
    }

    @Override
    @Transactional(readOnly = true)
    public PlanningPokerSessionResponse getSession(Long sessionId, Long userId) {
        PlanningPokerSession session = findSessionById(sessionId);
        Project project = resolveProject(session.getStory());
        validateMembership(project, userId);
        return buildResponse(session);
    }

    @Override
    @Transactional
    public PlanningPokerSessionResponse vote(Long sessionId, PlanningPokerVoteRequest request, Long userId) {
        PlanningPokerSession session = findSessionById(sessionId);
        Project project = resolveProject(session.getStory());
        validateMembership(project, userId);

        if (session.getStatus() != PlanningPokerStatus.OPEN) {
            throw new BadRequestException("Voting is only allowed while the session is open");
        }
        validateVoteValue(request.getValue());

        User voter = findUserById(userId);
        PlanningPokerVote vote = voteRepository.findBySessionAndVoter(session, voter)
                .orElseGet(() -> PlanningPokerVote.builder()
                        .session(session)
                        .voter(voter)
                        .build());
        vote.setValue(request.getValue());
        voteRepository.save(vote);

        return buildResponse(session);
    }

    @Override
    @Transactional
    public PlanningPokerSessionResponse reveal(Long sessionId, Long userId) {
        PlanningPokerSession session = findSessionById(sessionId);
        Project project = resolveProject(session.getStory());
        requireRole(project, userId, Role.SCRUM_MASTER, Role.PRODUCT_OWNER);

        if (session.getStatus() != PlanningPokerStatus.OPEN) {
            throw new BadRequestException("Only open sessions can be revealed");
        }

        session.setStatus(PlanningPokerStatus.REVEALED);
        session.setRevealedAt(LocalDateTime.now());
        session = sessionRepository.save(session);
        return buildResponse(session);
    }

    @Override
    @Transactional
    public PlanningPokerSessionResponse finalizeSession(Long sessionId,
            PlanningPokerFinalizeRequest request, Long userId) {
        PlanningPokerSession session = findSessionById(sessionId);
        Project project = resolveProject(session.getStory());
        requireRole(project, userId, Role.SCRUM_MASTER, Role.PRODUCT_OWNER);

        if (session.getStatus() != PlanningPokerStatus.REVEALED) {
            throw new BadRequestException("Only revealed sessions can be finalized");
        }
        validateVoteValue(request.getFinalPoints());

        Story story = session.getStory();
        story.setStoryPoints(request.getFinalPoints());
        validateSprintCapacity(story);
        storyRepository.save(story);

        session.setFinalPoints(request.getFinalPoints());
        session.setStatus(PlanningPokerStatus.CLOSED);
        session.setClosedAt(LocalDateTime.now());
        session = sessionRepository.save(session);
        return buildResponse(session);
    }

    private PlanningPokerSession findSessionById(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .filter(session -> session.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Planning poker session not found"));
    }

    private Story findStoryById(Long storyId) {
        return storyRepository.findById(storyId)
                .filter(story -> story.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Story not found"));
    }

    private Ceremony findCeremonyById(Long ceremonyId) {
        return ceremonyRepository.findById(ceremonyId)
                .filter(ceremony -> ceremony.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Ceremony not found"));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private Project resolveProject(Story story) {
        if (story.getEpic() != null) {
            return story.getEpic().getProject();
        }
        if (story.getSprint() != null) {
            return story.getSprint().getProject();
        }
        throw new NotFoundException("Story is not linked to a project");
    }

    private void validateMembership(Project project, Long userId) {
        User user = findUserById(userId);
        if (!projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(project, user)) {
            throw new ForbiddenException("You are not a member of this project");
        }
    }

    private void requireRole(Project project, Long userId, Role... allowedRoles) {
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

    private void validateVoteValue(Integer value) {
        if (value == null || !FIBONACCI_POINTS.contains(value)) {
            throw new BadRequestException("Vote must follow the Fibonacci scale");
        }
    }

    private void validateSprintCapacity(Story story) {
        if (story.getSprint() == null) {
            return;
        }
        Integer capacity = story.getSprint().getCapacityPoints();
        if (capacity == null || capacity <= 0) {
            return;
        }
        int totalPoints = storyRepository.findBySprint(story.getSprint()).stream()
                .filter(Story::isActive)
                .filter(existing -> !existing.getId().equals(story.getId()))
                .map(Story::getStoryPoints)
                .filter(points -> points != null)
                .mapToInt(Integer::intValue)
                .sum();
        int storyPoints = story.getStoryPoints() != null ? story.getStoryPoints() : 0;
        if (totalPoints + storyPoints > capacity) {
            throw new BadRequestException("Sprint capacity exceeded");
        }
    }

    private PlanningPokerSessionResponse buildResponse(PlanningPokerSession session) {
        List<PlanningPokerVoteResponse> votes = null;
        if (session.getStatus() == PlanningPokerStatus.REVEALED
                || session.getStatus() == PlanningPokerStatus.CLOSED) {
            votes = voteRepository.findBySession(session).stream()
                    .map(vote -> PlanningPokerVoteResponse.builder()
                            .userId(vote.getVoter().getId())
                            .value(vote.getValue())
                            .createdAt(vote.getCreatedAt())
                            .build())
                    .collect(Collectors.toList());
        }

        int voteCount = voteRepository.findBySession(session).size();

        return PlanningPokerSessionResponse.builder()
                .id(session.getId())
                .status(session.getStatus())
                .storyId(session.getStory().getId())
                .storyKey(session.getStory().getKey())
                .ceremonyId(session.getCeremony() != null ? session.getCeremony().getId() : null)
                .createdById(session.getCreatedBy().getId())
                .finalPoints(session.getFinalPoints())
                .voteCount(voteCount)
                .votes(votes)
                .createdAt(session.getCreatedAt())
                .revealedAt(session.getRevealedAt())
                .closedAt(session.getClosedAt())
                .build();
    }
}

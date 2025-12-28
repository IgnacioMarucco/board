package com.board.service.impl;

import com.board.dto.retroitem.RetroItemCreateRequest;
import com.board.dto.retroitem.RetroItemResponse;
import com.board.dto.retroitem.RetroItemUpdateRequest;
import com.board.entity.Ceremony;
import com.board.entity.Project;
import com.board.entity.RetroItem;
import com.board.entity.User;
import com.board.exception.BadRequestException;
import com.board.exception.ForbiddenException;
import com.board.exception.NotFoundException;
import com.board.mapper.RetroItemMapper;
import com.board.repository.CeremonyRepository;
import com.board.repository.ProjectMemberRepository;
import com.board.repository.RetroItemRepository;
import com.board.repository.UserRepository;
import com.board.service.RetroItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of RetroItemService.
 */
@Service
@RequiredArgsConstructor
public class RetroItemServiceImpl implements RetroItemService {

    private final RetroItemRepository retroItemRepository;
    private final CeremonyRepository ceremonyRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final RetroItemMapper retroItemMapper;

    @Override
    @Transactional
    public RetroItemResponse createRetroItem(Long ceremonyId,
            RetroItemCreateRequest request, Long userId) {
        Ceremony ceremony = findCeremonyById(ceremonyId);
        validateMembership(ceremony.getSprint().getProject(), userId);

        if (!ceremony.isRetrospective()) {
            throw new BadRequestException("Retro items can only be added to retrospective ceremonies");
        }

        User author = request.getIsAnonymous() ? null : findUserById(userId);

        RetroItem retroItem = RetroItem.builder()
                .content(request.getContent())
                .category(request.getCategory())
                .isAnonymous(request.getIsAnonymous())
                .ceremony(ceremony)
                .author(author)
                .build();

        retroItem = retroItemRepository.save(retroItem);
        return retroItemMapper.toResponse(retroItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RetroItemResponse> getRetroItemsForCeremony(Long ceremonyId, Long userId) {
        Ceremony ceremony = findCeremonyById(ceremonyId);
        validateMembership(ceremony.getSprint().getProject(), userId);

        List<RetroItem> retroItems = retroItemRepository
                .findByCeremonyOrderByVotesDescCreatedAtAsc(ceremony);
        return retroItemMapper.toResponseList(retroItems);
    }

    @Override
    @Transactional
    public RetroItemResponse updateRetroItem(Long retroItemId,
            RetroItemUpdateRequest request, Long userId) {
        RetroItem retroItem = findRetroItemById(retroItemId);
        validateOwnership(retroItem, userId);

        retroItem.setContent(request.getContent());
        if (request.getCategory() != null) {
            retroItem.setCategory(request.getCategory());
        }

        retroItem = retroItemRepository.save(retroItem);
        return retroItemMapper.toResponse(retroItem);
    }

    @Override
    @Transactional
    public RetroItemResponse voteRetroItem(Long retroItemId, Long userId) {
        RetroItem retroItem = findRetroItemById(retroItemId);
        validateMembership(retroItem.getCeremony().getSprint().getProject(), userId);

        retroItem.addVote();
        retroItem = retroItemRepository.save(retroItem);

        return retroItemMapper.toResponse(retroItem);
    }

    @Override
    @Transactional
    public void deleteRetroItem(Long retroItemId, Long userId) {
        RetroItem retroItem = findRetroItemById(retroItemId);
        validateOwnership(retroItem, userId);

        retroItem.softDelete();
        retroItemRepository.save(retroItem);
    }

    private RetroItem findRetroItemById(Long retroItemId) {
        return retroItemRepository.findById(retroItemId)
                .filter(r -> r.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Retro item not found"));
    }

    private Ceremony findCeremonyById(Long ceremonyId) {
        return ceremonyRepository.findById(ceremonyId)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Ceremony not found"));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private void validateMembership(Project project, Long userId) {
        User user = findUserById(userId);
        if (!project.getOwner().getId().equals(userId)
                && !projectMemberRepository.existsByProjectAndUser(project, user)) {
            throw new ForbiddenException("You are not a member of this project");
        }
    }

    private void validateOwnership(RetroItem retroItem, Long userId) {
        if (retroItem.getIsAnonymous() || retroItem.getAuthor() == null) {
            throw new ForbiddenException("Cannot modify anonymous retro items");
        }
        if (!retroItem.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("You can only modify your own retro items");
        }
    }
}

package com.board.service;

import com.board.dto.retroitem.RetroItemCreateRequest;
import com.board.dto.retroitem.RetroItemResponse;
import com.board.dto.retroitem.RetroItemUpdateRequest;

import java.util.List;

/**
 * Service interface for retro item operations.
 */
public interface RetroItemService {

    /**
     * Creates a retro item for a ceremony.
     *
     * @param ceremonyId the ceremony ID
     * @param request    the create request
     * @param userId     the requesting user's ID
     * @return the created retro item
     */
    RetroItemResponse createRetroItem(Long ceremonyId, RetroItemCreateRequest request, Long userId);

    /**
     * Gets all retro items for a ceremony.
     *
     * @param ceremonyId the ceremony ID
     * @param userId     the requesting user's ID
     * @return list of retro items
     */
    List<RetroItemResponse> getRetroItemsForCeremony(Long ceremonyId, Long userId);

    /**
     * Updates a retro item.
     *
     * @param retroItemId the retro item ID
     * @param request     the update request
     * @param userId      the requesting user's ID
     * @return the updated retro item
     */
    RetroItemResponse updateRetroItem(Long retroItemId, RetroItemUpdateRequest request, Long userId);

    /**
     * Adds a vote to a retro item.
     *
     * @param retroItemId the retro item ID
     * @param userId      the requesting user's ID
     * @return the updated retro item
     */
    RetroItemResponse voteRetroItem(Long retroItemId, Long userId);

    /**
     * Deletes a retro item.
     *
     * @param retroItemId the retro item ID
     * @param userId      the requesting user's ID
     */
    void deleteRetroItem(Long retroItemId, Long userId);
}

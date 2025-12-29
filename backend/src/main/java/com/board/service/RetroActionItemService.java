package com.board.service;

import com.board.dto.retroactionitem.RetroActionItemConvertRequest;
import com.board.dto.retroactionitem.RetroActionItemCreateRequest;
import com.board.dto.retroactionitem.RetroActionItemResponse;
import com.board.dto.retroactionitem.RetroActionItemUpdateRequest;

import java.util.List;

/**
 * Service interface for retrospective action items.
 */
public interface RetroActionItemService {

    RetroActionItemResponse createActionItem(Long ceremonyId,
            RetroActionItemCreateRequest request, Long userId);

    List<RetroActionItemResponse> getActionItemsForCeremony(Long ceremonyId, Long userId);

    RetroActionItemResponse updateActionItem(Long actionItemId,
            RetroActionItemUpdateRequest request, Long userId);

    RetroActionItemResponse convertToTask(Long actionItemId,
            RetroActionItemConvertRequest request, Long userId);

    void deleteActionItem(Long actionItemId, Long userId);
}

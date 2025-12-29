package com.board.controller;

import com.board.dto.retroactionitem.RetroActionItemConvertRequest;
import com.board.dto.retroactionitem.RetroActionItemCreateRequest;
import com.board.dto.retroactionitem.RetroActionItemResponse;
import com.board.dto.retroactionitem.RetroActionItemUpdateRequest;
import com.board.security.UserPrincipal;
import com.board.service.RetroActionItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for retro action items.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Retro Action Items", description = "Retrospective action item endpoints")
public class RetroActionItemController {

    private final RetroActionItemService retroActionItemService;

    @PostMapping("/ceremonies/{ceremonyId}/action-items")
    @Operation(summary = "Create an action item for a retrospective")
    public ResponseEntity<RetroActionItemResponse> createActionItem(
            @PathVariable Long ceremonyId,
            @Valid @RequestBody RetroActionItemCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        RetroActionItemResponse response = retroActionItemService.createActionItem(
                ceremonyId, request, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/ceremonies/{ceremonyId}/action-items")
    @Operation(summary = "Get action items for a retrospective")
    public ResponseEntity<List<RetroActionItemResponse>> getActionItemsForCeremony(
            @PathVariable Long ceremonyId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(retroActionItemService.getActionItemsForCeremony(
                ceremonyId, principal.getUserId()));
    }

    @PutMapping("/action-items/{actionItemId}")
    @Operation(summary = "Update an action item")
    public ResponseEntity<RetroActionItemResponse> updateActionItem(
            @PathVariable Long actionItemId,
            @Valid @RequestBody RetroActionItemUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(retroActionItemService.updateActionItem(
                actionItemId, request, principal.getUserId()));
    }

    @PostMapping("/action-items/{actionItemId}/convert-to-task")
    @Operation(summary = "Convert an action item to a task")
    public ResponseEntity<RetroActionItemResponse> convertToTask(
            @PathVariable Long actionItemId,
            @Valid @RequestBody RetroActionItemConvertRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(retroActionItemService.convertToTask(
                actionItemId, request, principal.getUserId()));
    }

    @DeleteMapping("/action-items/{actionItemId}")
    @Operation(summary = "Delete an action item")
    public ResponseEntity<Void> deleteActionItem(
            @PathVariable Long actionItemId,
            @AuthenticationPrincipal UserPrincipal principal) {
        retroActionItemService.deleteActionItem(actionItemId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}

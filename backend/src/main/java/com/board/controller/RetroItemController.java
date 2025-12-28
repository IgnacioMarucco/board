package com.board.controller;

import com.board.dto.retroitem.RetroItemCreateRequest;
import com.board.dto.retroitem.RetroItemResponse;
import com.board.dto.retroitem.RetroItemUpdateRequest;
import com.board.security.UserPrincipal;
import com.board.service.RetroItemService;
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
 * REST controller for retro item operations.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Retro Items", description = "Retrospective item management endpoints")
public class RetroItemController {

    private final RetroItemService retroItemService;

    @PostMapping("/ceremonies/{ceremonyId}/retro-items")
    @Operation(summary = "Create a retro item for a ceremony")
    public ResponseEntity<RetroItemResponse> createRetroItem(
            @PathVariable Long ceremonyId,
            @Valid @RequestBody RetroItemCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        RetroItemResponse response = retroItemService.createRetroItem(ceremonyId, request,
                principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/ceremonies/{ceremonyId}/retro-items")
    @Operation(summary = "Get all retro items for a ceremony")
    public ResponseEntity<List<RetroItemResponse>> getRetroItemsForCeremony(
            @PathVariable Long ceremonyId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(retroItemService.getRetroItemsForCeremony(ceremonyId,
                principal.getUserId()));
    }

    @PutMapping("/retro-items/{retroItemId}")
    @Operation(summary = "Update a retro item")
    public ResponseEntity<RetroItemResponse> updateRetroItem(
            @PathVariable Long retroItemId,
            @Valid @RequestBody RetroItemUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(retroItemService.updateRetroItem(retroItemId, request,
                principal.getUserId()));
    }

    @PostMapping("/retro-items/{retroItemId}/vote")
    @Operation(summary = "Vote on a retro item")
    public ResponseEntity<RetroItemResponse> voteRetroItem(
            @PathVariable Long retroItemId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(retroItemService.voteRetroItem(retroItemId,
                principal.getUserId()));
    }

    @DeleteMapping("/retro-items/{retroItemId}")
    @Operation(summary = "Delete a retro item")
    public ResponseEntity<Void> deleteRetroItem(
            @PathVariable Long retroItemId,
            @AuthenticationPrincipal UserPrincipal principal) {
        retroItemService.deleteRetroItem(retroItemId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}

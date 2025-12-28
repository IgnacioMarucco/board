package com.board.controller;

import com.board.dto.ceremony.CeremonyCreateRequest;
import com.board.dto.ceremony.CeremonyResponse;
import com.board.dto.ceremony.CeremonyUpdateRequest;
import com.board.security.UserPrincipal;
import com.board.service.CeremonyService;
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
 * REST controller for ceremony operations.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Ceremonies", description = "Ceremony management endpoints")
public class CeremonyController {

    private final CeremonyService ceremonyService;

    @PostMapping("/sprints/{sprintId}/ceremonies")
    @Operation(summary = "Create a ceremony for a sprint")
    public ResponseEntity<CeremonyResponse> createCeremony(
            @PathVariable Long sprintId,
            @Valid @RequestBody CeremonyCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        CeremonyResponse response = ceremonyService.createCeremony(sprintId, request,
                principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/sprints/{sprintId}/ceremonies")
    @Operation(summary = "Get all ceremonies for a sprint")
    public ResponseEntity<List<CeremonyResponse>> getCeremoniesForSprint(
            @PathVariable Long sprintId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ceremonyService.getCeremoniesForSprint(sprintId,
                principal.getUserId()));
    }

    @GetMapping("/ceremonies/{ceremonyId}")
    @Operation(summary = "Get a ceremony by ID")
    public ResponseEntity<CeremonyResponse> getCeremony(
            @PathVariable Long ceremonyId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ceremonyService.getCeremony(ceremonyId, principal.getUserId()));
    }

    @PutMapping("/ceremonies/{ceremonyId}")
    @Operation(summary = "Update a ceremony")
    public ResponseEntity<CeremonyResponse> updateCeremony(
            @PathVariable Long ceremonyId,
            @Valid @RequestBody CeremonyUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(ceremonyService.updateCeremony(ceremonyId, request,
                principal.getUserId()));
    }

    @DeleteMapping("/ceremonies/{ceremonyId}")
    @Operation(summary = "Delete a ceremony")
    public ResponseEntity<Void> deleteCeremony(
            @PathVariable Long ceremonyId,
            @AuthenticationPrincipal UserPrincipal principal) {
        ceremonyService.deleteCeremony(ceremonyId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}

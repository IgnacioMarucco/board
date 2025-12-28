package com.board.controller;

import com.board.dto.story.StoryAssignRequest;
import com.board.dto.story.StoryCreateRequest;
import com.board.dto.story.StoryResponse;
import com.board.dto.story.StoryUpdateRequest;
import com.board.security.UserPrincipal;
import com.board.service.StoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for story operations.
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/stories")
@RequiredArgsConstructor
@Tag(name = "Stories", description = "Story management endpoints")
public class StoryController {

    private final StoryService storyService;

    /**
     * Creates a new story.
     *
     * @param projectId the project ID
     * @param request   the create request
     * @param principal the authenticated user
     * @return the created story
     */
    @PostMapping
    @Operation(summary = "Create a new story")
    public ResponseEntity<StoryResponse> createStory(
            @PathVariable Long projectId,
            @Valid @RequestBody StoryCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        StoryResponse response = storyService.createStory(projectId, request, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets all stories for a project.
     *
     * @param projectId the project ID
     * @param principal the authenticated user
     * @return list of stories
     */
    @GetMapping
    @Operation(summary = "Get all stories for a project")
    public ResponseEntity<List<StoryResponse>> getStories(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(storyService.getStoriesForProject(projectId, principal.getUserId()));
    }

    /**
     * Gets a story by ID.
     *
     * @param projectId the project ID
     * @param storyId   the story ID
     * @param principal the authenticated user
     * @return the story
     */
    @GetMapping("/{storyId}")
    @Operation(summary = "Get a story by ID")
    public ResponseEntity<StoryResponse> getStory(
            @PathVariable Long projectId,
            @PathVariable Long storyId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(storyService.getStory(projectId, storyId, principal.getUserId()));
    }

    /**
     * Updates a story.
     *
     * @param projectId the project ID
     * @param storyId   the story ID
     * @param request   the update request
     * @param principal the authenticated user
     * @return the updated story
     */
    @PutMapping("/{storyId}")
    @Operation(summary = "Update a story")
    public ResponseEntity<StoryResponse> updateStory(
            @PathVariable Long projectId,
            @PathVariable Long storyId,
            @Valid @RequestBody StoryUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                storyService.updateStory(projectId, storyId, request, principal.getUserId()));
    }

    /**
     * Deletes a story (soft delete).
     *
     * @param projectId the project ID
     * @param storyId   the story ID
     * @param principal the authenticated user
     * @return no content
     */
    @DeleteMapping("/{storyId}")
    @Operation(summary = "Delete a story")
    public ResponseEntity<Void> deleteStory(
            @PathVariable Long projectId,
            @PathVariable Long storyId,
            @AuthenticationPrincipal UserPrincipal principal) {
        storyService.deleteStory(projectId, storyId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Assigns a story to a user.
     *
     * @param projectId the project ID
     * @param storyId   the story ID
     * @param request   the assign request
     * @param principal the authenticated user
     * @return the updated story
     */
    @PatchMapping("/{storyId}/assign")
    @Operation(summary = "Assign a story to a user")
    public ResponseEntity<StoryResponse> assignStory(
            @PathVariable Long projectId,
            @PathVariable Long storyId,
            @Valid @RequestBody StoryAssignRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                storyService.assignStory(projectId, storyId, request, principal.getUserId()));
    }
}

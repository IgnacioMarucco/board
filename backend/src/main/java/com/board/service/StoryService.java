package com.board.service;

import com.board.dto.story.StoryAssignRequest;
import com.board.dto.story.StoryCreateRequest;
import com.board.dto.story.StoryResponse;
import com.board.dto.story.StoryUpdateRequest;

import java.util.List;

/**
 * Service interface for story operations.
 */
public interface StoryService {

    /**
     * Creates a new story.
     *
     * @param projectId the project ID (from epic)
     * @param request   the create request
     * @param userId    the requesting user's ID
     * @return the created story
     */
    StoryResponse createStory(Long projectId, StoryCreateRequest request, Long userId);

    /**
     * Gets a story by ID.
     *
     * @param projectId the project ID
     * @param storyId   the story ID
     * @param userId    the requesting user's ID
     * @return the story
     */
    StoryResponse getStory(Long projectId, Long storyId, Long userId);

    /**
     * Gets all stories for a project.
     *
     * @param projectId the project ID
     * @param userId    the requesting user's ID
     * @return list of stories
     */
    List<StoryResponse> getStoriesForProject(Long projectId, Long userId);

    /**
     * Updates a story.
     *
     * @param projectId the project ID
     * @param storyId   the story ID
     * @param request   the update request
     * @param userId    the requesting user's ID
     * @return the updated story
     */
    StoryResponse updateStory(Long projectId, Long storyId, StoryUpdateRequest request, Long userId);

    /**
     * Deletes a story (soft delete).
     *
     * @param projectId the project ID
     * @param storyId   the story ID
     * @param userId    the requesting user's ID
     */
    void deleteStory(Long projectId, Long storyId, Long userId);

    /**
     * Assigns a story to a user.
     *
     * @param projectId the project ID
     * @param storyId   the story ID
     * @param request   the assign request
     * @param userId    the requesting user's ID
     * @return the updated story
     */
    StoryResponse assignStory(Long projectId, Long storyId, StoryAssignRequest request, Long userId);
}

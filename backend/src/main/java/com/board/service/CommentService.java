package com.board.service;

import com.board.dto.comment.CommentCreateRequest;
import com.board.dto.comment.CommentResponse;
import com.board.dto.comment.CommentUpdateRequest;

import java.util.List;

/**
 * Service interface for comment operations.
 */
public interface CommentService {

    /**
     * Creates a comment on an epic.
     *
     * @param epicId  the epic ID
     * @param request the create request
     * @param userId  the requesting user's ID
     * @return the created comment
     */
    CommentResponse createCommentOnEpic(Long epicId, CommentCreateRequest request, Long userId);

    /**
     * Creates a comment on a story.
     *
     * @param storyId the story ID
     * @param request the create request
     * @param userId  the requesting user's ID
     * @return the created comment
     */
    CommentResponse createCommentOnStory(Long storyId, CommentCreateRequest request, Long userId);

    /**
     * Creates a comment on a task.
     *
     * @param taskId  the task ID
     * @param request the create request
     * @param userId  the requesting user's ID
     * @return the created comment
     */
    CommentResponse createCommentOnTask(Long taskId, CommentCreateRequest request, Long userId);

    /**
     * Gets a comment by ID.
     *
     * @param commentId the comment ID
     * @param userId    the requesting user's ID
     * @return the comment
     */
    CommentResponse getComment(Long commentId, Long userId);

    /**
     * Gets all comments for an epic.
     *
     * @param epicId the epic ID
     * @param userId the requesting user's ID
     * @return list of comments
     */
    List<CommentResponse> getCommentsForEpic(Long epicId, Long userId);

    /**
     * Gets all comments for a story.
     *
     * @param storyId the story ID
     * @param userId  the requesting user's ID
     * @return list of comments
     */
    List<CommentResponse> getCommentsForStory(Long storyId, Long userId);

    /**
     * Gets all comments for a task.
     *
     * @param taskId the task ID
     * @param userId the requesting user's ID
     * @return list of comments
     */
    List<CommentResponse> getCommentsForTask(Long taskId, Long userId);

    /**
     * Updates a comment.
     *
     * @param commentId the comment ID
     * @param request   the update request
     * @param userId    the requesting user's ID
     * @return the updated comment
     */
    CommentResponse updateComment(Long commentId, CommentUpdateRequest request, Long userId);

    /**
     * Deletes a comment (soft delete).
     *
     * @param commentId the comment ID
     * @param userId    the requesting user's ID
     */
    void deleteComment(Long commentId, Long userId);
}

package com.board.service;

import com.board.dto.boardcolumn.BoardColumnCreateRequest;
import com.board.dto.boardcolumn.BoardColumnResponse;
import com.board.dto.boardcolumn.BoardColumnUpdateRequest;

import java.util.List;

/**
 * Service interface for board column operations.
 */
public interface BoardColumnService {

    /**
     * Creates a new board column.
     *
     * @param projectId the project ID
     * @param request   the create request
     * @param userId    the requesting user's ID
     * @return the created board column
     */
    BoardColumnResponse createColumn(Long projectId, BoardColumnCreateRequest request, Long userId);

    /**
     * Gets a board column by ID.
     *
     * @param projectId the project ID
     * @param columnId  the column ID
     * @param userId    the requesting user's ID
     * @return the board column
     */
    BoardColumnResponse getColumn(Long projectId, Long columnId, Long userId);

    /**
     * Gets all board columns for a project.
     *
     * @param projectId the project ID
     * @param userId    the requesting user's ID
     * @return list of board columns ordered by position
     */
    List<BoardColumnResponse> getColumnsForProject(Long projectId, Long userId);

    /**
     * Updates a board column.
     *
     * @param projectId the project ID
     * @param columnId  the column ID
     * @param request   the update request
     * @param userId    the requesting user's ID
     * @return the updated board column
     */
    BoardColumnResponse updateColumn(Long projectId, Long columnId,
            BoardColumnUpdateRequest request, Long userId);

    /**
     * Deletes a board column (soft delete).
     *
     * @param projectId the project ID
     * @param columnId  the column ID
     * @param userId    the requesting user's ID
     */
    void deleteColumn(Long projectId, Long columnId, Long userId);
}

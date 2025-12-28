package com.board.controller;

import com.board.dto.boardcolumn.BoardColumnCreateRequest;
import com.board.dto.boardcolumn.BoardColumnResponse;
import com.board.dto.boardcolumn.BoardColumnUpdateRequest;
import com.board.security.UserPrincipal;
import com.board.service.BoardColumnService;
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
 * REST controller for board column operations.
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/columns")
@RequiredArgsConstructor
@Tag(name = "Board Columns", description = "Board column management endpoints")
public class BoardColumnController {

    private final BoardColumnService boardColumnService;

    /**
     * Creates a new board column.
     *
     * @param projectId the project ID
     * @param request   the create request
     * @param principal the authenticated user
     * @return the created board column
     */
    @PostMapping
    @Operation(summary = "Create a new board column")
    public ResponseEntity<BoardColumnResponse> createColumn(
            @PathVariable Long projectId,
            @Valid @RequestBody BoardColumnCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        BoardColumnResponse response = boardColumnService.createColumn(projectId, request,
                principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Gets all board columns for a project.
     *
     * @param projectId the project ID
     * @param principal the authenticated user
     * @return list of board columns
     */
    @GetMapping
    @Operation(summary = "Get all board columns for a project")
    public ResponseEntity<List<BoardColumnResponse>> getColumns(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(boardColumnService.getColumnsForProject(projectId,
                principal.getUserId()));
    }

    /**
     * Gets a board column by ID.
     *
     * @param projectId the project ID
     * @param columnId  the column ID
     * @param principal the authenticated user
     * @return the board column
     */
    @GetMapping("/{columnId}")
    @Operation(summary = "Get a board column by ID")
    public ResponseEntity<BoardColumnResponse> getColumn(
            @PathVariable Long projectId,
            @PathVariable Long columnId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(boardColumnService.getColumn(projectId, columnId,
                principal.getUserId()));
    }

    /**
     * Updates a board column.
     *
     * @param projectId the project ID
     * @param columnId  the column ID
     * @param request   the update request
     * @param principal the authenticated user
     * @return the updated board column
     */
    @PutMapping("/{columnId}")
    @Operation(summary = "Update a board column")
    public ResponseEntity<BoardColumnResponse> updateColumn(
            @PathVariable Long projectId,
            @PathVariable Long columnId,
            @Valid @RequestBody BoardColumnUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(
                boardColumnService.updateColumn(projectId, columnId, request,
                        principal.getUserId()));
    }

    /**
     * Deletes a board column (soft delete).
     *
     * @param projectId the project ID
     * @param columnId  the column ID
     * @param principal the authenticated user
     * @return no content
     */
    @DeleteMapping("/{columnId}")
    @Operation(summary = "Delete a board column")
    public ResponseEntity<Void> deleteColumn(
            @PathVariable Long projectId,
            @PathVariable Long columnId,
            @AuthenticationPrincipal UserPrincipal principal) {
        boardColumnService.deleteColumn(projectId, columnId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}

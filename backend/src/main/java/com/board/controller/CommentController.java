package com.board.controller;

import com.board.dto.comment.CommentCreateRequest;
import com.board.dto.comment.CommentResponse;
import com.board.dto.comment.CommentUpdateRequest;
import com.board.security.UserPrincipal;
import com.board.service.CommentService;
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
 * REST controller for comment operations.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "Comment management endpoints")
public class CommentController {

    private final CommentService commentService;

    // Epic comments
    @PostMapping("/epics/{epicId}/comments")
    @Operation(summary = "Create a comment on an epic")
    public ResponseEntity<CommentResponse> createCommentOnEpic(
            @PathVariable Long epicId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        CommentResponse response = commentService.createCommentOnEpic(epicId, request,
                principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/epics/{epicId}/comments")
    @Operation(summary = "Get all comments for an epic")
    public ResponseEntity<List<CommentResponse>> getCommentsForEpic(
            @PathVariable Long epicId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(commentService.getCommentsForEpic(epicId, principal.getUserId()));
    }

    // Story comments
    @PostMapping("/stories/{storyId}/comments")
    @Operation(summary = "Create a comment on a story")
    public ResponseEntity<CommentResponse> createCommentOnStory(
            @PathVariable Long storyId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        CommentResponse response = commentService.createCommentOnStory(storyId, request,
                principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/stories/{storyId}/comments")
    @Operation(summary = "Get all comments for a story")
    public ResponseEntity<List<CommentResponse>> getCommentsForStory(
            @PathVariable Long storyId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(commentService.getCommentsForStory(storyId,
                principal.getUserId()));
    }

    // Task comments
    @PostMapping("/tasks/{taskId}/comments")
    @Operation(summary = "Create a comment on a task")
    public ResponseEntity<CommentResponse> createCommentOnTask(
            @PathVariable Long taskId,
            @Valid @RequestBody CommentCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        CommentResponse response = commentService.createCommentOnTask(taskId, request,
                principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/tasks/{taskId}/comments")
    @Operation(summary = "Get all comments for a task")
    public ResponseEntity<List<CommentResponse>> getCommentsForTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(commentService.getCommentsForTask(taskId,
                principal.getUserId()));
    }

    // Generic comment operations
    @GetMapping("/comments/{commentId}")
    @Operation(summary = "Get a comment by ID")
    public ResponseEntity<CommentResponse> getComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(commentService.getComment(commentId, principal.getUserId()));
    }

    @PutMapping("/comments/{commentId}")
    @Operation(summary = "Update a comment")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(commentService.updateComment(commentId, request,
                principal.getUserId()));
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "Delete a comment")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        commentService.deleteComment(commentId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}

package com.board.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for comment data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private String content;

    private Long authorId;
    private String authorEmail;

    private Long epicId;
    private Long storyId;
    private Long taskId;

    private Long parentCommentId;
    private Integer replyCount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

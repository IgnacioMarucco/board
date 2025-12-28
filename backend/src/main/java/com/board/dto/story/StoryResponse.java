package com.board.dto.story;

import com.board.entity.enums.Priority;
import com.board.entity.enums.StoryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for story data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryResponse {

    private Long id;
    private String key;
    private String title;
    private String description;
    private String acceptanceCriteria;
    private StoryStatus status;
    private Priority priority;
    private Integer storyPoints;
    private Integer position;

    private Long epicId;
    private String epicKey;

    private Long sprintId;
    private String sprintName;

    private Long assigneeId;
    private String assigneeEmail;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

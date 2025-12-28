package com.board.dto.story;

import com.board.entity.enums.Priority;
import com.board.entity.enums.StoryStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing story.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryUpdateRequest {

    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;

    @Size(max = 2000, message = "Acceptance criteria cannot exceed 2000 characters")
    private String acceptanceCriteria;

    private StoryStatus status;

    private Priority priority;

    @Min(value = 0, message = "Story points must be non-negative")
    private Integer storyPoints;

    private Long epicId;

    private Long sprintId;

    private Long assigneeId;
}

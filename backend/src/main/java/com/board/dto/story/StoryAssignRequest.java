package com.board.dto.story;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for assigning a story to a user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoryAssignRequest {

    @NotNull(message = "Assignee ID is required")
    private Long assigneeId;
}

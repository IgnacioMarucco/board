package com.board.dto.retroactionitem;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for converting a retro action item into a task.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetroActionItemConvertRequest {

    @NotNull(message = "Story ID is required")
    private Long storyId;

    @NotBlank(message = "Task key is required")
    @Size(min = 2, max = 20, message = "Task key must be 2-20 characters")
    private String key;

    @NotBlank(message = "Task title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;

    private Long assigneeId;

    private BigDecimal estimatedHours;
}

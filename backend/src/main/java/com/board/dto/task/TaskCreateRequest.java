package com.board.dto.task;

import com.board.entity.enums.TaskStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new task.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCreateRequest {

    @NotBlank(message = "Task key is required")
    @Size(min = 2, max = 20, message = "Task key must be 2-20 characters")
    private String key;

    @NotBlank(message = "Task title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    private TaskStatus status;

    @DecimalMin(value = "0.0", message = "Estimated hours must be non-negative")
    private BigDecimal estimatedHours;

    private Long assigneeId;
}

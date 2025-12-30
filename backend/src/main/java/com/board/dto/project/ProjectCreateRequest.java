package com.board.dto.project;

import com.board.entity.enums.BoardTemplate;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new project.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCreateRequest {

    @NotBlank(message = "Project key is required")
    @Size(min = 2, max = 10, message = "Project key must be 2-10 characters")
    private String key;

    @NotBlank(message = "Project name is required")
    @Size(max = 100, message = "Project name cannot exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private BoardTemplate boardTemplate;

    @Min(value = 1, message = "Sprint duration must be at least 1 week")
    @Max(value = 4, message = "Sprint duration cannot exceed 4 weeks")
    private Integer sprintDurationWeeks;

    @Size(max = 50, message = "Time zone cannot exceed 50 characters")
    private String timeZone;
}

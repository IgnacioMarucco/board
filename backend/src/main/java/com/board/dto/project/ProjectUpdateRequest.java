package com.board.dto.project;

import com.board.entity.enums.BoardTemplate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing project.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectUpdateRequest {

    @Size(max = 100, message = "Project name cannot exceed 100 characters")
    @Schema(description = "Project name", example = "Board App")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    @Schema(description = "Project description", example = "Agile board for the team")
    private String description;

    @Schema(description = "Template applied to the board", example = "SOFTWARE")
    private BoardTemplate boardTemplate;

    @Min(value = 1, message = "Sprint duration must be at least 1 week")
    @Max(value = 4, message = "Sprint duration cannot exceed 4 weeks")
    @Schema(description = "Sprint duration in weeks", example = "2")
    private Integer sprintDurationWeeks;

    @Size(max = 50, message = "Time zone cannot exceed 50 characters")
    @Schema(description = "Project time zone (defaults to UTC)", example = "UTC")
    private String timeZone;
}

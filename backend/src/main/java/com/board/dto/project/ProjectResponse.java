package com.board.dto.project;

import com.board.entity.enums.BoardTemplate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for project data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {

    @Schema(description = "Project identifier", example = "10")
    private Long id;
    @Schema(description = "Short project key", example = "BRD")
    private String key;
    @Schema(description = "Project name", example = "Board App")
    private String name;
    @Schema(description = "Project description", example = "Agile board for the team")
    private String description;
    @Schema(description = "Template applied to the board", example = "SOFTWARE")
    private BoardTemplate boardTemplate;
    @Schema(description = "Sprint duration in weeks", example = "2")
    private Integer sprintDurationWeeks;
    @Schema(description = "Project time zone", example = "UTC")
    private String timeZone;
    private OwnerInfo owner;
    @Schema(description = "Creation timestamp", example = "2025-01-01T10:15:30")
    private LocalDateTime createdAt;
    @Schema(description = "Last update timestamp", example = "2025-01-15T09:45:00")
    private LocalDateTime updatedAt;

    /**
     * Owner information included in project response.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerInfo {
        @Schema(description = "Owner identifier", example = "3")
        private Long id;
        @Schema(description = "Owner email", example = "owner@board.app")
        private String email;
        @Schema(description = "Owner first name", example = "Alex")
        private String firstName;
        @Schema(description = "Owner last name", example = "Mora")
        private String lastName;
    }
}

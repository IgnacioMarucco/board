package com.board.dto.project;

import com.board.entity.enums.BoardTemplate;
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

    private Long id;
    private String key;
    private String name;
    private String description;
    private BoardTemplate boardTemplate;
    private Integer sprintDurationWeeks;
    private OwnerInfo owner;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Owner information included in project response.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OwnerInfo {
        private Long id;
        private String email;
        private String firstName;
        private String lastName;
    }
}

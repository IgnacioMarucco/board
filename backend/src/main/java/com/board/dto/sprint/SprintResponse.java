package com.board.dto.sprint;

import com.board.entity.enums.SprintStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for sprint data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintResponse {

    private Long id;
    private String name;
    private String goal;
    private LocalDate startDate;
    private LocalDate endDate;
    private SprintStatus status;
    private Long projectId;
    private String projectKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

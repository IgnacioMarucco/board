package com.board.dto.epic;

import com.board.entity.enums.EpicStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for epic data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpicResponse {

    private Long id;
    private String key;
    private String title;
    private String description;
    private EpicStatus status;
    private Long projectId;
    private String projectKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

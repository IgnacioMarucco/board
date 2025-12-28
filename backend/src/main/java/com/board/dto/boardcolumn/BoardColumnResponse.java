package com.board.dto.boardcolumn;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for board column data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardColumnResponse {

    private Long id;
    private String name;
    private Integer position;
    private Integer wipLimit;
    private Long projectId;
    private String projectKey;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

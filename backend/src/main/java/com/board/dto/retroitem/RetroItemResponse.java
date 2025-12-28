package com.board.dto.retroitem;

import com.board.entity.enums.RetroCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for retro item data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetroItemResponse {

    private Long id;
    private String content;
    private RetroCategory category;
    private Integer votes;
    private Boolean isAnonymous;
    private Long ceremonyId;
    private Long authorId;
    private String authorEmail;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

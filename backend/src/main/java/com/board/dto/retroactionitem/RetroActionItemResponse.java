package com.board.dto.retroactionitem;

import com.board.entity.enums.RetroActionItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for retro action items.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetroActionItemResponse {

    private Long id;
    private String title;
    private String description;
    private RetroActionItemStatus status;
    private LocalDate dueDate;
    private Long assigneeId;
    private String assigneeEmail;
    private Long ceremonyId;
    private Long retroItemId;
    private Long taskId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

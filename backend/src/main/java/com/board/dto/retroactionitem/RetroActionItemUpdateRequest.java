package com.board.dto.retroactionitem;

import com.board.entity.enums.RetroActionItemStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for updating a retro action item.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetroActionItemUpdateRequest {

    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    private RetroActionItemStatus status;

    private LocalDate dueDate;

    private Long assigneeId;
}

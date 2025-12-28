package com.board.dto.epic;

import com.board.entity.enums.EpicStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating an existing epic.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpicUpdateRequest {

    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    private EpicStatus status;
}

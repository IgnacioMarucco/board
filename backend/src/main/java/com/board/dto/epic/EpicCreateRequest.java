package com.board.dto.epic;

import com.board.entity.enums.EpicStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new epic.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpicCreateRequest {

    @NotBlank(message = "Epic key is required")
    @Size(min = 2, max = 20, message = "Epic key must be 2-20 characters")
    private String key;

    @NotBlank(message = "Epic title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    private EpicStatus status;
}

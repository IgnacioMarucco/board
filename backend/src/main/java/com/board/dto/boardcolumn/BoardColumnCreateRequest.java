package com.board.dto.boardcolumn;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new board column.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardColumnCreateRequest {

    @NotBlank(message = "Column name is required")
    @Size(max = 100, message = "Column name cannot exceed 100 characters")
    private String name;

    @NotNull(message = "Position is required")
    @Min(value = 0, message = "Position must be non-negative")
    private Integer position;

    @Min(value = 1, message = "WIP limit must be at least 1")
    private Integer wipLimit;
}

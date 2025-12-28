package com.board.dto.retroitem;

import com.board.entity.enums.RetroCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a retro item.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetroItemCreateRequest {

    @NotBlank(message = "Content is required")
    @Size(max = 1000, message = "Content cannot exceed 1000 characters")
    private String content;

    @NotNull(message = "Category is required")
    private RetroCategory category;

    @Builder.Default
    private Boolean isAnonymous = false;
}

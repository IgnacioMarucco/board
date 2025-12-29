package com.board.dto.sprint;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for creating a new sprint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintCreateRequest {

    @NotBlank(message = "Sprint name is required")
    @Size(max = 100, message = "Sprint name cannot exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Goal cannot exceed 500 characters")
    private String goal;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @Future(message = "End date must be in the future")
    private LocalDate endDate;

    @Min(value = 0, message = "Capacity points must be non-negative")
    private Integer capacityPoints;
}

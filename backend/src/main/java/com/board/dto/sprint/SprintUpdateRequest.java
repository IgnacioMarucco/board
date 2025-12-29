package com.board.dto.sprint;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for updating an existing sprint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SprintUpdateRequest {

    @Size(max = 100, message = "Sprint name cannot exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Goal cannot exceed 500 characters")
    private String goal;

    private LocalDate startDate;

    private LocalDate endDate;

    @Min(value = 0, message = "Capacity points must be non-negative")
    private Integer capacityPoints;
}

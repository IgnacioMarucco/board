package com.board.dto.ceremony;

import com.board.entity.enums.CeremonyType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Request DTO for creating a ceremony.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CeremonyCreateRequest {

    @NotNull(message = "Ceremony type is required")
    private CeremonyType type;

    @NotNull(message = "Scheduled time is required")
    private LocalDateTime scheduledAt;

    @Positive(message = "Duration must be positive")
    private Integer durationMinutes;

    private String notes;

    private Set<Long> participantIds;
}

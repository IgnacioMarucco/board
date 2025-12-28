package com.board.dto.ceremony;

import com.board.entity.enums.CeremonyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Request DTO for updating a ceremony.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CeremonyUpdateRequest {

    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private CeremonyStatus status;
    private String notes;
    private Set<Long> participantIds;
}

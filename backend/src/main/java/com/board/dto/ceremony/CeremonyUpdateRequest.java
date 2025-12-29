package com.board.dto.ceremony;

import com.board.entity.enums.CeremonyStatus;
import com.board.entity.enums.RetroTemplate;
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
    private RetroTemplate retroTemplate;
    private Set<Long> participantIds;
}

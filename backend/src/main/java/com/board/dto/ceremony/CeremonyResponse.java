package com.board.dto.ceremony;

import com.board.entity.enums.CeremonyStatus;
import com.board.entity.enums.CeremonyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response DTO for ceremony data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CeremonyResponse {

    private Long id;
    private CeremonyType type;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private CeremonyStatus status;
    private String notes;
    private Long sprintId;
    private String sprintName;
    private Set<Long> participantIds;
    private Integer retroItemCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

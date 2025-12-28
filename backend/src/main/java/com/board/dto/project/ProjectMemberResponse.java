package com.board.dto.project;

import com.board.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for project member data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberResponse {

    private Long id;
    private Long userId;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private LocalDateTime joinedAt;
}

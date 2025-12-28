package com.board.dto.attachment;

import com.board.entity.enums.AttachmentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for attachment data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {

    private Long id;
    private String filename;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private String url;
    private AttachmentType entityType;
    private Long entityId;
    private Long uploadedByUserId;
    private String uploadedByUserEmail;
    private LocalDateTime createdAt;
}

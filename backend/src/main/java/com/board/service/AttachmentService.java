package com.board.service;

import com.board.dto.attachment.AttachmentResponse;
import com.board.entity.enums.AttachmentType;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Service interface for attachment operations.
 */
public interface AttachmentService {

    /**
     * Uploads a file to MinIO and stores metadata.
     *
     * @param file       the file to upload
     * @param entityType the entity type
     * @param entityId   the entity ID
     * @param userId     the uploading user's ID
     * @return the attachment response
     */
    AttachmentResponse uploadAttachment(MultipartFile file, AttachmentType entityType,
            Long entityId, Long userId);

    /**
     * Downloads a file from MinIO.
     *
     * @param attachmentId the attachment ID
     * @param userId       the requesting user's ID
     * @return the file resource
     */
    Resource downloadAttachment(Long attachmentId, Long userId);

    /**
     * Gets all attachments for an entity.
     *
     * @param entityType the entity type
     * @param entityId   the entity ID
     * @param userId     the requesting user's ID
     * @return list of attachments
     */
    List<AttachmentResponse> getAttachmentsForEntity(AttachmentType entityType,
            Long entityId, Long userId);

    /**
     * Deletes an attachment from MinIO and database.
     *
     * @param attachmentId the attachment ID
     * @param userId       the requesting user's ID
     */
    void deleteAttachment(Long attachmentId, Long userId);

    /**
     * Gets attachment metadata by ID.
     *
     * @param attachmentId the attachment ID
     * @param userId       the requesting user's ID
     * @return the attachment response
     */
    AttachmentResponse getAttachment(Long attachmentId, Long userId);
}

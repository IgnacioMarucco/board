package com.board.service.impl;

import com.board.dto.attachment.AttachmentResponse;
import com.board.entity.Attachment;
import com.board.entity.User;
import com.board.entity.enums.AttachmentType;
import com.board.exception.BadRequestException;
import com.board.exception.ForbiddenException;
import com.board.exception.NotFoundException;
import com.board.mapper.AttachmentMapper;
import com.board.repository.AttachmentRepository;
import com.board.repository.UserRepository;
import com.board.service.AttachmentService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;

/**
 * Implementation of AttachmentService.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final UserRepository userRepository;
    private final AttachmentMapper attachmentMapper;
    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String minioEndpoint;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    @Override
    @Transactional
    public AttachmentResponse uploadAttachment(MultipartFile file, AttachmentType entityType,
            Long entityId, Long userId) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds 10MB limit");
        }

        User user = findUserById(userId);

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String filename = UUID.randomUUID() + "_" + originalFilename;

        try {
            // Upload to MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filename)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());

            // Save metadata to database
            String url = minioEndpoint + "/" + bucketName + "/" + filename;
            Attachment attachment = Attachment.builder()
                    .filename(filename)
                    .originalFilename(originalFilename)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .url(url)
                    .entityType(entityType)
                    .entityId(entityId)
                    .uploadedBy(user)
                    .build();

            attachment = attachmentRepository.save(attachment);
            return attachmentMapper.toResponse(attachment);

        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
            log.error("Error uploading file to MinIO", e);
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Resource downloadAttachment(Long attachmentId, Long userId) {
        Attachment attachment = findAttachmentById(attachmentId);
        validateAccess(userId);

        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(attachment.getFilename())
                            .build());
            return new InputStreamResource(stream);
        } catch (Exception e) {
            log.error("Error downloading file from MinIO", e);
            throw new BadRequestException("Failed to download file: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttachmentResponse> getAttachmentsForEntity(AttachmentType entityType,
            Long entityId, Long userId) {
        findUserById(userId);
        List<Attachment> attachments = attachmentRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
        return attachmentMapper.toResponseList(attachments);
    }

    @Override
    @Transactional
    public void deleteAttachment(Long attachmentId, Long userId) {
        Attachment attachment = findAttachmentById(attachmentId);
        validateOwnership(attachment, userId);

        try {
            // Delete from MinIO
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(attachment.getFilename())
                            .build());

            // Delete from database
            attachment.softDelete();
            attachmentRepository.save(attachment);

        } catch (Exception e) {
            log.error("Error deleting file from MinIO", e);
            throw new BadRequestException("Failed to delete file: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AttachmentResponse getAttachment(Long attachmentId, Long userId) {
        Attachment attachment = findAttachmentById(attachmentId);
        validateAccess(userId);
        return attachmentMapper.toResponse(attachment);
    }

    private Attachment findAttachmentById(Long attachmentId) {
        return attachmentRepository.findById(attachmentId)
                .filter(a -> a.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Attachment not found"));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private void validateAccess(Long userId) {
        findUserById(userId);
        // Basic access check - all authenticated users can view
        // Can be extended with project membership checks
    }

    private void validateOwnership(Attachment attachment, Long userId) {
        if (!attachment.getUploadedBy().getId().equals(userId)) {
            throw new ForbiddenException("You can only delete your own attachments");
        }
    }
}

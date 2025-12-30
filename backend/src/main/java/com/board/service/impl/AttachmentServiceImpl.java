package com.board.service.impl;

import com.board.dto.attachment.AttachmentResponse;
import com.board.entity.Attachment;
import com.board.entity.Ceremony;
import com.board.entity.Comment;
import com.board.entity.Epic;
import com.board.entity.Project;
import com.board.entity.Story;
import com.board.entity.Task;
import com.board.entity.User;
import com.board.entity.enums.AttachmentType;
import com.board.exception.BadRequestException;
import com.board.exception.ForbiddenException;
import com.board.exception.NotFoundException;
import com.board.mapper.AttachmentMapper;
import com.board.repository.AttachmentRepository;
import com.board.repository.CeremonyRepository;
import com.board.repository.CommentRepository;
import com.board.repository.EpicRepository;
import com.board.repository.ProjectMemberRepository;
import com.board.repository.StoryRepository;
import com.board.repository.TaskRepository;
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
    private final EpicRepository epicRepository;
    private final StoryRepository storyRepository;
    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;
    private final CeremonyRepository ceremonyRepository;
    private final ProjectMemberRepository projectMemberRepository;

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
        Project project = resolveProjectForEntity(entityType, entityId);
        validateMembership(project, userId);

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
        Project project = resolveProjectForEntity(attachment.getEntityType(), attachment.getEntityId());
        validateMembership(project, userId);

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
        Project project = resolveProjectForEntity(entityType, entityId);
        validateMembership(project, userId);
        List<Attachment> attachments = attachmentRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId);
        return attachmentMapper.toResponseList(attachments);
    }

    @Override
    @Transactional
    public void deleteAttachment(Long attachmentId, Long userId) {
        Attachment attachment = findAttachmentById(attachmentId);
        Project project = resolveProjectForEntity(attachment.getEntityType(), attachment.getEntityId());
        validateMembership(project, userId);
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
        Project project = resolveProjectForEntity(attachment.getEntityType(), attachment.getEntityId());
        validateMembership(project, userId);
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

    private void validateMembership(Project project, Long userId) {
        User user = findUserById(userId);
        if (!projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(project, user)) {
            throw new ForbiddenException("You are not a member of this project");
        }
    }

    private Project resolveProjectForEntity(AttachmentType entityType, Long entityId) {
        return switch (entityType) {
            case EPIC -> findEpicById(entityId).getProject();
            case STORY -> findStoryById(entityId).getEpic().getProject();
            case TASK -> findTaskById(entityId).getStory().getEpic().getProject();
            case COMMENT -> getProjectFromComment(findCommentById(entityId));
            case CEREMONY -> findCeremonyById(entityId).getSprint().getProject();
        };
    }

    private Project getProjectFromComment(Comment comment) {
        if (comment.getEpic() != null) {
            return comment.getEpic().getProject();
        } else if (comment.getStory() != null) {
            return comment.getStory().getEpic().getProject();
        } else if (comment.getTask() != null) {
            return comment.getTask().getStory().getEpic().getProject();
        }
        throw new IllegalStateException("Comment must be associated with an Epic, Story, or Task");
    }

    private void validateOwnership(Attachment attachment, Long userId) {
        if (!attachment.getUploadedBy().getId().equals(userId)) {
            throw new ForbiddenException("You can only delete your own attachments");
        }
    }

    private Epic findEpicById(Long epicId) {
        return epicRepository.findById(epicId)
                .filter(e -> e.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Epic not found"));
    }

    private Story findStoryById(Long storyId) {
        return storyRepository.findById(storyId)
                .filter(s -> s.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Story not found"));
    }

    private Task findTaskById(Long taskId) {
        return taskRepository.findById(taskId)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Task not found"));
    }

    private Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Comment not found"));
    }

    private Ceremony findCeremonyById(Long ceremonyId) {
        return ceremonyRepository.findById(ceremonyId)
                .filter(c -> c.getDeletedAt() == null)
                .orElseThrow(() -> new NotFoundException("Ceremony not found"));
    }
}

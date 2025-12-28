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
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AttachmentServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class AttachmentServiceImplTest {

    @Mock
    private AttachmentRepository attachmentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AttachmentMapper attachmentMapper;
    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private AttachmentServiceImpl attachmentService;

    private User testUser;
    private Attachment testAttachment;
    private AttachmentResponse testResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("test@example.com").build();
        testAttachment = Attachment.builder()
                .id(1L)
                .filename("test.pdf")
                .originalFilename("test.pdf")
                .uploadedBy(testUser)
                .build();
        testResponse = AttachmentResponse.builder().id(1L).build();

        ReflectionTestUtils.setField(attachmentService, "bucketName", "test-bucket");
        ReflectionTestUtils.setField(attachmentService, "minioEndpoint", "http://localhost:9000");
    }

    @Nested
    @DisplayName("uploadAttachment")
    class UploadAttachment {

        @Test
        @DisplayName("should throw BadRequestException when file is empty")
        void shouldThrowWhenFileIsEmpty() {
            // Given
            MultipartFile file = new MockMultipartFile(
                    "file", "test.pdf", "application/pdf", new byte[0]);

            // When/Then
            assertThatThrownBy(() -> attachmentService.uploadAttachment(
                    file, AttachmentType.COMMENT, 1L, 1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("File is empty");
        }

        @Test
        @DisplayName("should throw BadRequestException when file exceeds size limit")
        void shouldThrowWhenFileTooLarge() {
            // Given
            byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
            MultipartFile file = new MockMultipartFile(
                    "file", "test.pdf", "application/pdf", largeContent);

            // When/Then
            assertThatThrownBy(() -> attachmentService.uploadAttachment(
                    file, AttachmentType.COMMENT, 1L, 1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("File size exceeds 10MB limit");
        }
    }

    @Nested
    @DisplayName("getAttachmentsForEntity")
    class GetAttachmentsForEntity {

        @Test
        @DisplayName("should return attachments for entity")
        void shouldReturnAttachmentsForEntity() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(attachmentRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                    AttachmentType.COMMENT, 1L)).thenReturn(List.of(testAttachment));
            when(attachmentMapper.toResponseList(any())).thenReturn(List.of(testResponse));

            // When
            List<AttachmentResponse> responses = attachmentService.getAttachmentsForEntity(
                    AttachmentType.COMMENT, 1L, 1L);

            // Then
            assertThat(responses).hasSize(1);
        }
    }

    @Nested
    @DisplayName("getAttachment")
    class GetAttachment {

        @Test
        @DisplayName("should return attachment metadata")
        void shouldReturnAttachmentMetadata() {
            // Given
            when(attachmentRepository.findById(1L)).thenReturn(Optional.of(testAttachment));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(attachmentMapper.toResponse(testAttachment)).thenReturn(testResponse);

            // When
            AttachmentResponse response = attachmentService.getAttachment(1L, 1L);

            // Then
            assertThat(response).isNotNull();
            verify(attachmentMapper).toResponse(testAttachment);
        }

        @Test
        @DisplayName("should throw NotFoundException when attachment not found")
        void shouldThrowWhenAttachmentNotFound() {
            // Given
            when(attachmentRepository.findById(1L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> attachmentService.getAttachment(1L, 1L))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Attachment not found");
        }
    }

    @Nested
    @DisplayName("deleteAttachment")
    class DeleteAttachment {

        @Test
        @DisplayName("should throw ForbiddenException when not owner")
        void shouldThrowWhenNotOwner() {
            // Given
            User otherUser = User.builder().id(2L).build();
            testAttachment.setUploadedBy(otherUser);
            when(attachmentRepository.findById(1L)).thenReturn(Optional.of(testAttachment));

            // When/Then
            assertThatThrownBy(() -> attachmentService.deleteAttachment(1L, 1L))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("You can only delete your own attachments");
        }
    }
}

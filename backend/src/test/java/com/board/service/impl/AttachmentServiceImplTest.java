package com.board.service.impl;

import com.board.dto.attachment.AttachmentResponse;
import com.board.entity.Attachment;
import com.board.entity.Comment;
import com.board.entity.Epic;
import com.board.entity.Project;
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
    private EpicRepository epicRepository;
    @Mock
    private StoryRepository storyRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CeremonyRepository ceremonyRepository;
    @Mock
    private ProjectMemberRepository projectMemberRepository;
    @Mock
    private AttachmentMapper attachmentMapper;
    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private AttachmentServiceImpl attachmentService;

    private User testUser;
    private Project testProject;
    private Epic testEpic;
    private Comment testComment;
    private Attachment testAttachment;
    private AttachmentResponse testResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).email("test@example.com").build();
        testProject = Project.builder().id(1L).key("TEST").name("Test").owner(testUser).build();
        testEpic = Epic.builder().id(10L).key("EPIC-1").title("Epic").project(testProject).build();
        testComment = Comment.builder().id(100L).content("Comment").author(testUser).epic(testEpic).build();
        testAttachment = Attachment.builder()
                .id(1L)
                .filename("test.pdf")
                .originalFilename("test.pdf")
                .entityType(AttachmentType.COMMENT)
                .entityId(100L)
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
            when(commentRepository.findById(100L)).thenReturn(Optional.of(testComment));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(true);
            when(attachmentRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                    AttachmentType.COMMENT, 100L)).thenReturn(List.of(testAttachment));
            when(attachmentMapper.toResponseList(any())).thenReturn(List.of(testResponse));

            // When
            List<AttachmentResponse> responses = attachmentService.getAttachmentsForEntity(
                    AttachmentType.COMMENT, 100L, 1L);

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
            when(commentRepository.findById(100L)).thenReturn(Optional.of(testComment));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(true);
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
            when(commentRepository.findById(100L)).thenReturn(Optional.of(testComment));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(projectMemberRepository.existsByProjectAndUserAndDeletedAtIsNull(testProject, testUser))
                    .thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> attachmentService.deleteAttachment(1L, 1L))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessageContaining("You can only delete your own attachments");
        }
    }
}

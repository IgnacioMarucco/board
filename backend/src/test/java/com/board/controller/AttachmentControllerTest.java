package com.board.controller;

import com.board.dto.attachment.AttachmentResponse;
import com.board.entity.enums.AttachmentType;
import com.board.security.WithMockCustomUser;
import com.board.service.AttachmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests for AttachmentController.
 */
@WebMvcTest(AttachmentController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockCustomUser
class AttachmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AttachmentService attachmentService;

    @MockBean
    private com.board.security.JwtService jwtService;

    @MockBean
    private com.board.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    private AttachmentResponse attachmentResponse;

    @BeforeEach
    void setUp() {
        attachmentResponse = AttachmentResponse.builder()
                .id(1L)
                .filename("test.pdf")
                .originalFilename("test.pdf")
                .contentType("application/pdf")
                .build();
    }

    @Test
    @DisplayName("POST /upload should return 201")
    void uploadAttachmentShouldReturnCreated() throws Exception {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "content".getBytes());

        when(attachmentService.uploadAttachment(any(), eq(AttachmentType.COMMENT), eq(1L), any()))
                .thenReturn(attachmentResponse);

        // When/Then
        mockMvc.perform(multipart("/api/v1/attachments/upload")
                .file(file)
                .param("entityType", "COMMENT")
                .param("entityId", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /{id}/download should return file")
    void downloadAttachmentShouldReturnFile() throws Exception {
        // Given
        Resource resource = new ByteArrayResource("content".getBytes());
        when(attachmentService.getAttachment(eq(1L), any())).thenReturn(attachmentResponse);
        when(attachmentService.downloadAttachment(eq(1L), any())).thenReturn(resource);

        // When/Then
        mockMvc.perform(get("/api/v1/attachments/1/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"test.pdf\""));
    }

    @Test
    @DisplayName("GET / should return attachments list")
    void getAttachmentsForEntityShouldReturnList() throws Exception {
        // Given
        when(attachmentService.getAttachmentsForEntity(
                eq(AttachmentType.COMMENT), eq(1L), any()))
                .thenReturn(List.of(attachmentResponse));

        // When/Then
        mockMvc.perform(get("/api/v1/attachments")
                .param("entityType", "COMMENT")
                .param("entityId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("GET /{id} should return attachment metadata")
    void getAttachmentShouldReturnMetadata() throws Exception {
        // Given
        when(attachmentService.getAttachment(eq(1L), any())).thenReturn(attachmentResponse);

        // When/Then
        mockMvc.perform(get("/api/v1/attachments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("DELETE /{id} should return 204")
    void deleteAttachmentShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(attachmentService).deleteAttachment(eq(1L), any());

        // When/Then
        mockMvc.perform(delete("/api/v1/attachments/1"))
                .andExpect(status().isNoContent());
    }
}

package com.board.controller;

import com.board.dto.attachment.AttachmentResponse;
import com.board.entity.enums.AttachmentType;
import com.board.security.UserPrincipal;
import com.board.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST controller for attachment operations.
 */
@RestController
@RequestMapping("/api/v1/attachments")
@RequiredArgsConstructor
@Tag(name = "Attachments", description = "File attachment management endpoints")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a file")
    public ResponseEntity<AttachmentResponse> uploadAttachment(
            @RequestParam("file") MultipartFile file,
            @RequestParam("entityType") AttachmentType entityType,
            @RequestParam("entityId") Long entityId,
            @AuthenticationPrincipal UserPrincipal principal) {
        AttachmentResponse response = attachmentService.uploadAttachment(
                file, entityType, entityId, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{attachmentId}/download")
    @Operation(summary = "Download a file")
    public ResponseEntity<Resource> downloadAttachment(
            @PathVariable Long attachmentId,
            @AuthenticationPrincipal UserPrincipal principal) {

        AttachmentResponse metadata = attachmentService.getAttachment(
                attachmentId, principal.getUserId());
        Resource resource = attachmentService.downloadAttachment(
                attachmentId, principal.getUserId());

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + metadata.getOriginalFilename() + "\"")
                .body(resource);
    }

    @GetMapping
    @Operation(summary = "Get attachments for an entity")
    public ResponseEntity<List<AttachmentResponse>> getAttachmentsForEntity(
            @RequestParam("entityType") AttachmentType entityType,
            @RequestParam("entityId") Long entityId,
            @AuthenticationPrincipal UserPrincipal principal) {
        List<AttachmentResponse> attachments = attachmentService.getAttachmentsForEntity(
                entityType, entityId, principal.getUserId());
        return ResponseEntity.ok(attachments);
    }

    @GetMapping("/{attachmentId}")
    @Operation(summary = "Get attachment metadata")
    public ResponseEntity<AttachmentResponse> getAttachment(
            @PathVariable Long attachmentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(attachmentService.getAttachment(
                attachmentId, principal.getUserId()));
    }

    @DeleteMapping("/{attachmentId}")
    @Operation(summary = "Delete an attachment")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable Long attachmentId,
            @AuthenticationPrincipal UserPrincipal principal) {
        attachmentService.deleteAttachment(attachmentId, principal.getUserId());
        return ResponseEntity.noContent().build();
    }
}

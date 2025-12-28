package com.board.controller;

import com.board.dto.comment.CommentCreateRequest;
import com.board.dto.comment.CommentResponse;
import com.board.dto.comment.CommentUpdateRequest;
import com.board.security.WithMockCustomUser;
import com.board.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests for CommentController.
 */
@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockCustomUser
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @MockBean
    private com.board.security.JwtService jwtService;

    @MockBean
    private com.board.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    private CommentResponse commentResponse;

    @BeforeEach
    void setUp() {
        commentResponse = CommentResponse.builder()
                .id(1L)
                .content("Test comment")
                .authorId(1L)
                .authorEmail("user@example.com")
                .build();
    }

    @Test
    @DisplayName("POST /epics/{epicId}/comments should return 201")
    void createCommentOnEpicShouldReturnCreated() throws Exception {
        // Given
        CommentCreateRequest request = CommentCreateRequest.builder()
                .content("Test comment")
                .build();

        when(commentService.createCommentOnEpic(eq(1L), any(CommentCreateRequest.class), any()))
                .thenReturn(commentResponse);

        // When/Then
        mockMvc.perform(post("/api/v1/epics/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Test comment"));
    }

    @Test
    @DisplayName("GET /epics/{epicId}/comments should return 200")
    void getCommentsForEpicShouldReturnOk() throws Exception {
        // Given
        when(commentService.getCommentsForEpic(eq(1L), any()))
                .thenReturn(List.of(commentResponse));

        // When/Then
        mockMvc.perform(get("/api/v1/epics/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Test comment"));
    }

    @Test
    @DisplayName("POST /stories/{storyId}/comments should return 201")
    void createCommentOnStoryShouldReturnCreated() throws Exception {
        // Given
        CommentCreateRequest request = CommentCreateRequest.builder()
                .content("Test comment")
                .build();

        when(commentService.createCommentOnStory(eq(1L), any(CommentCreateRequest.class), any()))
                .thenReturn(commentResponse);

        // When/Then
        mockMvc.perform(post("/api/v1/stories/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("GET /stories/{storyId}/comments should return 200")
    void getCommentsForStoryShouldReturnOk() throws Exception {
        // Given
        when(commentService.getCommentsForStory(eq(1L), any()))
                .thenReturn(List.of(commentResponse));

        // When/Then
        mockMvc.perform(get("/api/v1/stories/1/comments"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /tasks/{taskId}/comments should return 201")
    void createCommentOnTaskShouldReturnCreated() throws Exception {
        // Given
        CommentCreateRequest request = CommentCreateRequest.builder()
                .content("Test comment")
                .build();

        when(commentService.createCommentOnTask(eq(1L), any(CommentCreateRequest.class), any()))
                .thenReturn(commentResponse);

        // When/Then
        mockMvc.perform(post("/api/v1/tasks/1/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("GET /tasks/{taskId}/comments should return 200")
    void getCommentsForTaskShouldReturnOk() throws Exception {
        // Given
        when(commentService.getCommentsForTask(eq(1L), any()))
                .thenReturn(List.of(commentResponse));

        // When/Then
        mockMvc.perform(get("/api/v1/tasks/1/comments"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /comments/{commentId} should return 200")
    void getCommentShouldReturnOk() throws Exception {
        // Given
        when(commentService.getComment(eq(1L), any())).thenReturn(commentResponse);

        // When/Then
        mockMvc.perform(get("/api/v1/comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PUT /comments/{commentId} should return 200")
    void updateCommentShouldReturnOk() throws Exception {
        // Given
        CommentUpdateRequest request = CommentUpdateRequest.builder()
                .content("Updated content")
                .build();

        when(commentService.updateComment(eq(1L), any(CommentUpdateRequest.class), any()))
                .thenReturn(commentResponse);

        // When/Then
        mockMvc.perform(put("/api/v1/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /comments/{commentId} should return 204")
    void deleteCommentShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(commentService).deleteComment(eq(1L), any());

        // When/Then
        mockMvc.perform(delete("/api/v1/comments/1"))
                .andExpect(status().isNoContent());
    }
}

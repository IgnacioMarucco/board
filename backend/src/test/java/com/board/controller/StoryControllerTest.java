package com.board.controller;

import com.board.dto.story.StoryAssignRequest;
import com.board.dto.story.StoryCreateRequest;
import com.board.dto.story.StoryResponse;
import com.board.dto.story.StoryUpdateRequest;
import com.board.entity.enums.Priority;
import com.board.entity.enums.StoryStatus;
import com.board.security.WithMockCustomUser;
import com.board.service.StoryService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests for StoryController.
 */
@WebMvcTest(StoryController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockCustomUser
class StoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StoryService storyService;

    @MockBean
    private com.board.security.JwtService jwtService;

    @MockBean
    private com.board.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    private StoryResponse storyResponse;

    @BeforeEach
    void setUp() {
        storyResponse = StoryResponse.builder()
                .id(1L)
                .key("STORY-1")
                .title("Story Title")
                .description("Story Description")
                .status(StoryStatus.BACKLOG)
                .priority(Priority.MEDIUM)
                .epicId(1L)
                .epicKey("EPIC-1")
                .build();
    }

    @Test
    @DisplayName("POST /projects/{projectId}/stories should return 201 Created")
    void createStoryShouldReturnCreated() throws Exception {
        // Given
        StoryCreateRequest request = StoryCreateRequest.builder()
                .key("STORY-1")
                .title("Story Title")
                .description("Description")
                .epicId(1L)
                .build();

        when(storyService.createStory(eq(1L), any(StoryCreateRequest.class), any()))
                .thenReturn(storyResponse);

        // When/Then
        mockMvc.perform(post("/api/v1/projects/1/stories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.key").value("STORY-1"))
                .andExpect(jsonPath("$.title").value("Story Title"));
    }

    @Test
    @DisplayName("GET /projects/{projectId}/stories should return 200 with list")
    void getStoriesShouldReturnOk() throws Exception {
        // Given
        when(storyService.getStoriesForProject(eq(1L), any())).thenReturn(List.of(storyResponse));

        // When/Then
        mockMvc.perform(get("/api/v1/projects/1/stories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].key").value("STORY-1"));
    }

    @Test
    @DisplayName("GET /projects/{projectId}/stories/{storyId} should return 200")
    void getStoryShouldReturnOk() throws Exception {
        // Given
        when(storyService.getStory(eq(1L), eq(1L), any())).thenReturn(storyResponse);

        // When/Then
        mockMvc.perform(get("/api/v1/projects/1/stories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Story Title"));
    }

    @Test
    @DisplayName("PUT /projects/{projectId}/stories/{storyId} should return 200")
    void updateStoryShouldReturnOk() throws Exception {
        // Given
        StoryUpdateRequest request = StoryUpdateRequest.builder()
                .title("Updated Title")
                .build();

        when(storyService.updateStory(eq(1L), eq(1L), any(StoryUpdateRequest.class), any()))
                .thenReturn(storyResponse);

        // When/Then
        mockMvc.perform(put("/api/v1/projects/1/stories/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /projects/{projectId}/stories/{storyId} should return 204 No Content")
    void deleteStoryShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(storyService).deleteStory(eq(1L), eq(1L), any());

        // When/Then
        mockMvc.perform(delete("/api/v1/projects/1/stories/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /projects/{projectId}/stories/{storyId}/assign should return 200")
    void assignStoryShouldReturnOk() throws Exception {
        // Given
        StoryAssignRequest request = StoryAssignRequest.builder()
                .assigneeId(2L)
                .build();

        when(storyService.assignStory(eq(1L), eq(1L), any(StoryAssignRequest.class), any()))
                .thenReturn(storyResponse);

        // When/Then
        mockMvc.perform(patch("/api/v1/projects/1/stories/1/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("STORY-1"));
    }
}

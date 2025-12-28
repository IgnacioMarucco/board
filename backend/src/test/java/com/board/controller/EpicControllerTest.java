package com.board.controller;

import com.board.dto.epic.EpicCreateRequest;
import com.board.dto.epic.EpicResponse;
import com.board.dto.epic.EpicUpdateRequest;
import com.board.entity.enums.EpicStatus;
import com.board.security.WithMockCustomUser;
import com.board.service.EpicService;
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
 * Controller tests for EpicController.
 */
@WebMvcTest(EpicController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockCustomUser
class EpicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EpicService epicService;

    @MockBean
    private com.board.security.JwtService jwtService;

    @MockBean
    private com.board.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    private EpicResponse epicResponse;

    @BeforeEach
    void setUp() {
        epicResponse = EpicResponse.builder()
                .id(1L)
                .key("EPIC-1")
                .title("Epic Title")
                .description("Epic Description")
                .status(EpicStatus.BACKLOG)
                .projectId(1L)
                .projectKey("TEST")
                .build();
    }

    @Test
    @DisplayName("POST /projects/{projectId}/epics should return 201 Created")
    void createEpicShouldReturnCreated() throws Exception {
        // Given
        EpicCreateRequest request = EpicCreateRequest.builder()
                .key("EPIC-1")
                .title("Epic Title")
                .description("Description")
                .build();

        when(epicService.createEpic(eq(1L), any(EpicCreateRequest.class), any()))
                .thenReturn(epicResponse);

        // When/Then
        mockMvc.perform(post("/api/v1/projects/1/epics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.key").value("EPIC-1"))
                .andExpect(jsonPath("$.title").value("Epic Title"));
    }

    @Test
    @DisplayName("GET /projects/{projectId}/epics should return 200 with list")
    void getEpicsShouldReturnOk() throws Exception {
        // Given
        when(epicService.getEpicsForProject(eq(1L), any())).thenReturn(List.of(epicResponse));

        // When/Then
        mockMvc.perform(get("/api/v1/projects/1/epics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].key").value("EPIC-1"));
    }

    @Test
    @DisplayName("GET /projects/{projectId}/epics/{epicId} should return 200")
    void getEpicShouldReturnOk() throws Exception {
        // Given
        when(epicService.getEpic(eq(1L), eq(1L), any())).thenReturn(epicResponse);

        // When/Then
        mockMvc.perform(get("/api/v1/projects/1/epics/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Epic Title"));
    }

    @Test
    @DisplayName("PUT /projects/{projectId}/epics/{epicId} should return 200")
    void updateEpicShouldReturnOk() throws Exception {
        // Given
        EpicUpdateRequest request = EpicUpdateRequest.builder()
                .title("Updated Title")
                .build();

        when(epicService.updateEpic(eq(1L), eq(1L), any(EpicUpdateRequest.class), any()))
                .thenReturn(epicResponse);

        // When/Then
        mockMvc.perform(put("/api/v1/projects/1/epics/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /projects/{projectId}/epics/{epicId} should return 204 No Content")
    void deleteEpicShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(epicService).deleteEpic(eq(1L), eq(1L), any());

        // When/Then
        mockMvc.perform(delete("/api/v1/projects/1/epics/1"))
                .andExpect(status().isNoContent());
    }
}

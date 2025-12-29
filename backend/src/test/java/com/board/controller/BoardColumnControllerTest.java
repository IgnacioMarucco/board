package com.board.controller;

import com.board.dto.boardcolumn.BoardColumnCreateRequest;
import com.board.dto.boardcolumn.BoardColumnResponse;
import com.board.dto.boardcolumn.BoardColumnUpdateRequest;
import com.board.exception.BadRequestException;
import com.board.security.WithMockCustomUser;
import com.board.service.BoardColumnService;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests for BoardColumnController.
 */
@WebMvcTest(BoardColumnController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockCustomUser
class BoardColumnControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BoardColumnService boardColumnService;

    @MockBean
    private com.board.security.JwtService jwtService;

    @MockBean
    private com.board.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    private BoardColumnResponse columnResponse;

    @BeforeEach
    void setUp() {
        columnResponse = BoardColumnResponse.builder()
                .id(1L)
                .name("To Do")
                .position(0)
                .wipLimit(5)
                .projectId(1L)
                .projectKey("TEST")
                .build();
    }

    @Test
    @DisplayName("POST /projects/{projectId}/columns should return 400")
    void createColumnShouldReturnBadRequest() throws Exception {
        // Given
        BoardColumnCreateRequest request = BoardColumnCreateRequest.builder()
                .name("To Do")
                .position(0)
                .wipLimit(5)
                .build();

        when(boardColumnService.createColumn(eq(1L), any(BoardColumnCreateRequest.class), any()))
                .thenThrow(new BadRequestException("Board columns are fixed by template in v1.0"));

        // When/Then
        mockMvc.perform(post("/api/v1/projects/1/columns")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /projects/{projectId}/columns should return 200 with list")
    void getColumnsShouldReturnOk() throws Exception {
        // Given
        when(boardColumnService.getColumnsForProject(eq(1L), any()))
                .thenReturn(List.of(columnResponse));

        // When/Then
        mockMvc.perform(get("/api/v1/projects/1/columns"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("To Do"));
    }

    @Test
    @DisplayName("GET /projects/{projectId}/columns/{columnId} should return 200")
    void getColumnShouldReturnOk() throws Exception {
        // Given
        when(boardColumnService.getColumn(eq(1L), eq(1L), any())).thenReturn(columnResponse);

        // When/Then
        mockMvc.perform(get("/api/v1/projects/1/columns/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("To Do"));
    }

    @Test
    @DisplayName("PUT /projects/{projectId}/columns/{columnId} should return 200")
    void updateColumnShouldReturnOk() throws Exception {
        // Given
        BoardColumnUpdateRequest request = BoardColumnUpdateRequest.builder()
                .wipLimit(10)
                .build();

        when(boardColumnService.updateColumn(eq(1L), eq(1L),
                any(BoardColumnUpdateRequest.class), any()))
                .thenReturn(columnResponse);

        // When/Then
        mockMvc.perform(put("/api/v1/projects/1/columns/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /projects/{projectId}/columns/{columnId} should return 400")
    void deleteColumnShouldReturnBadRequest() throws Exception {
        // Given
        doThrow(new BadRequestException("Board columns are fixed by template in v1.0"))
                .when(boardColumnService)
                .deleteColumn(eq(1L), eq(1L), any());

        // When/Then
        mockMvc.perform(delete("/api/v1/projects/1/columns/1"))
                .andExpect(status().isBadRequest());
    }
}

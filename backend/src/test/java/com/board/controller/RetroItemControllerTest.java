package com.board.controller;

import com.board.dto.retroitem.RetroItemCreateRequest;
import com.board.dto.retroitem.RetroItemResponse;
import com.board.dto.retroitem.RetroItemUpdateRequest;
import com.board.entity.enums.RetroCategory;
import com.board.security.WithMockCustomUser;
import com.board.service.RetroItemService;
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
 * Controller tests for RetroItemController.
 */
@WebMvcTest(RetroItemController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockCustomUser
class RetroItemControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private RetroItemService retroItemService;

        @MockBean
        private com.board.security.JwtService jwtService;

        @MockBean
        private com.board.security.JwtAuthenticationFilter jwtAuthenticationFilter;

        private RetroItemResponse retroItemResponse;

        @BeforeEach
        void setUp() {
                retroItemResponse = RetroItemResponse.builder()
                                .id(1L)
                                .content("Test item")
                                .category(RetroCategory.START)
                                .build();
        }

        @Test
        @DisplayName("POST /ceremonies/{ceremonyId}/retro-items should return 201")
        void createRetroItemShouldReturnCreated() throws Exception {
                // Given
                RetroItemCreateRequest request = RetroItemCreateRequest.builder()
                                .content("Test item")
                                .category(RetroCategory.START)
                                .isAnonymous(false)
                                .build();

                when(retroItemService.createRetroItem(eq(1L), any(RetroItemCreateRequest.class), any()))
                                .thenReturn(retroItemResponse);

                // When/Then
                mockMvc.perform(post("/api/v1/ceremonies/1/retro-items")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.content").value("Test item"));
        }

        @Test
        @DisplayName("GET /ceremonies/{ceremonyId}/retro-items should return 200")
        void getRetroItemsForCeremonyShouldReturnOk() throws Exception {
                // Given
                when(retroItemService.getRetroItemsForCeremony(eq(1L), any()))
                                .thenReturn(List.of(retroItemResponse));

                // When/Then
                mockMvc.perform(get("/api/v1/ceremonies/1/retro-items"))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("PUT /retro-items/{retroItemId} should return 200")
        void updateRetroItemShouldReturnOk() throws Exception {
                // Given
                RetroItemUpdateRequest request = RetroItemUpdateRequest.builder()
                                .content("Updated content")
                                .build();

                when(retroItemService.updateRetroItem(eq(1L), any(RetroItemUpdateRequest.class), any()))
                                .thenReturn(retroItemResponse);

                // When/Then
                mockMvc.perform(put("/api/v1/retro-items/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /retro-items/{retroItemId}/vote should return 200")
        void voteRetroItemShouldReturnOk() throws Exception {
                // Given
                when(retroItemService.voteRetroItem(eq(1L), any())).thenReturn(retroItemResponse);

                // When/Then
                mockMvc.perform(post("/api/v1/retro-items/1/vote"))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("DELETE /retro-items/{retroItemId} should return 204")
        void deleteRetroItemShouldReturnNoContent() throws Exception {
                // Given
                doNothing().when(retroItemService).deleteRetroItem(eq(1L), any());

                // When/Then
                mockMvc.perform(delete("/api/v1/retro-items/1"))
                                .andExpect(status().isNoContent());
        }
}

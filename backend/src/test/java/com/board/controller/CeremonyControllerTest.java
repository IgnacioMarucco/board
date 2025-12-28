package com.board.controller;

import com.board.dto.ceremony.CeremonyCreateRequest;
import com.board.dto.ceremony.CeremonyResponse;
import com.board.dto.ceremony.CeremonyUpdateRequest;
import com.board.entity.enums.CeremonyStatus;
import com.board.entity.enums.CeremonyType;
import com.board.security.WithMockCustomUser;
import com.board.service.CeremonyService;
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

import java.time.LocalDateTime;
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
 * Controller tests for CeremonyController.
 */
@WebMvcTest(CeremonyController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockCustomUser
class CeremonyControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private CeremonyService ceremonyService;

        @MockBean
        private com.board.security.JwtService jwtService;

        @MockBean
        private com.board.security.JwtAuthenticationFilter jwtAuthenticationFilter;

        private CeremonyResponse ceremonyResponse;

        @BeforeEach
        void setUp() {
                ceremonyResponse = CeremonyResponse.builder()
                                .id(1L)
                                .type(CeremonyType.PLANNING)
                                .build();
        }

        @Test
        @DisplayName("POST /sprints/{sprintId}/ceremonies should return 201")
        void createCeremonyShouldReturnCreated() throws Exception {
                // Given
                CeremonyCreateRequest request = CeremonyCreateRequest.builder()
                                .type(CeremonyType.PLANNING)
                                .scheduledAt(LocalDateTime.now().plusDays(1))
                                .durationMinutes(120)
                                .build();

                when(ceremonyService.createCeremony(eq(1L), any(CeremonyCreateRequest.class), any()))
                                .thenReturn(ceremonyResponse);

                // When/Then
                mockMvc.perform(post("/api/v1/sprints/1/ceremonies")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("GET /sprints/{sprintId}/ceremonies should return 200")
        void getCeremoniesForSprintShouldReturnOk() throws Exception {
                // Given
                when(ceremonyService.getCeremoniesForSprint(eq(1L), any()))
                                .thenReturn(List.of(ceremonyResponse));

                // When/Then
                mockMvc.perform(get("/api/v1/sprints/1/ceremonies"))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /ceremonies/{ceremonyId} should return 200")
        void getCeremonyShouldReturnOk() throws Exception {
                // Given
                when(ceremonyService.getCeremony(eq(1L), any())).thenReturn(ceremonyResponse);

                // When/Then
                mockMvc.perform(get("/api/v1/ceremonies/1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("PUT /ceremonies/{ceremonyId} should return 200")
        void updateCeremonyShouldReturnOk() throws Exception {
                // Given
                CeremonyUpdateRequest request = CeremonyUpdateRequest.builder()
                                .status(CeremonyStatus.COMPLETED)
                                .build();

                when(ceremonyService.updateCeremony(eq(1L), any(CeremonyUpdateRequest.class), any()))
                                .thenReturn(ceremonyResponse);

                // When/Then
                mockMvc.perform(put("/api/v1/ceremonies/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("DELETE /ceremonies/{ceremonyId} should return 204")
        void deleteCeremonyShouldReturnNoContent() throws Exception {
                // Given
                doNothing().when(ceremonyService).deleteCeremony(eq(1L), any());

                // When/Then
                mockMvc.perform(delete("/api/v1/ceremonies/1"))
                                .andExpect(status().isNoContent());
        }
}

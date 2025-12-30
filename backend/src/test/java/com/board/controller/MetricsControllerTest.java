package com.board.controller;

import com.board.dto.metrics.BurndownPointResponse;
import com.board.dto.metrics.BurndownResponse;
import com.board.dto.metrics.VelocityEntryResponse;
import com.board.dto.metrics.VelocityResponse;
import com.board.security.WithMockCustomUser;
import com.board.service.MetricsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests for MetricsController.
 */
@WebMvcTest(MetricsController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockCustomUser
class MetricsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MetricsService metricsService;

    @MockBean
    private com.board.security.JwtService jwtService;

    @MockBean
    private com.board.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /metrics/sprints/{id}/burndown should return burndown data")
    void getBurndownShouldReturnData() throws Exception {
        BurndownResponse response = BurndownResponse.builder()
                .sprintId(1L)
                .sprintName("Sprint 1")
                .committedPoints(10)
                .points(List.of(BurndownPointResponse.builder()
                        .date(LocalDate.of(2025, 1, 1))
                        .remainingPoints(8)
                        .build()))
                .build();

        when(metricsService.getBurndown(anyLong(), anyLong(), anyLong()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/projects/1/metrics/sprints/1/burndown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sprintId").value(1))
                .andExpect(jsonPath("$.points[0].remainingPoints").value(8));
    }

    @Test
    @DisplayName("GET /metrics/velocity should return velocity data")
    void getVelocityShouldReturnData() throws Exception {
        VelocityResponse response = VelocityResponse.builder()
                .projectId(1L)
                .sprints(List.of(VelocityEntryResponse.builder()
                        .sprintId(2L)
                        .sprintName("Sprint 2")
                        .completedPoints(5)
                        .endDate(LocalDate.of(2025, 1, 15))
                        .build()))
                .build();

        when(metricsService.getVelocity(anyLong(), anyLong(), isNull()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/projects/1/metrics/velocity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.sprints[0].completedPoints").value(5));
    }
}

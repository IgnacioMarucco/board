package com.board.controller;

import com.board.dto.activity.ActivityLogResponse;
import com.board.security.WithMockCustomUser;
import com.board.service.ActivityLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests for ActivityLogController.
 */
@WebMvcTest(ActivityLogController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockCustomUser
class ActivityLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActivityLogService activityLogService;

    @MockBean
    private com.board.security.JwtService jwtService;

    @MockBean
    private com.board.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /projects/{id}/activity should return activity list")
    void getProjectActivityShouldReturnList() throws Exception {
        ActivityLogResponse response = ActivityLogResponse.builder()
                .id(1L)
                .action("STATUS_CHANGED")
                .entityType("STORY")
                .entityId(10L)
                .actorId(2L)
                .actorUsername("actor")
                .build();

        when(activityLogService.getActivityForProject(anyLong(), anyLong()))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/projects/1/activity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].entityType").value("STORY"));
    }
}

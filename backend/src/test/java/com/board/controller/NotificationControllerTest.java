package com.board.controller;

import com.board.dto.notification.NotificationResponse;
import com.board.security.WithMockCustomUser;
import com.board.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests for NotificationController.
 */
@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockCustomUser
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private com.board.security.JwtService jwtService;

    @MockBean
    private com.board.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    private NotificationResponse notificationResponse;

    @BeforeEach
    void setUp() {
        notificationResponse = NotificationResponse.builder()
                .id(1L)
                .type("MENTION")
                .title("You were mentioned")
                .message("User mentioned you in a comment")
                .isRead(false)
                .build();
    }

    @Test
    @DisplayName("GET /notifications should return 200")
    void getUserNotificationsShouldReturnOk() throws Exception {
        // Given
        when(notificationService.getUserNotifications(any()))
                .thenReturn(List.of(notificationResponse));

        // When/Then
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("You were mentioned"));
    }

    @Test
    @DisplayName("GET /notifications/unread should return 200")
    void getUnreadNotificationsShouldReturnOk() throws Exception {
        // Given
        when(notificationService.getUnreadNotifications(any()))
                .thenReturn(List.of(notificationResponse));

        // When/Then
        mockMvc.perform(get("/api/v1/notifications/unread"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /notifications/{id}/read should return 200")
    void markAsReadShouldReturnOk() throws Exception {
        // Given
        when(notificationService.markAsRead(eq(1L), any())).thenReturn(notificationResponse);

        // When/Then
        mockMvc.perform(put("/api/v1/notifications/1/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PUT /notifications/read-all should return 204")
    void markAllAsReadShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(notificationService).markAllAsRead(any());

        // When/Then
        mockMvc.perform(put("/api/v1/notifications/read-all"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /notifications/{id} should return 204")
    void deleteNotificationShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(notificationService).deleteNotification(eq(1L), any());

        // When/Then
        mockMvc.perform(delete("/api/v1/notifications/1"))
                .andExpect(status().isNoContent());
    }
}

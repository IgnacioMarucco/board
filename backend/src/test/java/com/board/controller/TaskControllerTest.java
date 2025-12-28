package com.board.controller;

import com.board.dto.task.TaskCreateRequest;
import com.board.dto.task.TaskResponse;
import com.board.dto.task.TaskUpdateRequest;
import com.board.entity.enums.TaskStatus;
import com.board.security.WithMockCustomUser;
import com.board.service.TaskService;
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

import java.math.BigDecimal;
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
 * Controller tests for TaskController.
 */
@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockCustomUser
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @MockBean
    private com.board.security.JwtService jwtService;

    @MockBean
    private com.board.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    private TaskResponse taskResponse;

    @BeforeEach
    void setUp() {
        taskResponse = TaskResponse.builder()
                .id(1L)
                .key("TASK-1")
                .title("Task Title")
                .description("Task Description")
                .status(TaskStatus.TODO)
                .estimatedHours(BigDecimal.valueOf(5))
                .storyId(1L)
                .storyKey("STORY-1")
                .build();
    }

    @Test
    @DisplayName("POST /projects/{projectId}/stories/{storyId}/tasks should return 201")
    void createTaskShouldReturnCreated() throws Exception {
        // Given
        TaskCreateRequest request = TaskCreateRequest.builder()
                .key("TASK-1")
                .title("Task Title")
                .description("Description")
                .build();

        when(taskService.createTask(eq(1L), eq(1L), any(TaskCreateRequest.class), any()))
                .thenReturn(taskResponse);

        // When/Then
        mockMvc.perform(post("/api/v1/projects/1/stories/1/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.key").value("TASK-1"))
                .andExpect(jsonPath("$.title").value("Task Title"));
    }

    @Test
    @DisplayName("GET /projects/{projectId}/stories/{storyId}/tasks should return 200")
    void getTasksShouldReturnOk() throws Exception {
        // Given
        when(taskService.getTasksForStory(eq(1L), eq(1L), any())).thenReturn(List.of(taskResponse));

        // When/Then
        mockMvc.perform(get("/api/v1/projects/1/stories/1/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].key").value("TASK-1"));
    }

    @Test
    @DisplayName("GET /projects/{projectId}/stories/{storyId}/tasks/{taskId} should return 200")
    void getTaskShouldReturnOk() throws Exception {
        // Given
        when(taskService.getTask(eq(1L), eq(1L), eq(1L), any())).thenReturn(taskResponse);

        // When/Then
        mockMvc.perform(get("/api/v1/projects/1/stories/1/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Task Title"));
    }

    @Test
    @DisplayName("PUT /projects/{projectId}/stories/{storyId}/tasks/{taskId} should return 200")
    void updateTaskShouldReturnOk() throws Exception {
        // Given
        TaskUpdateRequest request = TaskUpdateRequest.builder()
                .title("Updated Title")
                .build();

        when(taskService.updateTask(eq(1L), eq(1L), eq(1L), any(TaskUpdateRequest.class), any()))
                .thenReturn(taskResponse);

        // When/Then
        mockMvc.perform(put("/api/v1/projects/1/stories/1/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /projects/{projectId}/stories/{storyId}/tasks/{taskId} should return 204")
    void deleteTaskShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(taskService).deleteTask(eq(1L), eq(1L), eq(1L), any());

        // When/Then
        mockMvc.perform(delete("/api/v1/projects/1/stories/1/tasks/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PATCH /projects/{projectId}/stories/{storyId}/tasks/{taskId}/complete should return 200")
    void completeTaskShouldReturnOk() throws Exception {
        // Given
        when(taskService.completeTask(eq(1L), eq(1L), eq(1L), any())).thenReturn(taskResponse);

        // When/Then
        mockMvc.perform(patch("/api/v1/projects/1/stories/1/tasks/1/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.key").value("TASK-1"));
    }
}

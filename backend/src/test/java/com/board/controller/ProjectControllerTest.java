package com.board.controller;

import com.board.dto.project.ProjectCreateRequest;
import com.board.dto.project.ProjectMemberRequest;
import com.board.dto.project.ProjectMemberResponse;
import com.board.dto.project.ProjectResponse;
import com.board.dto.project.ProjectUpdateRequest;
import com.board.entity.enums.Role;
import com.board.security.WithMockCustomUser;
import com.board.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests for ProjectController.
 */
@WebMvcTest(ProjectController.class)
@WithMockCustomUser
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private com.board.security.JwtService jwtService;

    private ProjectResponse projectResponse;
    private ProjectMemberResponse memberResponse;

    @BeforeEach
    void setUp() {
        projectResponse = ProjectResponse.builder()
                .id(1L)
                .key("TEST")
                .name("Test Project")
                .description("Description")
                .build();

        memberResponse = ProjectMemberResponse.builder()
                .id(1L)
                .userId(2L)
                .email("member@example.com")
                .role(Role.DEVELOPER)
                .build();
    }

    @Test
    @DisplayName("POST /projects should return 201 Created")
    void createProjectShouldReturnCreated() throws Exception {
        // Given
        ProjectCreateRequest request = ProjectCreateRequest.builder()
                .key("NEW")
                .name("New Project")
                .description("Description")
                .build();

        when(projectService.createProject(any(ProjectCreateRequest.class), any()))
                .thenReturn(projectResponse);

        // When/Then
        mockMvc.perform(post("/api/v1/projects")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.key").value("TEST"))
                .andExpect(jsonPath("$.name").value("Test Project"));
    }

    @Test
    @DisplayName("POST /projects should return 400 when key is missing")
    void createProjectShouldReturnBadRequestWhenKeyMissing() throws Exception {
        // Given
        ProjectCreateRequest request = ProjectCreateRequest.builder()
                .name("New Project")
                .build();

        // When/Then
        mockMvc.perform(post("/api/v1/projects")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /projects should return 200 with list")
    void getProjectsShouldReturnOk() throws Exception {
        // Given
        when(projectService.getProjectsForUser(any())).thenReturn(List.of(projectResponse));

        // When/Then
        mockMvc.perform(get("/api/v1/projects")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].key").value("TEST"));
    }

    @Test
    @DisplayName("GET /projects/{id} should return 200")
    void getProjectShouldReturnOk() throws Exception {
        // Given
        when(projectService.getProject(eq(1L), any())).thenReturn(projectResponse);

        // When/Then
        mockMvc.perform(get("/api/v1/projects/1")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Project"));
    }

    @Test
    @DisplayName("PUT /projects/{id} should return 200")
    void updateProjectShouldReturnOk() throws Exception {
        // Given
        ProjectUpdateRequest request = ProjectUpdateRequest.builder()
                .name("Updated Name")
                .build();

        when(projectService.updateProject(eq(1L), any(ProjectUpdateRequest.class), any()))
                .thenReturn(projectResponse);

        // When/Then
        mockMvc.perform(put("/api/v1/projects/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /projects/{id} should return 204 No Content")
    void deleteProjectShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(projectService).deleteProject(eq(1L), any());

        // When/Then
        mockMvc.perform(delete("/api/v1/projects/1")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /projects/{id}/members should return 201 Created")
    void addMemberShouldReturnCreated() throws Exception {
        // Given
        ProjectMemberRequest request = ProjectMemberRequest.builder()
                .userId(2L)
                .role(Role.DEVELOPER)
                .build();

        when(projectService.addMember(eq(1L), any(ProjectMemberRequest.class), any()))
                .thenReturn(memberResponse);

        // When/Then
        mockMvc.perform(post("/api/v1/projects/1/members")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(2))
                .andExpect(jsonPath("$.role").value("DEVELOPER"));
    }

    @Test
    @DisplayName("GET /projects/{id}/members should return 200")
    void getMembersShouldReturnOk() throws Exception {
        // Given
        when(projectService.getMembers(eq(1L), any())).thenReturn(List.of(memberResponse));

        // When/Then
        mockMvc.perform(get("/api/v1/projects/1/members")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(2));
    }

    @Test
    @DisplayName("DELETE /projects/{id}/members/{userId} should return 204 No Content")
    void removeMemberShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(projectService).removeMember(eq(1L), eq(2L), any());

        // When/Then
        mockMvc.perform(delete("/api/v1/projects/1/members/2")
                .with(csrf()))
                .andExpect(status().isNoContent());
    }
}

package com.board.controller;

import com.board.dto.projectmember.ProjectMemberInviteRequest;
import com.board.dto.projectmember.ProjectMemberResponse;
import com.board.dto.projectmember.ProjectMemberUpdateRequest;
import com.board.entity.enums.Role;
import com.board.security.WithMockCustomUser;
import com.board.service.ProjectMemberService;
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
 * Controller tests for ProjectMemberController.
 */
@WebMvcTest(ProjectMemberController.class)
@AutoConfigureMockMvc(addFilters = false)
@WithMockCustomUser
class ProjectMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectMemberService projectMemberService;

    @MockBean
    private com.board.security.JwtService jwtService;

    @MockBean
    private com.board.security.JwtAuthenticationFilter jwtAuthenticationFilter;

    private ProjectMemberResponse memberResponse;

    @BeforeEach
    void setUp() {
        memberResponse = ProjectMemberResponse.builder()
                .id(1L)
                .userId(2L)
                .userEmail("member@example.com")
                .userName("Member")
                .role(Role.DEVELOPER)
                .build();
    }

    @Test
    @DisplayName("POST /projects/{projectId}/members should return 201")
    void inviteMemberShouldReturnCreated() throws Exception {
        // Given
        ProjectMemberInviteRequest request = ProjectMemberInviteRequest.builder()
                .userEmail("member@example.com")
                .role(Role.DEVELOPER)
                .build();

        when(projectMemberService.inviteMember(eq(1L),
                any(ProjectMemberInviteRequest.class), any()))
                .thenReturn(memberResponse);

        // When/Then
        mockMvc.perform(post("/api/v1/projects/1/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userEmail").value("member@example.com"));
    }

    @Test
    @DisplayName("GET /projects/{projectId}/members should return 200")
    void getProjectMembersShouldReturnOk() throws Exception {
        // Given
        when(projectMemberService.getProjectMembers(eq(1L), any()))
                .thenReturn(List.of(memberResponse));

        // When/Then
        mockMvc.perform(get("/api/v1/projects/1/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userEmail").value("member@example.com"));
    }

    @Test
    @DisplayName("GET /projects/{projectId}/members/{memberId} should return 200")
    void getProjectMemberShouldReturnOk() throws Exception {
        // Given
        when(projectMemberService.getProjectMember(eq(1L), eq(1L), any()))
                .thenReturn(memberResponse);

        // When/Then
        mockMvc.perform(get("/api/v1/projects/1/members/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PUT /projects/{projectId}/members/{memberId} should return 200")
    void updateMemberRoleShouldReturnOk() throws Exception {
        // Given
        ProjectMemberUpdateRequest request = ProjectMemberUpdateRequest.builder()
                .role(Role.SCRUM_MASTER)
                .build();

        when(projectMemberService.updateMemberRole(eq(1L), eq(1L),
                any(ProjectMemberUpdateRequest.class), any()))
                .thenReturn(memberResponse);

        // When/Then
        mockMvc.perform(put("/api/v1/projects/1/members/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /projects/{projectId}/members/{memberId} should return 204")
    void removeMemberShouldReturnNoContent() throws Exception {
        // Given
        doNothing().when(projectMemberService).removeMember(eq(1L), eq(1L), any());

        // When/Then
        mockMvc.perform(delete("/api/v1/projects/1/members/1"))
                .andExpect(status().isNoContent());
    }
}

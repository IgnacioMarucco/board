package com.board.controller;

import com.board.dto.auth.AuthResponse;
import com.board.dto.auth.LoginRequest;
import com.board.dto.auth.RefreshTokenRequest;
import com.board.dto.auth.RegisterRequest;
import com.board.service.AuthService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Controller tests for AuthController.
 */
@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private AuthService authService;

        @MockBean
        private com.board.security.JwtService jwtService;

        private AuthResponse authResponse;

        @BeforeEach
        void setUp() {
                authResponse = AuthResponse.builder()
                                .accessToken("test-access-token")
                                .refreshToken("test-refresh-token")
                                .expiresIn(900L)
                                .user(AuthResponse.UserInfo.builder()
                                                .id(1L)
                                                .email("test@example.com")
                                                .firstName("Test")
                                                .lastName("User")
                                                .build())
                                .build();
        }

        @Test
        @DisplayName("POST /register should return 201 Created with auth response")
        void registerShouldReturnCreated() throws Exception {
                // Given
                RegisterRequest request = RegisterRequest.builder()
                                .email("new@example.com")
                                .password("password123")
                                .firstName("New")
                                .lastName("User")
                                .build();

                when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

                // When/Then
                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.accessToken").value("test-access-token"))
                                .andExpect(jsonPath("$.refreshToken").value("test-refresh-token"))
                                .andExpect(jsonPath("$.user.email").value("test@example.com"));
        }

        @Test
        @DisplayName("POST /register should return 400 when email is invalid")
        void registerShouldReturnBadRequestForInvalidEmail() throws Exception {
                // Given
                RegisterRequest request = RegisterRequest.builder()
                                .email("invalid-email")
                                .password("password123")
                                .firstName("Test")
                                .lastName("User")
                                .build();

                // When/Then
                mockMvc.perform(post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /login should return 200 OK with auth response")
        void loginShouldReturnOk() throws Exception {
                // Given
                LoginRequest request = LoginRequest.builder()
                                .email("test@example.com")
                                .password("password123")
                                .build();

                when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

                // When/Then
                mockMvc.perform(post("/api/v1/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").value("test-access-token"))
                                .andExpect(jsonPath("$.user.id").value(1));
        }

        @Test
        @DisplayName("POST /refresh should return 200 OK with new tokens")
        void refreshShouldReturnOk() throws Exception {
                // Given
                RefreshTokenRequest request = RefreshTokenRequest.builder()
                                .refreshToken("old-refresh-token")
                                .build();

                when(authService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(authResponse);

                // When/Then
                mockMvc.perform(post("/api/v1/auth/refresh")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").value("test-access-token"));
        }

        @Test
        @DisplayName("POST /logout should return 204 No Content")
        void logoutShouldReturnNoContent() throws Exception {
                // Given
                RefreshTokenRequest request = RefreshTokenRequest.builder()
                                .refreshToken("test-refresh-token")
                                .build();

                doNothing().when(authService).logout(any());

                // When/Then
                mockMvc.perform(post("/api/v1/auth/logout")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isNoContent());
        }
}

package com.board.service.impl;

import com.board.dto.auth.AuthResponse;
import com.board.dto.auth.LoginRequest;
import com.board.dto.auth.RefreshTokenRequest;
import com.board.dto.auth.RegisterRequest;
import com.board.entity.RefreshToken;
import com.board.entity.User;
import com.board.exception.BadRequestException;
import com.board.exception.UnauthorizedException;
import com.board.repository.RefreshTokenRepository;
import com.board.repository.UserRepository;
import com.board.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuthServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private RefreshToken testRefreshToken;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshExpirationMs", 604800000L);

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("hashedPassword")
                .firstName("Test")
                .lastName("User")
                .build();

        testRefreshToken = RefreshToken.builder()
                .id(1L)
                .token("test-refresh-token")
                .user(testUser)
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();
    }

    @Nested
    @DisplayName("register")
    class Register {

        @Test
        @DisplayName("should register new user successfully")
        void shouldRegisterNewUser() {
            // Given
            RegisterRequest request = RegisterRequest.builder()
                    .email("new@example.com")
                    .password("password123")
                    .firstName("New")
                    .lastName("User")
                    .build();

            when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(request.getPassword())).thenReturn("hashedPassword");
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(jwtService.generateAccessToken(any(), anyString())).thenReturn("access-token");
            when(jwtService.getExpirationMs()).thenReturn(900000L);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testRefreshToken);

            // When
            AuthResponse response = authService.register(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("access-token");
            assertThat(response.getRefreshToken()).isEqualTo("test-refresh-token");
            assertThat(response.getUser().getEmail()).isEqualTo("test@example.com");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("should throw BadRequestException when email already exists")
        void shouldThrowWhenEmailExists() {
            // Given
            RegisterRequest request = RegisterRequest.builder()
                    .email("existing@example.com")
                    .password("password123")
                    .firstName("Test")
                    .lastName("User")
                    .build();

            when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Email already registered");
        }
    }

    @Nested
    @DisplayName("login")
    class Login {

        @Test
        @DisplayName("should login successfully with valid credentials")
        void shouldLoginWithValidCredentials() {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("password123")
                    .build();

            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(request.getPassword(), testUser.getPassword()))
                    .thenReturn(true);
            when(jwtService.generateAccessToken(any(), anyString())).thenReturn("access-token");
            when(jwtService.getExpirationMs()).thenReturn(900000L);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testRefreshToken);

            // When
            AuthResponse response = authService.login(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("access-token");
        }

        @Test
        @DisplayName("should throw UnauthorizedException when user not found")
        void shouldThrowWhenUserNotFound() {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .email("notfound@example.com")
                    .password("password123")
                    .build();

            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Invalid credentials");
        }

        @Test
        @DisplayName("should throw UnauthorizedException when password is wrong")
        void shouldThrowWhenPasswordWrong() {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .email("test@example.com")
                    .password("wrongpassword")
                    .build();

            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches(request.getPassword(), testUser.getPassword()))
                    .thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Invalid credentials");
        }
    }

    @Nested
    @DisplayName("refreshToken")
    class RefreshTokenTests {

        @Test
        @DisplayName("should refresh token successfully")
        void shouldRefreshTokenSuccessfully() {
            // Given
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("test-refresh-token")
                    .build();

            when(refreshTokenRepository.findByTokenAndRevokedFalse(request.getRefreshToken()))
                    .thenReturn(Optional.of(testRefreshToken));
            when(jwtService.generateAccessToken(any(), anyString())).thenReturn("new-access-token");
            when(jwtService.getExpirationMs()).thenReturn(900000L);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testRefreshToken);

            // When
            AuthResponse response = authService.refreshToken(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("new-access-token");
            verify(refreshTokenRepository).save(testRefreshToken);
        }

        @Test
        @DisplayName("should throw UnauthorizedException when refresh token not found")
        void shouldThrowWhenTokenNotFound() {
            // Given
            RefreshTokenRequest request = RefreshTokenRequest.builder()
                    .refreshToken("invalid-token")
                    .build();

            when(refreshTokenRepository.findByTokenAndRevokedFalse(request.getRefreshToken()))
                    .thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> authService.refreshToken(request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Invalid refresh token");
        }
    }

    @Nested
    @DisplayName("logout")
    class Logout {

        @Test
        @DisplayName("should revoke refresh token on logout")
        void shouldRevokeTokenOnLogout() {
            // Given
            when(refreshTokenRepository.findByToken("test-refresh-token"))
                    .thenReturn(Optional.of(testRefreshToken));

            // When
            authService.logout("test-refresh-token");

            // Then
            verify(refreshTokenRepository).save(testRefreshToken);
            assertThat(testRefreshToken.getRevoked()).isTrue();
        }
    }
}

package com.board.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for JwtService.
 */
class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET = Base64.getEncoder()
            .encodeToString("test-secret-key-that-is-at-least-256-bits-long-for-hmac".getBytes());
    private static final long EXPIRATION_MS = 900000L;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", EXPIRATION_MS);
    }

    @Nested
    @DisplayName("generateAccessToken")
    class GenerateAccessToken {

        @Test
        @DisplayName("should generate a valid token")
        void shouldGenerateValidToken() {
            // When
            String token = jwtService.generateAccessToken(1L, "test@example.com");

            // Then
            assertThat(token).isNotBlank();
            assertThat(jwtService.isTokenValid(token)).isTrue();
        }

        @Test
        @DisplayName("should include userId in token")
        void shouldIncludeUserId() {
            // When
            String token = jwtService.generateAccessToken(42L, "test@example.com");

            // Then
            Long userId = jwtService.extractUserId(token);
            assertThat(userId).isEqualTo(42L);
        }

        @Test
        @DisplayName("should include email in token")
        void shouldIncludeEmail() {
            // When
            String token = jwtService.generateAccessToken(1L, "email@test.com");

            // Then
            String email = jwtService.extractEmail(token);
            assertThat(email).isEqualTo("email@test.com");
        }
    }

    @Nested
    @DisplayName("isTokenValid")
    class IsTokenValid {

        @Test
        @DisplayName("should return true for valid token")
        void shouldReturnTrueForValidToken() {
            // Given
            String token = jwtService.generateAccessToken(1L, "test@example.com");

            // When/Then
            assertThat(jwtService.isTokenValid(token)).isTrue();
        }

        @Test
        @DisplayName("should return false for invalid token")
        void shouldReturnFalseForInvalidToken() {
            // When/Then
            assertThat(jwtService.isTokenValid("invalid.token.here")).isFalse();
        }

        @Test
        @DisplayName("should return false for tampered token")
        void shouldReturnFalseForTamperedToken() {
            // Given
            String token = jwtService.generateAccessToken(1L, "test@example.com");
            String tamperedToken = token.substring(0, token.length() - 5) + "xxxxx";

            // When/Then
            assertThat(jwtService.isTokenValid(tamperedToken)).isFalse();
        }

        @Test
        @DisplayName("should return false for empty token")
        void shouldReturnFalseForEmptyToken() {
            // When/Then
            assertThat(jwtService.isTokenValid("")).isFalse();
        }
    }

    @Nested
    @DisplayName("getExpirationMs")
    class GetExpirationMs {

        @Test
        @DisplayName("should return configured expiration")
        void shouldReturnConfiguredExpiration() {
            // When/Then
            assertThat(jwtService.getExpirationMs()).isEqualTo(EXPIRATION_MS);
        }
    }
}

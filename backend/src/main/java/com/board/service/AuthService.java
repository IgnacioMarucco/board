package com.board.service;

import com.board.dto.auth.AuthResponse;
import com.board.dto.auth.LoginRequest;
import com.board.dto.auth.RefreshTokenRequest;
import com.board.dto.auth.RegisterRequest;

/**
 * Service interface for authentication operations.
 */
public interface AuthService {

    /**
     * Registers a new user.
     *
     * @param request the registration request
     * @return the auth response with tokens
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates a user.
     *
     * @param request the login request
     * @return the auth response with tokens
     */
    AuthResponse login(LoginRequest request);

    /**
     * Refreshes the access token.
     *
     * @param request the refresh token request
     * @return the auth response with new tokens
     */
    AuthResponse refreshToken(RefreshTokenRequest request);

    /**
     * Logs out a user by revoking their refresh token.
     *
     * @param refreshToken the refresh token to revoke
     */
    void logout(String refreshToken);
}

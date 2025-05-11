package com.authservice.service;

import com.authservice.model.dto.AuthResponse;
import com.authservice.model.dto.LoginRequest;
import com.authservice.model.dto.RefreshTokenRequest;
import com.authservice.model.dto.RegisterRequest;

/**
 * Service interface for authentication operations.
 */
public interface AuthService {

    /**
     * Register a new user.
     *
     * @param request the registration request
     * @return the authentication response with tokens
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticate a user and generate tokens.
     *
     * @param request the login request
     * @return the authentication response with tokens
     */
    AuthResponse login(LoginRequest request);

    /**
     * Refresh an access token using a refresh token.
     *
     * @param request the refresh token request
     * @return the authentication response with new tokens
     */
    AuthResponse refreshToken(RefreshTokenRequest request);

    /**
     * Logout a user by revoking their refresh token.
     *
     * @param refreshToken the refresh token to revoke
     */
    void logout(String refreshToken);
}

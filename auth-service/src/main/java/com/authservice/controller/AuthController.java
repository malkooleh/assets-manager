package com.authservice.controller;

import com.authservice.model.dto.AuthResponse;
import com.authservice.model.dto.LoginRequest;
import com.authservice.model.dto.RefreshTokenRequest;
import com.authservice.model.dto.RegisterRequest;
import com.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling authentication requests.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user.
     *
     * @param request the registration request
     * @return the authentication response with tokens
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Received registration request for user: {}", request.getUsername());
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Authenticate a user and generate tokens.
     *
     * @param request the login request
     * @return the authentication response with tokens
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Received login request for user: {}", request.getUsername());
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Refresh an access token using a refresh token.
     *
     * @param request the refresh token request
     * @return the authentication response with new tokens
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Received refresh token request");
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    /**
     * Logout a user by revoking their refresh token.
     *
     * @param request the refresh token request
     * @return a response entity with no content
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Received logout request");
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}

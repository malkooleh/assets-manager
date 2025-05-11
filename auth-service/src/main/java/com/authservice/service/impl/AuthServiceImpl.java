package com.authservice.service.impl;

import com.authservice.exception.AuthenticationException;
import com.authservice.exception.TokenRefreshException;
import com.authservice.model.dto.AuthResponse;
import com.authservice.model.dto.LoginRequest;
import com.authservice.model.dto.RefreshTokenRequest;
import com.authservice.model.dto.RegisterRequest;
import com.authservice.model.entity.RefreshToken;
import com.authservice.model.entity.Role;
import com.authservice.model.entity.User;
import com.authservice.repository.RefreshTokenRepository;
import com.authservice.repository.RoleRepository;
import com.authservice.repository.UserRepository;
import com.authservice.security.JwtService;
import com.authservice.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${security.jwt.token-validity}")
    private long tokenValidity;

    @Value("${security.jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with username: {}", request.getUsername());

        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AuthenticationException("Username is already taken");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthenticationException("Email is already in use");
        }

        // Get the default user role
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        // Create the user entity
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .roles(Collections.singleton(userRole))
                .build();

        // Save the user
        User savedUser = userRepository.save(user);

        // Generate tokens
        String accessToken = jwtService.generateToken(savedUser);
        RefreshToken refreshToken = createRefreshToken(savedUser);

        // Extract roles
        List<String> roles = savedUser.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .collect(Collectors.toList());

        // Return the authentication response
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(tokenValidity / 1000)
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .roles(roles)
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Authenticating user: {}", request.getUsername());

        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            // Get the authenticated user
            User user = (User) authentication.getPrincipal();

            // Generate tokens
            String accessToken = jwtService.generateToken(user);
            RefreshToken refreshToken = createRefreshToken(user);

            // Extract roles
            List<String> roles = user.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(authority -> authority.startsWith("ROLE_"))
                    .collect(Collectors.toList());

            // Return the authentication response
            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .tokenType("Bearer")
                    .expiresIn(tokenValidity / 1000)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .roles(roles)
                    .build();
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", request.getUsername(), e);
            throw new AuthenticationException("Invalid username or password");
        }
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        log.info("Refreshing token");

        // Validate the refresh token
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new TokenRefreshException("Invalid refresh token"));

        // Check if the token is expired or revoked
        if (refreshToken.isExpired() || refreshToken.isRevoked()) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenRefreshException("Refresh token is expired or revoked");
        }

        // Get the user
        User user = refreshToken.getUser();

        // Generate new tokens
        String accessToken = jwtService.generateToken(user);
        RefreshToken newRefreshToken = createRefreshToken(user);

        // Revoke the old refresh token
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        // Extract roles
        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .collect(Collectors.toList());

        // Return the authentication response
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(tokenValidity / 1000)
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(roles)
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        log.info("Logging out user with refresh token");

        // Find the refresh token
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    // Revoke the token
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    /**
     * Create a new refresh token for a user.
     *
     * @param user the user to create a token for
     * @return the created refresh token
     */
    private RefreshToken createRefreshToken(User user) {
        // Create the refresh token
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusSeconds(refreshTokenValidity / 1000))
                .revoked(false)
                .build();

        // Save the refresh token
        return refreshTokenRepository.save(refreshToken);
    }
}

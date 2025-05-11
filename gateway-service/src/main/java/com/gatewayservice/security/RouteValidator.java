package com.gatewayservice.security;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

/**
 * Validator to determine which routes require authentication and which are open.
 */
@Component
public class RouteValidator {

    /**
     * List of open API endpoints that don't require authentication.
     */
    public static final List<String> OPEN_API_ENDPOINTS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh-token",
            "/.well-known/jwks.json",
            "/actuator/health",
            "/actuator/info",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/webjars/**"
    );

    /**
     * Predicate to test if a request is for a secured endpoint.
     */
    public Predicate<ServerHttpRequest> isSecured =
            request -> OPEN_API_ENDPOINTS.stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
}

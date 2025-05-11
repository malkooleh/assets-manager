package com.gatewayservice.security;

import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Predicate;

/**
 * Global filter for JWT authentication.
 * This filter extracts the JWT from the Authorization header,
 * validates it, and adds user information to the request headers
 * for downstream services.
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter {

    @Autowired
    private RouteValidator routeValidator;

    @Autowired
    private JwtService jwtService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Check if the route is secured
        if (routeValidator.isSecured.test(request)) {
            // Check if the Authorization header is present
            if (!request.getHeaders().containsKey("Authorization")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // Extract the token from the Authorization header
            String authHeader = request.getHeaders().getFirst("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.substring(7);

            try {
                // Validate the token
                if (!jwtService.validateToken(token)) {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                // Extract claims from the token
                Claims claims = jwtService.extractAllClaims(token);

                // Add user information to request headers for downstream services
                ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                        .header("X-Auth-User-ID", claims.getSubject())
                        .header("X-Auth-Username", claims.get("preferred_username", String.class))
                        .build();

                // Add roles if present
                if (claims.get("roles") instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> roles = (List<String>) claims.get("roles");
                    if (!roles.isEmpty()) {
                        mutatedRequest = mutatedRequest.mutate()
                                .header("X-Auth-Roles", String.join(",", roles))
                                .build();
                    }
                }

                // Add permissions if present
                if (claims.get("permissions") instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> permissions = (List<String>) claims.get("permissions");
                    if (!permissions.isEmpty()) {
                        mutatedRequest = mutatedRequest.mutate()
                                .header("X-Auth-Permissions", String.join(",", permissions))
                                .build();
                    }
                }

                // Forward the request with the added headers
                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            } catch (Exception e) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        }

        // If the route is not secured, just forward the request
        return chain.filter(exchange);
    }
}

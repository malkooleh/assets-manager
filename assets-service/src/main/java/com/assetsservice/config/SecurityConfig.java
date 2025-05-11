package com.assetsservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Security configuration for the assets service.
 * This configuration relies on the gateway service for authentication and authorization.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Configure the security filter chain.
     *
     * @param http the HttpSecurity to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize
                        // Actuator endpoints for health checks
                        .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        // Swagger/OpenAPI endpoints
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Secure all other endpoints
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(gatewayAuthenticationFilter(), RequestHeaderAuthenticationFilter.class)
                .build();
    }

    /**
     * Create a filter that extracts authentication information from the gateway-provided headers.
     *
     * @return the filter
     */
    @Bean
    public OncePerRequestFilter gatewayAuthenticationFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                    throws ServletException, IOException {

                // Extract user information from headers set by the gateway
                String userId = request.getHeader("X-Auth-User-ID");
                String username = request.getHeader("X-Auth-Username");
                String rolesHeader = request.getHeader("X-Auth-Roles");
                String permissionsHeader = request.getHeader("X-Auth-Permissions");

                // If the headers are present, create an authentication token
                if (userId != null && username != null) {
                    // Parse roles and permissions
                    List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();

                    if (rolesHeader != null && !rolesHeader.isEmpty()) {
                        List<SimpleGrantedAuthority> roles = Arrays.stream(rolesHeader.split(","))
                                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                .collect(Collectors.toList());

                        authorities.addAll(roles);
                    }

                    if (permissionsHeader != null && !permissionsHeader.isEmpty()) {
                        List<SimpleGrantedAuthority> permissions = Arrays.stream(permissionsHeader.split(","))
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());

                        authorities.addAll(permissions);
                    }

                    // Create a user principal
                    GatewayAuthenticatedUser user = new GatewayAuthenticatedUser(userId, username, authorities);

                    // Create an authentication token
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user, null, authorities);

                    // Set the authentication in the security context
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

                // Continue with the filter chain
                filterChain.doFilter(request, response);
            }
        };
    }

    /**
     * Simple user principal class for gateway-authenticated users.
     */
    public record GatewayAuthenticatedUser(String id, String username, List<SimpleGrantedAuthority> authorities) {
    }
}

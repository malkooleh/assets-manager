package com.assetsservice.security;

import com.assetsservice.config.SecurityConfig.GatewayAuthenticatedUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Utility class for Spring Security.
 */
@Component
public class SecurityUtils {

    /**
     * Get the username of the current user.
     *
     * @return the username of the current user
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null) {
            return "system";
        }
        
        if (authentication.getPrincipal() instanceof GatewayAuthenticatedUser user) {
            return user.username();
        }
        
        return authentication.getName();
    }

    /**
     * Check if the current user has a specific authority.
     *
     * @param authority the authority to check
     * @return true if the current user has the authority, false otherwise
     */
    public static boolean hasAuthority(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority));
    }

    /**
     * Check if the current user has any of the specified authorities.
     *
     * @param authorities the authorities to check
     * @return true if the current user has any of the authorities, false otherwise
     */
    public static boolean hasAnyAuthority(String... authorities) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null) {
            return false;
        }
        
        for (String authority : authorities) {
            if (authentication.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals(authority))) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Get the user ID from the authentication context.
     *
     * @return the user ID or empty if not found
     */
    public static Optional<String> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof GatewayAuthenticatedUser user) {
            return Optional.ofNullable(user.id());
        }
        
        return Optional.empty();
    }

    /**
     * Get the roles of the current user from the authentication context.
     *
     * @return list of roles or empty list if not found
     */
    public static List<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null) {
            return Collections.emptyList();
        }
        
        return authentication.getAuthorities().stream()
                .filter(authority -> authority.getAuthority().startsWith("ROLE_"))
                .map(authority -> authority.getAuthority().substring(5)) // Remove "ROLE_" prefix
                .toList();
    }
}

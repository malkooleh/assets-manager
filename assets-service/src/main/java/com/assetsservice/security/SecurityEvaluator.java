package com.assetsservice.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Custom security evaluator for method security expressions.
 */
@Component("securityEvaluator")
public class SecurityEvaluator {

    /**
     * Check if the current authenticated user is the user with the given ID.
     *
     * @param userId the user ID to check
     * @return true if the current user has the given ID, false otherwise
     */
    public boolean isCurrentUser(Integer userId) {
        if (userId == null) {
            return false;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken authenticationToken) {
            Jwt jwt = authenticationToken.getToken();
            String subject = jwt.getClaimAsString("sub");

            // The subject claim should contain the user ID
            return subject != null && subject.equals(userId.toString());
        }

        return false;
    }

    /**
     * Check if the current user has permission to access data for a specific department.
     *
     * @param departmentId the department ID to check
     * @return true if the user has access to the department, false otherwise
     */
    public boolean hasDepartmentAccess(Integer departmentId) {
        if (departmentId == null) {
            return false;
        }

        // Check if user is admin (admins have access to all departments)
        if (SecurityUtils.hasAuthority("ROLE_ADMIN")) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken authenticationToken) {
            Jwt jwt = authenticationToken.getToken();

            // Check if user has access to the specific department
            // This assumes the JWT contains a claim with the departments the user has access to
            return jwt.getClaimAsStringList("departments")
                    .stream()
                    .anyMatch(dept -> dept.equals(departmentId.toString()));
        }

        return false;
    }
}

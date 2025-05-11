package com.authservice.repository;

import com.authservice.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    
    /**
     * Find a role by name.
     *
     * @param name the role name to search for
     * @return an Optional containing the role if found, or empty if not found
     */
    Optional<Role> findByName(String name);
    
    /**
     * Check if a role with the given name exists.
     *
     * @param name the role name to check
     * @return true if a role with the name exists, false otherwise
     */
    boolean existsByName(String name);
}

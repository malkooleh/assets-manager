package com.authservice.repository;

import com.authservice.model.entity.RefreshToken;
import com.authservice.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    /**
     * Find a refresh token by token value.
     *
     * @param token the token value to search for
     * @return an Optional containing the refresh token if found, or empty if not found
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Find all refresh tokens for a specific user.
     *
     * @param user the user to find tokens for
     * @return a list of refresh tokens for the user
     */
    List<RefreshToken> findByUser(User user);

    /**
     * Delete all refresh tokens for a specific user.
     *
     * @param user the user to delete tokens for
     */
    void deleteByUser(User user);

    /**
     * Delete all expired and revoked tokens.
     *
     * @param now the current time
     * @return the number of tokens deleted
     */
    @Modifying
    @Query("DELETE FROM RefreshToken t WHERE t.expiryDate < ?1 OR t.revoked = true")
    int deleteAllExpiredOrRevoked(LocalDateTime now);
}

package com.userservice.controller;

import com.userservice.model.client.response.AssetsResponse;
import com.userservice.model.response.UserResponse;
import com.userservice.service.UserService;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

/**
 * Asynchronous controller for user operations that might take longer to complete.
 * Uses time limiters for fault tolerance.
 */
@RestController
@RequestMapping("/async/users")
@AllArgsConstructor
@Slf4j
public class AsyncUserController {

    private final UserService userService;

    /**
     * Retrieves user assets asynchronously with a time limiter.
     *
     * @param userId the userId ID
     * @return a CompletableFuture with the ResponseEntity containing assets
     */
    @GetMapping("/{id}/assets")
    @TimeLimiter(name = "default")
    public CompletableFuture<ResponseEntity<AssetsResponse>> getUserAssetsAsync(
            @PathVariable("id") Integer userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("Async retrieving assets for user ID: {}", userId);
                AssetsResponse userAssets = userService.getUserAssets(userId);
                return ResponseEntity.ok(userAssets);
            } catch (Exception e) {
                log.error("Error retrieving user assets", e);
                throw e;
            }
        });
    }

    /**
     * Retrieves all users asynchronously with a time limiter.
     *
     * @return a CompletableFuture with the page of users
     */
    @GetMapping
    @TimeLimiter(name = "default")
    public CompletableFuture<UserResponse> getUsers() {
        return CompletableFuture.supplyAsync(() -> {
            log.debug("Async retrieving all users");
            return userService.findAll();
        });
    }
}

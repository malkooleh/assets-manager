package com.userservice.service.impl;

import com.userservice.client.AssetClient;
import com.userservice.exception.UserNotFoundException;
import com.userservice.model.client.response.AssetsResponse;
import com.userservice.model.dto.UserDto;
import com.userservice.model.mapper.UserMapper;
import com.userservice.model.response.UserResponse;
import com.userservice.repository.UserRepository;
import com.userservice.service.UserService;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@AllArgsConstructor
@Slf4j

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AssetClient assetClient;

    @Override
    public void addUser(UserDto userDto) {
        var user = UserMapper.INSTANCE.userDtoToUser(userDto);
        userRepository.save(user);
    }

    @Override
    @CircuitBreaker(name = "default")
    @Retry(name = "default")
    public UserDto findByName(String name) {
        log.debug("Find user by name: {}", name);

        var user = userRepository.findByName(name);
        return UserMapper.INSTANCE.userToUserDto(user);
    }

    @Override
    public UserDto findById(Integer userId) {
        return userRepository.findById(userId)
                .map(UserMapper.INSTANCE::userToUserDto)
                .orElse(null);
    }

    @Override
    public UserResponse findAll() {
        log.debug("Retrieving all users");
        return new UserResponse(userRepository.findAll().stream()
                .map(UserMapper.INSTANCE::userToUserDto)
                .toList());
    }

    @Override
    @CircuitBreaker(name = "default")
    @Retry(name = "default")
    public void delete(Integer userId) {
        userRepository.deleteById(userId);
    }

    @Override
    @CircuitBreaker(name = "default")
    @Retry(name = "default")
    @Bulkhead(name = "default")
    public AssetsResponse getUserAssets(Integer userId) {
        log.debug("Retrieving assets for user ID: {}", userId);
        userRepository.findById(userId)
                .map(UserMapper.INSTANCE::userToUserDto)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return assetClient.getUserAssets(userId);
    }

    /**
     * Fallback method for getUserAssets in case of failure.
     * Returns an empty response when the primary method fails.
     *
     * @param userId the user ID
     * @param ex     the exception that triggered the fallback
     * @return an empty assets response
     */
    private AssetsResponse getUserAssetsFallback(Integer userId, Exception ex) {
        log.error("Circuit breaker triggered for getUserAssets method with user ID: {}", userId, ex);
        return new AssetsResponse(Collections.emptyList());
    }
}

package com.userservice.model.dto;

import java.time.LocalDateTime;

public record UserDto(
        Integer userId,
        String name,
        String email,
        LocalDateTime created
) {}

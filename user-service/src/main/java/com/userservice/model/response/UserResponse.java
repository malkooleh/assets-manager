package com.userservice.model.response;

import com.userservice.model.dto.UserDto;

import java.util.List;

public record UserResponse (
        List<UserDto> users
){}

package com.userservice.service;

import com.userservice.model.client.response.AssetsResponse;
import com.userservice.model.dto.UserDto;
import com.userservice.model.response.UserResponse;

public interface UserService {

    void addUser(UserDto userDto);

    UserDto findByName(String name);

    UserDto findById(Integer userId);

    UserResponse findAll();

    void delete(Integer userId);

    AssetsResponse getUserAssets(Integer userId);
}

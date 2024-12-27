package com.userservice.service.impl;

import com.userservice.client.AssetClient;
import com.userservice.model.client.response.AssetsResponse;
import com.userservice.model.dto.UserDto;
import com.userservice.model.mapper.UserMapper;
import com.userservice.model.response.UserResponse;
import com.userservice.repository.UserRepository;
import com.userservice.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor

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
    public UserDto findByName(String name) {
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
        return new UserResponse(userRepository.findAll().stream()
                .map(UserMapper.INSTANCE::userToUserDto)
                .toList());
    }

    @Override
    public void delete(Integer userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public AssetsResponse getUserAssets(Integer userId) {
        return assetClient.getUserAssets(userId);
    }
}

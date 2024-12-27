package com.userservice.controller;

import com.userservice.model.client.response.AssetsResponse;
import com.userservice.model.dto.UserDto;
import com.userservice.model.response.UserResponse;
import com.userservice.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void saveUser(@RequestBody UserDto user) {
        userService.addUser(user);
    }

    @GetMapping
    public UserResponse getUsers() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public UserDto getUser(@PathVariable("id") Integer userId) {
        return userService.findById(userId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Integer userId) {
        UserDto user = userService.findById(userId);
        if (user != null) {
            userService.delete(userId);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/{id}/assets")
    public AssetsResponse getUserAssets(@PathVariable("id") Integer userId) {
        return userService.getUserAssets(userId);
    }
}

package com.userservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UserNotFoundException extends BaseRuntimeException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
    private static final String CODE = "USER_NOT_FOUND";

    public UserNotFoundException() {
        super("No user with supplied ID exists.", STATUS, CODE);
    }

    public UserNotFoundException(Integer userId) {
        super("User not found with ID: " + userId, STATUS, CODE);
    }
}

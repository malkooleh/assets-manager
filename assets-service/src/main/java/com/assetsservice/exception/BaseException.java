package com.assetsservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BaseException extends Exception {

    private final HttpStatus status;
    private final String code;

    public BaseException(String message, HttpStatus status, String code) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public BaseException(String message, HttpStatus status, String code, Exception cause) {
        super(message, cause);
        this.status = status;
        this.code = code;
    }
}

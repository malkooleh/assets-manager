package com.assetsservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BaseRuntimeException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public BaseRuntimeException(String message, HttpStatus status, String code) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public BaseRuntimeException(String message, HttpStatus status, String code, Exception cause) {
        super(message, cause);
        this.status = status;
        this.code = code;
    }
}

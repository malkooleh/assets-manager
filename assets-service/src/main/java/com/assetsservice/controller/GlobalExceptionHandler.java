package com.assetsservice.controller;

import com.assetsservice.exception.BaseException;
import com.assetsservice.exception.BaseRuntimeException;
import com.assetsservice.model.response.ErrorModel;
import com.assetsservice.model.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<Object> handleBase(BaseException e) {
        ErrorResponse errorResponse = buildErrorResponseFromBaseException(e);
        return new ResponseEntity<>(errorResponse, e.getStatus());
    }

    @ExceptionHandler(BaseRuntimeException.class)
    protected ResponseEntity<Object> handleBaseRuntime(BaseRuntimeException e) {
        ErrorResponse errorResponse = buildErrorResponseFromBaseRuntimeException(e);
        return new ResponseEntity<>(errorResponse, e.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> internalServerError(Exception e) {
        ErrorResponse errorResponse = new ErrorResponse(
                new ErrorModel(getRootCause(e).getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.name())
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Throwable getRootCause(Exception e) {
        Throwable rootCause = e;
        while (e.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = e.getCause();
        }
        return rootCause;
    }

    private ErrorResponse buildErrorResponseFromBaseException(BaseException e) {
        ErrorModel error = new ErrorModel(e.getMessage(), e.getCode());
        return new ErrorResponse(error);
    }

    private ErrorResponse buildErrorResponseFromBaseRuntimeException(BaseRuntimeException e) {
        ErrorModel error = new ErrorModel(e.getMessage(), e.getCode());
        return new ErrorResponse(error);
    }
}

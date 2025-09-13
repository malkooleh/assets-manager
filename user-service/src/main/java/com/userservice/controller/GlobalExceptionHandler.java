package com.userservice.controller;

import com.userservice.exception.BaseException;
import com.userservice.exception.BaseRuntimeException;
import com.userservice.model.response.ErrorModel;
import com.userservice.model.response.ErrorResponse;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.concurrent.TimeoutException;

/**
 * Global exception handler for service exceptions.
 * Handles resilience-related exceptions and returns appropriate responses.
 */
@Slf4j
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
                new ErrorModel(getRootCause(e).getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.name()));
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles circuit breaker related exceptions.
     *
     * @param ex      the exception
     * @param request the web request
     * @return an error response with service unavailable status
     */
    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ErrorResponse> handleCircuitBreakerException(
            CallNotPermittedException ex,
            WebRequest request
    ) {
        log.error("Circuit breaker exception: {}. Path: {}", ex.getMessage(), request.getDescription(false));

        ErrorResponse errorResponse = new ErrorResponse(
                new ErrorModel(
                        "The service is currently unavailable. Please try again later.",
                        HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase()));

        return new ResponseEntity<>(errorResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    /**
     * Handles rate limiter related exceptions.
     *
     * @param ex      the exception
     * @param request the web request
     * @return an error response with too many requests status
     */
    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<ErrorResponse> handleRateLimiterException(
            RequestNotPermitted ex,
            WebRequest request
    ) {
        log.error("Rate limiter exception: {}. Path: {}", ex.getMessage(), request.getDescription(false));

        ErrorResponse errorResponse = new ErrorResponse(
                new ErrorModel(
                        "The request limit has been exceeded. Please try again later.",
                        HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase()));

        return new ResponseEntity<>(errorResponse, HttpStatus.TOO_MANY_REQUESTS);
    }

    /**
     * Handles timeout related exceptions.
     *
     * @param ex      the exception
     * @param request the web request
     * @return an error response with gateway timeout status
     */
    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ErrorResponse> handleTimeoutException(
            TimeoutException ex,
            WebRequest request
    ) {
        log.error("Timeout exception: {}. Path: {}", ex.getMessage(), request.getDescription(false));

        ErrorResponse errorResponse = new ErrorResponse(
                new ErrorModel(
                        "The request timed out. Please try again later.",
                        HttpStatus.GATEWAY_TIMEOUT.getReasonPhrase()));

        return new ResponseEntity<>(errorResponse, HttpStatus.GATEWAY_TIMEOUT);
    }

    /**
     * Handles bulkhead full exceptions.
     *
     * @param ex      the exception
     * @param request the web request
     * @return an error response with service unavailable status
     */
    @ExceptionHandler(BulkheadFullException.class)
    public ResponseEntity<ErrorResponse> handleBulkheadFullException(
            BulkheadFullException ex,
            WebRequest request
    ) {
        log.error("Bulkhead full exception: {}. Path: {}", ex.getMessage(), request.getDescription(false));

        ErrorResponse errorResponse = new ErrorResponse(
                new ErrorModel(
                        "The service is currently overloaded. Please try again later.",
                        HttpStatus.BANDWIDTH_LIMIT_EXCEEDED.getReasonPhrase()));

        return new ResponseEntity<>(errorResponse, HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);
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

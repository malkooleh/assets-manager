package com.userservice.service;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

/**
 * A resilience service that provides methods to make service calls with fault tolerance patterns.
 * This service demonstrates how to use Resilience4j annotations for different patterns.
 */
@Component
public class ResilienceService {

    /**
     * Executes the provided supplier with circuit breaker and retry patterns.
     * Use this for synchronous calls that need fault tolerance.
     *
     * @param serviceName the name of the service being called (used for the circuit breaker instance)
     * @param supplier    the supplier function that makes the actual call
     * @param fallback    the fallback value to use if the call fails
     * @param <T>         the type of result
     * @return the result from the supplier or the fallback
     */
    @CircuitBreaker(name = "#serviceName", fallbackMethod = "fallbackValue")
    @Retry(name = "#serviceName")
    public <T> T executeWithFallback(String serviceName, Supplier<T> supplier, T fallback) {
        return supplier.get();
    }

    /**
     * Executes the provided supplier asynchronously with circuit breaker, time
     * limiter, and retry patterns.
     * Use this for asynchronous calls that need fault tolerance.
     *
     * @param serviceName the name of the service being called
     * @param supplier    the supplier function that makes the actual call
     * @param <T>         the type of result
     * @return a CompletionStage with the result
     */
    @CircuitBreaker(name = "default")
    @Retry(name = "default")
    @TimeLimiter(name = "default")
    public <T> CompletionStage<T> executeAsync(String serviceName, Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier);
    }

    /**
     * Executes the provided supplier with bulkhead pattern to limit concurrent
     * calls.
     * Use this for calls that should be limited in concurrency.
     *
     * @param serviceName the name of the service being called
     * @param supplier    the supplier function that makes the actual call
     * @param <T>         the type of result
     * @return the result from the supplier
     */
    @Bulkhead(name = "default")
    public <T> T executeWithBulkhead(String serviceName, Supplier<T> supplier) {
        return supplier.get();
    }

    /**
     * Fallback method for circuit breaker.
     *
     * @param serviceName the name of the service being called
     * @param supplier    the supplier function that makes the actual call
     * @param fallback    the fallback value to use
     * @param e           the exception that was thrown
     * @param <T>         the type of result
     * @return the fallback value
     */
    private <T> T fallbackValue(String serviceName, Supplier<T> supplier, T fallback, Exception e) {
        return fallback;
    }
}

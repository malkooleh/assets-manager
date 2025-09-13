package com.userservice.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom health indicator that monitors the state of circuit breakers.
 * This allows monitoring systems to check the health of the service's resilience mechanisms.
 */
@Component
@RequiredArgsConstructor
public class ResilienceHealthIndicator implements HealthIndicator {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        boolean isDown = false;

        // Get all circuit breakers and check their state
        for (CircuitBreaker circuitBreaker : circuitBreakerRegistry.getAllCircuitBreakers()) {
            String name = circuitBreaker.getName();
            CircuitBreaker.State state = circuitBreaker.getState();
            CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();

            Map<String, Object> circuitBreakerDetails = new HashMap<>();
            circuitBreakerDetails.put("state", state);
            circuitBreakerDetails.put("failureRate", metrics.getFailureRate());
            circuitBreakerDetails.put("slowCallRate", metrics.getSlowCallRate());
            circuitBreakerDetails.put("bufferedCalls", metrics.getNumberOfBufferedCalls());
            circuitBreakerDetails.put("failedCalls", metrics.getNumberOfFailedCalls());
            circuitBreakerDetails.put("slowCalls", metrics.getNumberOfSlowCalls());

            details.put(name, circuitBreakerDetails);

            // If any circuit breaker is open, the service is considered degraded
            if (state == CircuitBreaker.State.OPEN || state == CircuitBreaker.State.FORCED_OPEN) {
                isDown = true;
            }
        }

        if (isDown) {
            return Health.down().withDetails(details).build();
        } else {
            return Health.up().withDetails(details).build();
        }
    }
}

# Fault Tolerance and Circuit Breaking - Users Service

This document describes the fault tolerance and circuit breaking mechanisms implemented in the Users Service.

## Overview

The Users Service has been enhanced with resilience patterns using Spring Cloud Circuit Breaker with Resilience4j.
These patterns help the service to gracefully handle failures and prevent cascading failures across the system.

## Implemented Resilience Patterns

### 1. Circuit Breaker

Circuit breaker prevents a service from repeatedly trying to execute an operation that's likely to fail. Instead, it
breaks the circuit and quickly fails additional requests.

- **Default Configuration**:
    - Failure Rate Threshold: 50%
    - Wait Duration in Open State: 5 seconds
    - Minimum Number of Calls: 10
    - Sliding Window Size: 10
    - Half-Open Permitted Calls: 5

### 2. Retry

The retry pattern automatically retries a failed operation a specified number of times before giving up.

- **Default Configuration**:
    - Maximum Attempts: 3
    - Wait Duration: 500ms
    - Exponential Backoff: Enabled
    - Exponential Multiplier: 2

### 3. Bulkhead

Bulkhead limits the number of concurrent calls to a service, preventing one service from exhausting all resources.

- **Default Configuration**:
    - Maximum Concurrent Calls: 25
    - Maximum Wait Duration: 500ms

### 4. Time Limiter

Time limiter enforces a timeout on calls to external services or long-running operations.

- **Default Configuration**:
    - Timeout Duration: 4 seconds
    - Cancel Running Future: Enabled

### 5. Rate Limiter

Rate limiter restricts the number of calls to a service within a time period.

- **Default Configuration**:
    - Limit for Period: 100
    - Limit Refresh Period: 1 second
    - Timeout Duration: 3 seconds

## Monitoring

Resilience metrics and health information are exposed through Spring Boot Actuator. The following endpoints are
available:

- Health: `/actuator/health`
- Circuit Breakers: `/actuator/circuitbreakers`
- Circuit Breaker Events: `/actuator/circuitbreakerevents`
- Prometheus Metrics: `/actuator/prometheus`

## Health Indicators

A custom `ResilienceHealthIndicator` provides detailed health information about the circuit breakers in the service.
This information is included in the health endpoint response.

## Exception Handling

A `GlobalExceptionHandler` handles resilience-related exceptions and returns appropriate HTTP responses:

- Circuit Breaker Open: 503 Service Unavailable
- Rate Limit Exceeded: 429 Too Many Requests
- Timeout: 504 Gateway Timeout

## Configuration

The resilience configurations are defined in both Java configuration classes and the `application.yml` file. The
configuration can be adjusted based on the service's requirements and observed behavior.

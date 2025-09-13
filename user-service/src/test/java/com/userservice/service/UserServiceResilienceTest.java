package com.userservice.service;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.userservice.helper.MockAssetsApiHelper;
import com.userservice.model.client.response.AssetsResponse;
import com.userservice.model.db.User;
import com.userservice.model.dto.UserDto;
import com.userservice.repository.UserRepository;
import feign.FeignException;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpServerErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserServiceResilienceTest {

    @RegisterExtension
    private static final WireMockExtension MOCK_ASSETS_API = WireMockExtension.newInstance()
            .options(wireMockConfig().port(8082)) //should be obtained from the property spring.cloud.openfeign.client.config.assets-service.url
            .build();
    @Autowired
    private UserService userService;
    @MockitoBean
    private UserRepository userRepository;

    private User testUser;
    private static final Integer TEST_USER_ID = 123;
    private static final String USER_NAME = "Test User";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId(TEST_USER_ID)
                .name(USER_NAME)
                .email("test@example.com")
                .build();

        // Reset mocks
        Mockito.reset(userRepository);
        MOCK_ASSETS_API.resetRequests();
    }

    @Test
    void testCircuitBreakerOnFindByName() {
        // Configure repository to throw exceptions
        when(userRepository.findByName(USER_NAME))
                .thenThrow(HttpServerErrorException.InternalServerError.class);

        // Initial calls should trigger circuit breaker failures
        for (int i = 1; i <= 5; i++) {
            assertThrows(Exception.class, () -> userService.findByName(USER_NAME));
        }

        // After threshold reached, circuit should be open
        for (int i = 1; i <= 3; i++) {
            assertThrows(CallNotPermittedException.class, () -> userService.findByName(USER_NAME));
        }

        // Verify repository was called expected number of times
        verify(userRepository, times(5)).findByName(USER_NAME);
    }

    @Test
    void testRetryOnFindByUserName() {
        // Configure repository to throw exception first 2 times, then succeed
        when(userRepository.findByName(USER_NAME))
                .thenThrow(HttpServerErrorException.InternalServerError.class)
                .thenThrow(HttpServerErrorException.InternalServerError.class)
                .thenReturn(testUser);

        // Call should eventually succeed due to retry
        UserDto result = userService.findByName(USER_NAME);

        // Verify result is correct
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(USER_NAME);

        // Verify repository was called expected number of times (1 initial + 2 retries)
        verify(userRepository, times(3)).findByName(USER_NAME);
    }

    @Test
    @DisplayName("Should trigger circuit breaker when assets service returns errors")
    void shouldTriggerCircuitBreaker_whenAssetsServiceFails() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        MockAssetsApiHelper.mockInternalServerErrorGetAssets(MOCK_ASSETS_API, TEST_USER_ID);

        // Initial calls should trigger circuit breaker failures
        for (int i = 1; i <= 5; i++) {
            assertThrows(FeignException.InternalServerError.class, () -> userService.getUserAssets(TEST_USER_ID));
        }

        // After threshold reached, circuit should be open
        for (int i = 1; i <= 3; i++) {
            assertThrows(CallNotPermittedException.class, () -> userService.getUserAssets(TEST_USER_ID));
        }

        // Verify repository was called expected number of times
        MOCK_ASSETS_API.verify(5, WireMock.getRequestedFor(WireMock.urlEqualTo("/assets/users/" + TEST_USER_ID)));
    }

    @Test
    @DisplayName("Should recover after circuit breaker timeout")
    void shouldRecover_afterCircuitBreakerTimeout() throws Exception {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        MockAssetsApiHelper.mockInternalServerErrorGetAssets(MOCK_ASSETS_API, testUser.getUserId());

        // Make enough calls to trip the circuit breaker
        for (int i = 1; i <= 5; i++) {
            assertThrows(FeignException.InternalServerError.class, () -> userService.getUserAssets(TEST_USER_ID));
        }

        // Verify the circuit is open
        assertThrows(CallNotPermittedException.class, () -> userService.getUserAssets(TEST_USER_ID));

        // Reset the WireMock to return success
        MOCK_ASSETS_API.resetAll();
        MockAssetsApiHelper.mockSuccessfulGetAssets(MOCK_ASSETS_API, testUser.getUserId());

        // Wait for the circuit breaker to go into half-open state
        TimeUnit.SECONDS.sleep(6);

        // The first call after timeout should work (circuit goes to half-open and succeeds)
        AssetsResponse userAssets = userService.getUserAssets(TEST_USER_ID);

        // Verify the response
        assertThat(userAssets).isNotNull();
        assertThat(userAssets.assets()).isNotEmpty();
    }

    @Test
    @DisplayName("Should handle bulkhead rejection when too many concurrent requests")
    void shouldHandleBulkheadRejection_whenTooManyConcurrentRequests() throws Exception {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        MockAssetsApiHelper.mockSuccessfulGetAssets(MOCK_ASSETS_API, TEST_USER_ID);

        // Create an executor service for concurrent calls
        ExecutorService executor = Executors.newFixedThreadPool(20);

        // Setup to track exceptions
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        List<Exception> bulkheadExceptions = new ArrayList<>();

        // Make concurrent calls to trigger bulkhead
        for (int i = 0; i < 15; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    // Sleep briefly to ensure calls overlap
                    Thread.sleep(10);
                    userService.getUserAssets(TEST_USER_ID);
                } catch (Exception e) {
                    synchronized (bulkheadExceptions) {
                        bulkheadExceptions.add(e);
                    }
                }
            }, executor);
            futures.add(future);
        }

        // Wait for all calls to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(5, TimeUnit.SECONDS);

        // Shutdown executor
        executor.shutdown();

        // Verify that bulkhead rejected some calls
        assertThat(bulkheadExceptions).isNotEmpty();
        assertThat(bulkheadExceptions.stream()
                .anyMatch(e -> e.getCause() instanceof BulkheadFullException ||
                               e.getMessage().contains("Bulkhead"))).isTrue();
    }
}

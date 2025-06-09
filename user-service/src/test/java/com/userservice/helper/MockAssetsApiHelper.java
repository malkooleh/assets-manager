package com.userservice.helper;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class MockAssetsApiHelper {

    private static final String USER_ASSETS_URL = "/assets/users/%s";

    public static void mockSuccessfulGetAssets(WireMockExtension mockService, int userId) {
        mockService.stubFor(WireMock.get(String.format(USER_ASSETS_URL, userId))
                .willReturn(aResponse()
                        .withStatus(OK.value())
                        .withBodyFile("assets-service/get-user-assets.json")
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                ));
    }

    public static void mockInternalServerErrorGetAssets(WireMockExtension mockService, int userId) {
        mockService.stubFor(WireMock.get(String.format(USER_ASSETS_URL, userId))
                .willReturn(aResponse()
                        .withStatus(INTERNAL_SERVER_ERROR.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                ));
    }

    public static void mockDelayedGetAssets(WireMockExtension mockService, int userId, int delayInMs) {
        mockService.stubFor(WireMock.get(String.format(USER_ASSETS_URL, userId))
                .willReturn(aResponse()
                        .withStatus(OK.value())
                        .withFixedDelay(delayInMs) // Add a delay to trigger timeout
                        .withBodyFile("assets-service/get-user-assets.json")
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                ));
    }

    public static void mockEmptyResponseGetUserAssets(WireMockExtension mockService, Integer userId) {
        mockService.stubFor(WireMock.get(String.format(USER_ASSETS_URL, userId))
                .willReturn(aResponse()
                        .withStatus(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody("{\"assets\":[]}")));
    }
}

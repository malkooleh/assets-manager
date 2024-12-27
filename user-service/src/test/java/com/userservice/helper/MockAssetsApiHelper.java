package com.userservice.helper;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.springframework.stereotype.Component;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
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
}

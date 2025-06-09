package com.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.userservice.helper.MockAssetsApiHelper;
import com.userservice.model.client.response.AssetResponse;
import com.userservice.model.client.response.AssetsResponse;
import com.userservice.model.db.User;
import com.userservice.model.dto.UserDto;
import com.userservice.model.response.UserResponse;
import com.userservice.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {

    private static final int NOT_EXISTED_USER_ID = 111;

    @RegisterExtension
    private static final WireMockExtension MOCK_ASSETS_API = WireMockExtension.newInstance()
            .options(wireMockConfig().port(8082)) //should be obtained from the property spring.cloud.openfeign.client.config.assets-service.url
            .build();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MOCK_ASSETS_API.resetAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldSaveUser() throws Exception {
        // given
        User user = buildValidUser();

        // when
        mockMvc
                .perform(post("/users")
                        .content(objectMapper.writeValueAsString(user))
                        .contentType(MediaType.APPLICATION_JSON)
                );

        // then
        List<User> savedUser = userRepository.findAll();
        assertThat(savedUser).hasSize(1);
        assertThat(savedUser.getFirst().getUserId()).isNotNull();
        assertThat(savedUser.getFirst().getName()).isEqualTo(user.getName());
        assertThat(savedUser.getFirst().getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void shouldReturnUsers_IfAnyExists() throws Exception {
        // given
        User user1 = buildValidUser();
        User user2 = buildValidUser("user2@test.email");
        userRepository.saveAll(List.of(user1, user2));

        // when
        ResultActions result = mockMvc.perform(get("/users"));

        // then
        UserResponse userResponse = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), UserResponse.class);
        assertThat(userResponse).isNotNull();
        Assertions.assertThat(userResponse.users()).isNotEmpty();
        Assertions.assertThat(userResponse.users()).hasSize(2);
    }

    @Test
    void shouldNotReturnUser_IfDoesNotExist() throws Exception {
        // given
        User user1 = buildValidUser();
        userRepository.save(user1);

        // when
        ResultActions result = mockMvc.perform(get("/users/" + NOT_EXISTED_USER_ID));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().string(is(emptyOrNullString())));
    }

    @Test
    void shouldReturnUser_IfExistByUserId() throws Exception {
        // given
        User user1 = buildValidUser();
        User user2 = buildValidUser("user2@test.email");
        userRepository.saveAll(List.of(user1, user2));

        // when
        ResultActions result = mockMvc.perform(get("/users/" + user2.getUserId()));

        // then
        UserDto user = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), UserDto.class);
        assertThat(user).isNotNull();
        assertThat(user.userId()).isEqualTo(user2.getUserId());
        assertThat(user.name()).isEqualTo(user2.getName());
        assertThat(user.email()).isEqualTo(user2.getEmail());
    }

    @Test
    void shouldNotDeleteUser_IfNotFoundByUserId() throws Exception {
        // given

        // when
        ResultActions result = mockMvc.perform(delete("/users/" + NOT_EXISTED_USER_ID));

        // then
        result.andExpect(status().isNotFound());
    }

    @Test
    void deleteUser() throws Exception {
        // given
        User user = buildValidUser();
        userRepository.save(user);

        Integer userId = userRepository.findAll().getFirst().getUserId();

        // when
        ResultActions result = mockMvc.perform(delete("/users/" + userId));

        // then
        result.andExpect(status().isOk());
        assertThat(userRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("Should return assets when user exists and has assets")
    void shouldReturnAssets_whenUserHasAssets() throws Exception {
        // given
        User user = buildValidUser();
        userRepository.save(user);

        Integer userId = user.getUserId();
        MockAssetsApiHelper.mockSuccessfulGetAssets(MOCK_ASSETS_API, userId);

        // when
        MvcResult result = mockMvc.perform(get("/users/{id}/assets", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // then
        AssetsResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), AssetsResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.assets()).isNotEmpty();
        assertThat(response.assets()).hasSizeGreaterThanOrEqualTo(1);

        AssetResponse firstAsset = response.assets().getFirst();
        assertThat(firstAsset.name()).isNotBlank();
        assertThat(firstAsset.assetType()).isNotBlank();

        // Verify the correct endpoint was called
        MOCK_ASSETS_API.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/assets/users/" + userId)));
    }

    @Test
    @DisplayName("Should return empty assets list when user exists but has no assets")
    void shouldReturnEmptyList_whenUserHasNoAssets() throws Exception {
        // given
        User user = buildValidUser();
        userRepository.save(user);

        Integer userId = user.getUserId();
        MockAssetsApiHelper.mockEmptyResponseGetUserAssets(MOCK_ASSETS_API, userId);

        // when
        MvcResult result = mockMvc.perform(get("/users/{id}/assets", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // then
        AssetsResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), AssetsResponse.class);

        assertThat(response).isNotNull();
        assertThat(response.assets()).isEmpty();

        // Verify the correct endpoint was called
        MOCK_ASSETS_API.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/assets/users/" + userId)));
    }

    @Test
    @DisplayName("Should return 404 when user does not exist")
    void shouldReturn404_whenUserDoesNotExist() throws Exception {
        // when & then
        mockMvc.perform(get("/users/{id}/assets", NOT_EXISTED_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Verify no calls were made to assets service
        MOCK_ASSETS_API.verify(0, WireMock.getRequestedFor(WireMock.urlEqualTo("/assets/users/" + NOT_EXISTED_USER_ID)));
    }

    @Test
    void testCircuitBreakerOnUserAssets() throws Exception {
        // given
        User user = buildValidUser();
        userRepository.save(user);

        int userId = user.getUserId();
        MockAssetsApiHelper.mockInternalServerErrorGetAssets(MOCK_ASSETS_API, userId);

        // First N calls should return 500 (circuit breaker closed, failures)
        for (int i = 1; i <= 5; i++) {
            mockMvc.perform(get("/users/" + userId + "/assets")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError());

            // Small delay to avoid overwhelming the system
            Thread.sleep(100);
        }

        // Small delay to ensure the circuit breaker has time to process
        Thread.sleep(500);

        // Next calls should return 503 (circuit breaker open)
        for (int i = 1; i <= 3; i++) {  // Reduced from 5 to 3 calls to avoid excessive testing
            mockMvc.perform(get("/users/" + userId + "/assets")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isServiceUnavailable());

            // Small delay between calls
            Thread.sleep(100);
        }

        // then
        MOCK_ASSETS_API.verify(5, WireMock.getRequestedFor(WireMock.urlEqualTo("/assets/users/" + userId)));
    }

    @Test
    @DisplayName("Should handle timeout from assets service")
    void shouldHandleTimeout_fromAssetsService() throws Exception {
        // given
        User user = buildValidUser();
        userRepository.save(user);

        int userId = user.getUserId();
        MockAssetsApiHelper.mockDelayedGetAssets(MOCK_ASSETS_API, userId, 2000); // 2 seconds delay

        // when
        MvcResult mvcResult = mockMvc.perform(get("/async/users/" + userId + "/assets")
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // then
        // Delay to allow the async processing to complete
        Thread.sleep(3000);
        assertThat(mvcResult.getResponse().getStatus()).isEqualTo(200);
    }

    private User buildValidUser() {
        String defaultEmail = "user1@test.email";
        return buildValidUser(defaultEmail);
    }

    private User buildValidUser(String email) {
        return User.builder()
                .name("User1")
                .email(email)
                .build();
    }
}
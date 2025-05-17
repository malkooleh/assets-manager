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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
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
class UserControllerTest {

    public static final int NOT_EXISTED_USER_ID = 111;
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
    void getUserAssets() throws Exception {
        // given
        User user = buildValidUser();
        userRepository.save(user);

        Integer userId = userRepository.findAll().getFirst().getUserId();
        MockAssetsApiHelper.mockSuccessfulGetAssets(MOCK_ASSETS_API, userId);

        // when
        ResultActions result = mockMvc.perform(get("/users/" + userId + "/assets"));

        // then
        MOCK_ASSETS_API.verify(1, WireMock.getRequestedFor(WireMock.urlEqualTo("/assets/users/" + userId)));

        AssetsResponse assetsResponse = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), AssetsResponse.class);
        assertThat(assetsResponse).isNotNull();
        assertThat(assetsResponse.assets()).isNotEmpty();
        AssetResponse asset = assetsResponse.assets().getFirst();
        assertThat(asset.name()).isEqualTo("device1");
        assertThat(asset.assetType()).isEqualTo("MONITOR");
        assertThat(asset.status()).isEqualTo("AVAILABLE");
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
package com.assetsservice.controller;

import com.assetsservice.model.db.Asset;
import com.assetsservice.model.dto.AssetDto;
import com.assetsservice.model.enumtype.AssetStatus;
import com.assetsservice.model.enumtype.AssetType;
import com.assetsservice.model.response.AssetHistoryResponse;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AssetControllerIntegrationTest extends BaseControllerIntegrationTest {

    private static final int NOT_EXISTED_ASSET_ID = 111;

    @Test
    void shouldCreateAsset() throws Exception {
        // given
        AssetDto assetDto = new AssetDto(
                null,
                "New Test Asset",
                AssetType.LAPTOP,
                AssetStatus.AVAILABLE,
                TEST_USER_ID,
                null
        );

        // when & then
        mockMvc.perform(post("/assets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assetDto)))
                .andExpect(status().isCreated());

        // verify asset was created
        List<Asset> assets = assetRepository.findAll();
        assertThat(assets).hasSize(1);
        assertThat(assets.getFirst().getName()).isEqualTo("New Test Asset");
        assertThat(assets.getFirst().getAssetType()).isEqualTo(AssetType.LAPTOP);
    }

    @Test
    void shouldGetAllAssets() throws Exception {
        // given
        createTestAsset("Asset 1", AssetType.LAPTOP, AssetStatus.AVAILABLE, TEST_USER_ID);
        createTestAsset("Asset 2", AssetType.MONITOR, AssetStatus.IN_USE, TEST_USER_ID);

        // when & then
        mockMvc.perform(get("/assets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].name", containsInAnyOrder("Asset 1", "Asset 2")))
                .andExpect(jsonPath("$.content[*].assetType", containsInAnyOrder("LAPTOP", "MONITOR")));
    }

    @Test
    void shouldReturnAssets_IfAnyExists_WhenDefaultPaginationIsApplied() throws Exception {
        // given
        Asset asset1 = createTestAsset("Asset 1", AssetType.LAPTOP, AssetStatus.AVAILABLE, TEST_USER_ID);
        Asset asset2 = createTestAsset("Asset 2", AssetType.MONITOR, AssetStatus.IN_USE, TEST_USER_ID);
        assetRepository.saveAll(List.of(asset1, asset2));

        // when
        ResultActions result = mockMvc.perform(get("/assets"));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.pageable.sort.unsorted", Matchers.equalTo(true)))
                .andExpect(jsonPath("$.pageable.offset", Matchers.equalTo(0)))
                .andExpect(jsonPath("$.pageable.pageSize", Matchers.equalTo(20)))
                .andExpect(jsonPath("$.pageable.pageNumber", Matchers.equalTo(0)))
                .andExpect(jsonPath("$.pageable.unpaged", Matchers.equalTo(false)))
                .andExpect(jsonPath("$.pageable.paged", Matchers.equalTo(true)))
                .andExpect(jsonPath("$.last", Matchers.equalTo(true)))
                .andExpect(jsonPath("$.totalPages", Matchers.equalTo(1)))
                .andExpect(jsonPath("$.totalElements", Matchers.equalTo(2)))
                .andExpect(jsonPath("$.number", Matchers.equalTo(0)))
                .andExpect(jsonPath("$.size", Matchers.equalTo(20)))
                .andExpect(jsonPath("$.numberOfElements", Matchers.equalTo(2)));
    }

    @Test
    void shouldReturnAssets_EvaluatesPageableParameter() throws Exception {
        // given
        List<Asset> assets = new ArrayList<>();
        IntStream.range(1, 10).forEach(value -> assets.add(createDefaultTestAsset()));
        assetRepository.saveAll(assets);

        // when
        ResultActions result = mockMvc.perform(get("/assets")
                        .param("page", "2")
                        .param("size", "4"))
                .andExpect(status().isOk());

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.pageable.offset", Matchers.equalTo(8)))
                .andExpect(jsonPath("$.pageable.pageSize", Matchers.equalTo(4)))
                .andExpect(jsonPath("$.pageable.pageNumber", Matchers.equalTo(2)))
                .andExpect(jsonPath("$.last", Matchers.equalTo(true)))
                .andExpect(jsonPath("$.totalPages", Matchers.equalTo(3)))
                .andExpect(jsonPath("$.totalElements", Matchers.equalTo(9)))
                .andExpect(jsonPath("$.numberOfElements", Matchers.equalTo(1)));
    }

    @Test
    void shouldNotReturnAsset_IfDoesNotExist() throws Exception {
        // given
        Asset asset = createDefaultTestAsset();
        assetRepository.save(asset);

        // when
        ResultActions result = mockMvc.perform(get("/assets/" + NOT_EXISTED_ASSET_ID));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().string(is(emptyOrNullString())));
    }

    @Test
    void shouldGetAssetById() throws Exception {
        // given
        Asset asset = createDefaultTestAsset();

        // when & then
        mockMvc.perform(get("/assets/{id}", asset.getAssetId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assetId", is(asset.getAssetId())))
                .andExpect(jsonPath("$.name", is("Test Asset")))
                .andExpect(jsonPath("$.assetType", is("LAPTOP")))
                .andExpect(jsonPath("$.status", is("AVAILABLE")));
    }

    @Test
    void shouldUpdateAsset() throws Exception {
        // given
        Asset asset = createDefaultTestAsset();

        AssetDto updateDto = new AssetDto(
                asset.getAssetId(),
                "Updated Asset",
                AssetType.MONITOR,
                AssetStatus.MAINTENANCE,
                TEST_USER_ID,
                null
        );

        // when & then
        mockMvc.perform(put("/assets", asset.getAssetId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        // verify asset was updated
        Asset updatedAsset = assetRepository.findById(asset.getAssetId()).orElseThrow();
        assertThat(updatedAsset.getName()).isEqualTo(updateDto.name());
        assertThat(updatedAsset.getAssetType()).isEqualTo(updateDto.assetType());
        assertThat(updatedAsset.getStatus()).isEqualTo(updateDto.status());
    }

    @Test
    void shouldNotDeleteAsset_IfNotFoundByAssetId() throws Exception {
        // given

        // when
        ResultActions result = mockMvc.perform(delete("/assets/{id}", NOT_EXISTED_ASSET_ID));

        // then
        result.andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteAsset() throws Exception {
        // given
        Asset asset = createDefaultTestAsset();

        // when & then
        mockMvc.perform(delete("/assets/{id}", asset.getAssetId()))
                .andExpect(status().isOk());

        // verify asset was deleted
        assertThat(assetRepository.findById(asset.getAssetId())).isEmpty();
    }

    @Test
    void shouldGetAssetsByUserId() throws Exception {
        // given
        createTestAsset("User Asset 1", AssetType.LAPTOP, AssetStatus.AVAILABLE, TEST_USER_ID);
        createTestAsset("User Asset 2", AssetType.MONITOR, AssetStatus.IN_USE, TEST_USER_ID);
        createTestAsset("Other User Asset", AssetType.HEADPHONES, AssetStatus.AVAILABLE, 789);

        // when & then
        mockMvc.perform(get("/assets/users/{userId}", TEST_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assets", hasSize(2)))
                .andExpect(jsonPath("$.assets[*].name", containsInAnyOrder("User Asset 1", "User Asset 2")))
                .andExpect(jsonPath("$.assets[*].userId", everyItem(is(TEST_USER_ID))));
    }

    @Test
    void shouldGetAssetsByStatus() throws Exception {
        // given
        createTestAsset("Available Asset 1", AssetType.LAPTOP, AssetStatus.AVAILABLE, TEST_USER_ID);
        createTestAsset("Available Asset 2", AssetType.MONITOR, AssetStatus.AVAILABLE, TEST_USER_ID);
        createTestAsset("In Use Asset", AssetType.HEADPHONES, AssetStatus.IN_USE, TEST_USER_ID);

        // when & then
        mockMvc.perform(get("/assets/status/{status}", AssetStatus.AVAILABLE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].name", containsInAnyOrder("Available Asset 1", "Available Asset 2")))
                .andExpect(jsonPath("$.content[*].status", everyItem(is("AVAILABLE"))));
    }

    @Test
    void shouldReturnAssetHistory() throws Exception {
        // given
        Asset asset = createDefaultTestAsset();

        // when & then
        MvcResult result = mockMvc.perform(get("/assets/{id}/history", asset.getAssetId()))
                .andExpect(status().isOk())
                .andReturn();

        // verify response structure
        AssetHistoryResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AssetHistoryResponse.class
        );
        assertThat(response).isNotNull();
        // Note: Actual history content depends on implementation details
    }

    @Test
    void shouldUpdateAssetStatus() throws Exception {
        // given
        Asset asset = createDefaultTestAsset();
        String notes = "Maintenance required";

        // when & then
        mockMvc.perform(put("/assets/{id}/status", asset.getAssetId())
                        .param("status", AssetStatus.MAINTENANCE.toString())
                        .param("notes", notes))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(AssetStatus.MAINTENANCE.toString())))
                .andExpect(jsonPath("$.notes", is(notes)));

        // verify asset status was updated
        Asset updatedAsset = assetRepository.findById(asset.getAssetId()).orElseThrow();
        assertThat(updatedAsset.getStatus()).isEqualTo(AssetStatus.MAINTENANCE);
        assertThat(updatedAsset.getNotes()).isEqualTo(notes);
    }

    @Test
    void shouldReturnAvailableAssets() throws Exception {
        // given
        createTestAsset("Available Laptop", AssetType.LAPTOP, AssetStatus.AVAILABLE, TEST_USER_ID);
        createTestAsset("In-Use Monitor", AssetType.MONITOR, AssetStatus.IN_USE, TEST_USER_ID);

        // when & then
        mockMvc.perform(get("/assets/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Available Laptop")))
                .andExpect(jsonPath("$.content[0].status", is(AssetStatus.AVAILABLE.toString())));
    }
}

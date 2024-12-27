package com.assetsservice.controller;

import com.assetsservice.model.db.Asset;
import com.assetsservice.repository.AssetRepository;
import com.assetsservice.model.dto.AssetDto;
import com.assetsservice.model.enumtype.AssetStatus;
import com.assetsservice.model.enumtype.AssetType;
import com.assetsservice.model.response.AssetsResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AssetControllerTest {

    public static final int NOT_EXISTED_ASSET_ID = 111;
    public static final int USER_ID = 111;
    public static final int USER_ID_WITHOUT_ASSETS = 123;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SpyBean
    private AssetRepository assetRepository;

    @BeforeEach
    public void setUp() {
        assetRepository.deleteAll();
    }

    @Test
    void shouldSaveAsset() throws Exception {
        // given
        Asset asset = buildAvailableAsset();

        // when
        mockMvc.perform(post("/assets")
                .content(objectMapper.writeValueAsString(asset))
                .contentType(MediaType.APPLICATION_JSON)
        );

        // then
        List<Asset> savedAsset = assetRepository.findAll();
        assertThat(savedAsset).hasSize(1);
        assertThat(savedAsset.get(0).getAssetId()).isNotNull();
        assertThat(savedAsset.get(0).getName()).isEqualTo(asset.getName());
        assertThat(savedAsset.get(0).getStatus()).isEqualTo(asset.getStatus());
        assertThat(savedAsset.get(0).getAssetType()).isEqualTo(asset.getAssetType());
    }

    @Test
    void shouldReturnAssets_IfAnyExists_WhenDefaultPaginationIsApplied() throws Exception {
        // given
        Asset asset1 = buildAvailableAsset();
        Asset asset2 = buildAvailableAsset();
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
        IntStream.range(1, 10).forEach(value -> assets.add(buildAvailableAsset()));
        assetRepository.saveAll(assets);

        // when
        ResultActions result = mockMvc.perform(get("/assets")
                        .param("page", "2")
                        .param("size", "4"))
                .andExpect(status().isOk());

        // then
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(assetRepository).findAll(pageableCaptor.capture());
        PageRequest pageable = (PageRequest) pageableCaptor.getValue();

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(4);

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
        Asset asset = buildAvailableAsset();
        assetRepository.save(asset);

        // when
        ResultActions result = mockMvc.perform(get("/assets/" + NOT_EXISTED_ASSET_ID));

        // then
        result.andExpect(status().isOk())
                .andExpect(content().string(is(emptyOrNullString())));
    }

    @Test
    void shouldReturnAsset_IfExistByAssetId() throws Exception {
        // given
        Asset asset1 = buildAvailableAsset();
        Asset asset2 = buildAvailableAsset();
        assetRepository.saveAll(List.of(asset1, asset2));

        // when
        ResultActions result = mockMvc.perform(get("/assets/" + asset2.getAssetId()));

        // then
        AssetDto asset = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), AssetDto.class);
        assertThat(asset).isNotNull();
        assertThat(asset.assetId()).isEqualTo(asset2.getAssetId());
        assertThat(asset.name()).isEqualTo(asset2.getName());
    }

    @Test
    void shouldNotDeleteAsset_IfNotFoundByAssetId() throws Exception {
        // given

        // when
        ResultActions result = mockMvc.perform(delete("/assets/" + NOT_EXISTED_ASSET_ID));

        // then
        result.andExpect(status().isNotFound());
    }

    @Test
    void deleteAsset() throws Exception {
        // given
        Asset asset = buildAvailableAsset();
        assetRepository.save(asset);

        Integer assetId = assetRepository.findAll().get(0).getAssetId();

        // when
        ResultActions result = mockMvc.perform(delete("/assets/" + assetId));

        // then
        result.andExpect(status().isOk());
        Assertions.assertThat(assetRepository.findAll()).isEmpty();
    }

    @Test
    void shouldReturnError_IfNotFoundAssetsByUserId() throws Exception {
        // given
        Asset asset = Asset.builder()
                .name("device1")
                .assetType(AssetType.LAPTOP)
                .status(AssetStatus.IN_USE)
                .userId(USER_ID)
                .build();
        assetRepository.save(asset);

        // when
        ResultActions result = mockMvc.perform(get("/assets/users/" + USER_ID_WITHOUT_ASSETS));

        // then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[*].code", hasItem("ASSET_NOT_FOUND")));
    }

    @Test
    void getAssetsBelongToUser() throws Exception {
        // given
        Asset asset = Asset.builder()
                .name("device1")
                .assetType(AssetType.LAPTOP)
                .status(AssetStatus.IN_USE)
                .userId(USER_ID)
                .build();
        assetRepository.save(asset);

        // when
        ResultActions result = mockMvc.perform(get("/assets/users/" + USER_ID));

        // then
        AssetsResponse assetsResponse = objectMapper.readValue(result.andReturn().getResponse().getContentAsString(), AssetsResponse.class);
        assertThat(assetsResponse).isNotNull();
        assertThat(assetsResponse.assets()).isNotEmpty();
        AssetDto assetDto = assetsResponse.assets().get(0);
        assertThat(assetDto.userId()).isEqualTo(asset.getUserId());
        assertThat(assetDto.name()).isEqualTo(asset.getName());
        assertThat(assetDto.assetType()).isEqualTo(asset.getAssetType());
    }

    private Asset buildAvailableAsset() {
        return Asset.builder()
                .name("device1")
                .assetType(AssetType.LAPTOP)
                .status(AssetStatus.AVAILABLE)
                .build();
    }
}
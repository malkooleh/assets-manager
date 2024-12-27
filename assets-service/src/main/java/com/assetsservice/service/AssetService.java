package com.assetsservice.service;

import com.assetsservice.exception.AssetNotFoundException;
import com.assetsservice.model.dto.AssetDto;
import com.assetsservice.model.response.AssetsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AssetService {

    void addAsset(AssetDto asset);

    AssetDto findByName(String name);

    AssetDto findById(Integer assetId);

    Page<AssetDto> findAll(Pageable pageable);

    void deleteById(Integer assetId);

    AssetsResponse findByUserId(Integer userId) throws AssetNotFoundException;

    void updateAsset(AssetDto asset) throws AssetNotFoundException;
}

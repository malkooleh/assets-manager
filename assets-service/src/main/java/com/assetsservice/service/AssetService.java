package com.assetsservice.service;

import com.assetsservice.exception.AssetNotFoundException;
import com.assetsservice.model.dto.AssetDto;
import com.assetsservice.model.enumtype.AssetStatus;
import com.assetsservice.model.response.AssetHistoryResponse;
import com.assetsservice.model.response.AssetsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AssetService {
    void addAsset(AssetDto assetDto);
    AssetDto findByName(String name);
    AssetDto findById(Integer assetId);
    Page<AssetDto> findAll(Pageable pageable);
    void deleteById(Integer assetId);
    AssetsResponse findByUserId(Integer userId);
    void updateAsset(AssetDto assetDto) throws AssetNotFoundException;

    /**
     * Retrieves the revision history for an asset
     *
     * @param assetId the ID of the asset
     * @return the history of changes made to the asset
     */
    AssetHistoryResponse getAssetHistory(Integer assetId);

    /**
     * Updates the status of an asset and optionally adds notes
     *
     * @param assetId the ID of the asset to update
     * @param status the new status for the asset
     * @param notes optional notes about the status change
     * @return the updated asset
     * @throws AssetNotFoundException if the asset is not found
     */
    AssetDto updateAssetStatus(Integer assetId, AssetStatus status, String notes) throws AssetNotFoundException;

    /**
     * Finds assets by their status
     *
     * @param status the status to filter by
     * @param pageable pagination information
     * @return a page of assets with the specified status
     */
    Page<AssetDto> findByStatus(AssetStatus status, Pageable pageable);
}
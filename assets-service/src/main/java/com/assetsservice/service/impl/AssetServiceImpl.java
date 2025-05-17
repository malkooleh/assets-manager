package com.assetsservice.service.impl;

import com.assetsservice.exception.AssetNotFoundException;
import com.assetsservice.model.db.Asset;
import com.assetsservice.model.dto.AssetDto;
import com.assetsservice.model.enumtype.AssetStatus;
import com.assetsservice.model.mapper.AssetMapper;
import com.assetsservice.model.response.AssetHistoryResponse;
import com.assetsservice.model.response.AssetsResponse;
import com.assetsservice.repository.AssetRepository;
import com.assetsservice.service.AssetService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;

    public AssetServiceImpl(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Override
    public void addAsset(AssetDto assetDto) {
        Asset entity = AssetMapper.INSTANCE.assetDtoToAsset(assetDto);
        assetRepository.save(entity);
    }

    @Override
    public AssetDto findByName(String name) {
        Asset asset = assetRepository.findByName(name);
        return AssetMapper.INSTANCE.assetToAssetDto(asset);
    }

    @Override
    public AssetDto findById(Integer assetId) {
        return assetRepository.findById(assetId)
                .map(AssetMapper.INSTANCE::assetToAssetDto)
                .orElse(null);
    }

    @Override
    public Page<AssetDto> findAll(Pageable pageable) {
        Page<Asset> assetPage = assetRepository.findAll(pageable);
        if (!assetPage.hasContent()) {
            return Page.empty();
        }

        return new PageImpl<>(assetPage.stream()
                .map(AssetMapper.INSTANCE::assetToAssetDto)
                .toList(), pageable, assetPage.getTotalElements());
    }

    @Override
    public void deleteById(Integer assetId) {
        assetRepository.deleteById(assetId);
    }

    @Override
    public AssetsResponse findByUserId(Integer userId) {
        List<Asset> assets = assetRepository.findByUserId(userId);

        if (assets.isEmpty()) {
            return new AssetsResponse(Collections.emptyList());
        }

        return new AssetsResponse(assets.stream().map(AssetMapper.INSTANCE::assetToAssetDto).toList());
    }

    @Override
    @Transactional
    public void updateAsset(AssetDto assetDto) throws AssetNotFoundException {
        if (assetRepository.findById(assetDto.assetId()).isPresent()) {
            addAsset(assetDto);
        } else {
            throw new AssetNotFoundException();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AssetHistoryResponse getAssetHistory(Integer assetId) {
        List<AssetDto> assetsHistory = Optional.ofNullable(assetRepository.findAssetsHistory(assetId))
                .orElse(Collections.emptyList());
        return new AssetHistoryResponse(assetsHistory);
    }

    @Override
    @Transactional
    public AssetDto updateAssetStatus(Integer assetId, AssetStatus status, String notes) throws AssetNotFoundException {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new AssetNotFoundException(assetId));

        asset.setStatus(status);
        if (notes != null) {
            asset.setNotes(notes);
        }

        // If the asset is marked as AVAILABLE, remove any user assignment
        if (status == AssetStatus.AVAILABLE) {
            asset.setUserId(null);
        }

        Asset updatedAsset = assetRepository.save(asset);
        return AssetMapper.INSTANCE.assetToAssetDto(updatedAsset);
    }

    @Override
    public Page<AssetDto> findByStatus(AssetStatus status, Pageable pageable) {
        return assetRepository.findByStatus(status, pageable)
                .map(AssetMapper.INSTANCE::assetToAssetDto);
    }
}
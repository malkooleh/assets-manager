package com.assetsservice.service.impl;

import com.assetsservice.exception.AssetNotFoundException;
import com.assetsservice.model.db.Asset;
import com.assetsservice.model.dto.AssetDto;
import com.assetsservice.model.mapper.AssetMapper;
import com.assetsservice.model.response.AssetsResponse;
import com.assetsservice.repository.AssetRepository;
import com.assetsservice.service.AssetService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor

@Service
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;

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

    public void deleteById(Integer assetId) {
        assetRepository.deleteById(assetId);
    }

    @Override
    public AssetsResponse findByUserId(Integer userId) throws AssetNotFoundException {
        Asset asset = Optional.ofNullable(assetRepository.findByUserId(userId))
                .orElseThrow(AssetNotFoundException::new);
        return new AssetsResponse(List.of(AssetMapper.INSTANCE.assetToAssetDto(asset)));
    }

    @Override
    public void updateAsset(AssetDto assetDto) throws AssetNotFoundException {
        if (assetRepository.findById(assetDto.assetId()).isPresent()) {
            addAsset(assetDto);
        } else {
            throw new AssetNotFoundException();
        }
    }
}

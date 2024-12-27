package com.assetsservice.model.mapper;

import com.assetsservice.model.db.Asset;
import com.assetsservice.model.dto.AssetDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface AssetMapper {
    AssetMapper INSTANCE = Mappers.getMapper(AssetMapper.class);

    AssetDto assetToAssetDto(Asset asset);

    Asset assetDtoToAsset(AssetDto userDto);
}
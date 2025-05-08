package com.assetsservice.model.mapper;

import com.assetsservice.model.db.Asset;
import com.assetsservice.model.db.AssetPhoto;
import com.assetsservice.model.dto.AssetPhotoDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AssetPhotoMapper {

    AssetPhotoMapper INSTANCE = Mappers.getMapper(AssetPhotoMapper.class);

    @Mapping(source = "asset.assetId", target = "assetId")
    AssetPhotoDto assetPhotoToDto(AssetPhoto photo);

    @Mapping(target = "asset", source = "assetId", qualifiedByName = "assetIdToAsset")
    AssetPhoto dtoToAssetPhoto(AssetPhotoDto dto);

    default Asset assetIdToAsset(Integer assetId) {
        if (assetId == null) {
            return null;
        }
        Asset asset = new Asset();
        asset.setAssetId(assetId);
        return asset;
    }
}

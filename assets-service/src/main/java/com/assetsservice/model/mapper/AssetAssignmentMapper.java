package com.assetsservice.model.mapper;

import com.assetsservice.model.db.Asset;
import com.assetsservice.model.db.AssetAssignment;
import com.assetsservice.model.dto.AssetAssignmentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AssetAssignmentMapper {

    AssetAssignmentMapper INSTANCE = Mappers.getMapper(AssetAssignmentMapper.class);

    @Mapping(source = "asset.assetId", target = "assetId")
    AssetAssignmentDto assetAssignmentToDto(AssetAssignment assignment);

    @Mapping(target = "asset", source = "assetId", qualifiedByName = "assetIdToAsset")
    AssetAssignment dtoToAssetAssignment(AssetAssignmentDto dto);

    default Asset assetIdToAsset(Integer assetId) {
        if (assetId == null) {
            return null;
        }
        Asset asset = new Asset();
        asset.setAssetId(assetId);
        return asset;
    }
}

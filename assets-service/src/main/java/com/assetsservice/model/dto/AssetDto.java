package com.assetsservice.model.dto;

import com.assetsservice.model.enumtype.AssetStatus;
import com.assetsservice.model.enumtype.AssetType;
import jakarta.validation.constraints.NotNull;

public record AssetDto(
        Integer assetId,
        @NotNull String name,
        @NotNull AssetType assetType,
        @NotNull AssetStatus status,
        Integer userId
) {}

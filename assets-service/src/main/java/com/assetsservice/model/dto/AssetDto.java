package com.assetsservice.model.dto;

import com.assetsservice.model.enumtype.AssetStatus;
import com.assetsservice.model.enumtype.AssetType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AssetDto(
        Integer assetId,
        @NotNull String name,
        @NotNull AssetType assetType,
        @NotNull AssetStatus status,
        Integer userId,
        LocalDateTime created,
        LocalDateTime lastModified,
        String createdBy,
        String modifiedBy,
        String notes
) {}

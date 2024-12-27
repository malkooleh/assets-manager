package com.userservice.model.client.response;

public record AssetResponse(
        Integer assetId,
        String name,
        String assetType,
        String status,
        Integer userId
) {}

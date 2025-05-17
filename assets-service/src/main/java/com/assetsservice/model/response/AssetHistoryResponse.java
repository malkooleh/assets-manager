package com.assetsservice.model.response;

import com.assetsservice.model.dto.AssetDto;

import java.util.List;

/**
 * Response object for asset history data
 */
public record AssetHistoryResponse(
        List<AssetDto> assets
) {
}
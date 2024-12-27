package com.assetsservice.model.response;

import com.assetsservice.model.dto.AssetDto;

import java.util.List;

public record AssetsResponse(
        List<AssetDto> assets
) {}

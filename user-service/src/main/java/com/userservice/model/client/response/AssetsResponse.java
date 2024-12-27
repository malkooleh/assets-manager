package com.userservice.model.client.response;

import java.util.List;

public record AssetsResponse (
        List<AssetResponse> assets
) {}

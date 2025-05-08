package com.assetsservice.model.dto;

import java.time.LocalDateTime;

public record AssetPhotoDto(
        Integer photoId,
        Integer assetId,
        String fileName,
        String contentType,
        Long fileSize,
        String filePath,
        String description,
        LocalDateTime uploadDate
) {}

package com.assetsservice.exception;

import org.springframework.http.HttpStatus;

public class AssetPhotoNotFoundException extends BaseRuntimeException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
    private static final String CODE = "ASSET_PHOTO_NOT_FOUND";

    public AssetPhotoNotFoundException() {
        super("Asset photo not found", STATUS, CODE);
    }

    public AssetPhotoNotFoundException(Integer photoId) {
        super("Photo not found with ID: " + photoId, STATUS, CODE);
    }
}

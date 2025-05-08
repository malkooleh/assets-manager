package com.assetsservice.exception;

public class AssetPhotoNotFoundException extends RuntimeException {

    public AssetPhotoNotFoundException() {
        super("Asset photo not found");
    }

    public AssetPhotoNotFoundException(String message) {
        super(message);
    }
}

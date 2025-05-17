package com.assetsservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AssetNotFoundException extends BaseException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
    private static final String CODE = "ASSET_NOT_FOUND";

    public AssetNotFoundException() {
        super("No asset with supplied user ID exists.", STATUS, CODE);
    }

    public AssetNotFoundException(Integer assertId) {
        super("Asset not found with ID: " + assertId, STATUS, CODE);
    }
}

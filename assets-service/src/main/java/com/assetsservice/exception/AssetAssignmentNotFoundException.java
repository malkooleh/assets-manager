package com.assetsservice.exception;

public class AssetAssignmentNotFoundException extends RuntimeException {
    
    public AssetAssignmentNotFoundException() {
        super("Asset assignment not found");
    }
    
    public AssetAssignmentNotFoundException(String message) {
        super(message);
    }
}

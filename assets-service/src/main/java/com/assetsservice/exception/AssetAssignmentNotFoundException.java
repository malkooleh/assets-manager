package com.assetsservice.exception;

import org.springframework.http.HttpStatus;

public class AssetAssignmentNotFoundException extends BaseRuntimeException {

    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
    private static final String CODE = "ASSET_ASSIGNMENT_NOT_FOUND";

    public AssetAssignmentNotFoundException() {
        super("Asset assignment not found", STATUS, CODE);
    }

    public AssetAssignmentNotFoundException(Integer assignmentId) {
        super("Assignment not found with ID: " + assignmentId, STATUS, CODE);
    }
}

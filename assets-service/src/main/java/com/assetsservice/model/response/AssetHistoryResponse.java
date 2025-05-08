package com.assetsservice.model.response;

import java.util.Date;
import java.util.List;

/**
 * Response object for asset history data
 */
public record AssetHistoryResponse(
        List<AssetRevision> revisions
) {
    /**
     * Represents a single revision entry in the asset history
     */
    public record AssetRevision(
            Integer revisionId,
            Date timestamp,
            String username,
            String fieldName,
            String oldValue,
            String newValue
    ) {}
}
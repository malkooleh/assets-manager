package com.assetsservice.repository;

import com.assetsservice.model.db.Asset;
import com.assetsservice.model.dto.AssetDto;
import com.assetsservice.model.enumtype.AssetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Integer> {

    String ASSET_ID_SEQUENCE = "asset_id_sequence";

    Asset findByName(String name);

    List<Asset> findByUserId(Integer userId);

    /**
     * Find assets by their status
     *
     * @param status   the status to filter by
     * @param pageable pagination information
     * @return a page of assets with the specified status
     */
    Page<Asset> findByStatus(AssetStatus status, Pageable pageable);

    @Query(value = """
            SELECT au.asset_id as assetId,\
                   au.name,\
                   au.asset_type as assetType,\
                   au.status,\
                   au.created,\
                   au.user_id as userId \
            FROM asset_audit au \
               JOIN revision_audit ra ON au.revision_id = ra.revision_id \
            ORDER BY ra.timestamp DESC"""
            , nativeQuery = true)
    List<AssetDto> findAssetsHistory(Integer assetId);
}

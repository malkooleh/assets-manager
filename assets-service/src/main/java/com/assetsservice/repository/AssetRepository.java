package com.assetsservice.repository;

import com.assetsservice.model.db.Asset;
import com.assetsservice.model.enumtype.AssetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Integer> {

    String ASSET_ID_SEQUENCE = "asset_id_sequence";

    Asset findByName(String name);

    Asset findByUserId(Integer userId);
    
    /**
     * Find assets by their status
     * 
     * @param status the status to filter by
     * @param pageable pagination information
     * @return a page of assets with the specified status
     */
    Page<Asset> findByStatus(AssetStatus status, Pageable pageable);
}

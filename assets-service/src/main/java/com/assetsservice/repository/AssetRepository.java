package com.assetsservice.repository;

import com.assetsservice.model.db.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Integer> {

    String ASSET_ID_SEQUENCE = "asset_id_sequence";

    Asset findByName(String name);

    Asset findByUserId(Integer userId);
}

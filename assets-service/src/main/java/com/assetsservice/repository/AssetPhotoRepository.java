package com.assetsservice.repository;

import com.assetsservice.model.db.AssetPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetPhotoRepository extends JpaRepository<AssetPhoto, Integer> {

    String ASSET_PHOTO_ID_SEQUENCE = "asset_photo_id_sequence";
    
    List<AssetPhoto> findByAssetAssetId(Integer assetId);
    
    void deleteByAssetAssetId(Integer assetId);
}

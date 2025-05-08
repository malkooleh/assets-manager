package com.assetsservice.repository;

import com.assetsservice.model.db.AssetPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetPhotoRepository extends JpaRepository<AssetPhoto, Integer> {
    
    List<AssetPhoto> findByAssetAssetId(Integer assetId);
    
    void deleteByAssetAssetId(Integer assetId);
}

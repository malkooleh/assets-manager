package com.assetsservice.repository;

import com.assetsservice.model.db.AssetAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssetAssignmentRepository extends JpaRepository<AssetAssignment, Integer> {

    String ASSET_ASSIGNMENT_ID_SEQUENCE = "asset_assignment_id_sequence";

    List<AssetAssignment> findByEmployeeId(Integer employeeId);
    
    List<AssetAssignment> findByAssetAssetId(Integer assetId);
    
    void deleteByAssetAssetId(Integer assetId);
}

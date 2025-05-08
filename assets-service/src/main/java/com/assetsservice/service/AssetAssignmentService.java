package com.assetsservice.service;

import com.assetsservice.model.dto.AssetAssignmentDto;
import com.assetsservice.model.enumtype.AssignmentStatus;
import com.assetsservice.model.response.AssetAssignmentsResponse;

import java.util.List;

public interface AssetAssignmentService {

    AssetAssignmentDto assignAsset(AssetAssignmentDto assignmentDto);
    
    void removeAssignment(Integer assignmentId);
    
    AssetAssignmentDto updateAssignmentStatus(Integer assignmentId, AssignmentStatus status);
    
    AssetAssignmentsResponse findByEmployeeId(Integer employeeId);
    
    AssetAssignmentsResponse findByAssetId(Integer assetId);
    
    AssetAssignmentDto findById(Integer assignmentId);
    
    AssetAssignmentDto markAsLostOrBroken(Integer assignmentId, AssignmentStatus status, String notes);
}

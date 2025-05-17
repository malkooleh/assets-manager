package com.assetsservice.service.impl;

import com.assetsservice.exception.AssetAssignmentNotFoundException;
import com.assetsservice.exception.AssetNotFoundException;
import com.assetsservice.model.db.Asset;
import com.assetsservice.model.db.AssetAssignment;
import com.assetsservice.model.dto.AssetAssignmentDto;
import com.assetsservice.model.enumtype.AssetStatus;
import com.assetsservice.model.enumtype.AssignmentStatus;
import com.assetsservice.model.mapper.AssetAssignmentMapper;
import com.assetsservice.model.response.AssetAssignmentsResponse;
import com.assetsservice.repository.AssetAssignmentRepository;
import com.assetsservice.repository.AssetRepository;
import com.assetsservice.service.AssetAssignmentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AssetAssignmentServiceImpl implements AssetAssignmentService {

    private final AssetAssignmentRepository assignmentRepository;
    private final AssetRepository assetRepository;

    @Override
    @Transactional
    public AssetAssignmentDto assignAsset(AssetAssignmentDto assignmentDto) throws AssetNotFoundException {
        Asset asset = assetRepository.findById(assignmentDto.assetId())
                .orElseThrow(() -> new AssetNotFoundException(assignmentDto.assetId()));

        // Update asset status to IN_USE
        asset.setStatus(AssetStatus.IN_USE);
        asset.setUserId(assignmentDto.employeeId());
        assetRepository.save(asset);

        // Create assignment
        AssetAssignment assignment = AssetAssignmentMapper.INSTANCE.dtoToAssetAssignment(assignmentDto);
        assignment.setAsset(asset);
        assignment.setAssignmentDate(LocalDateTime.now());
        assignment.setStatus(AssignmentStatus.ACTIVE);

        AssetAssignment savedAssignment = assignmentRepository.save(assignment);
        return AssetAssignmentMapper.INSTANCE.assetAssignmentToDto(savedAssignment);
    }

    @Override
    @Transactional
    public void removeAssignment(Integer assignmentId) throws AssetAssignmentNotFoundException {
        AssetAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssetAssignmentNotFoundException(assignmentId));

        // Update asset status to AVAILABLE
        Asset asset = assignment.getAsset();
        asset.setStatus(AssetStatus.AVAILABLE);
        asset.setUserId(null);
        assetRepository.save(asset);

        // Update assignment status to RETURNED
        assignment.setStatus(AssignmentStatus.RETURNED);
        assignment.setReturnDate(LocalDateTime.now());
        assignmentRepository.save(assignment);
    }

    @Override
    @Transactional
    public AssetAssignmentDto updateAssignmentStatus(Integer assignmentId, AssignmentStatus status) throws AssetAssignmentNotFoundException {
        AssetAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssetAssignmentNotFoundException(assignmentId));

        assignment.setStatus(status);

        // If returning the asset
        if (status == AssignmentStatus.RETURNED) {
            assignment.setReturnDate(LocalDateTime.now());

            // Update asset status
            Asset asset = assignment.getAsset();
            asset.setStatus(AssetStatus.AVAILABLE);
            asset.setUserId(null);
            assetRepository.save(asset);
        }

        AssetAssignment updatedAssignment = assignmentRepository.save(assignment);
        return AssetAssignmentMapper.INSTANCE.assetAssignmentToDto(updatedAssignment);
    }

    @Override
    public AssetAssignmentsResponse findByEmployeeId(Integer employeeId) {
        List<AssetAssignment> assignments = assignmentRepository.findByEmployeeId(employeeId);
        List<AssetAssignmentDto> assignmentDtos = assignments.stream()
                .map(AssetAssignmentMapper.INSTANCE::assetAssignmentToDto)
                .collect(Collectors.toList());

        return new AssetAssignmentsResponse(assignmentDtos);
    }

    @Override
    public AssetAssignmentsResponse findByAssetId(Integer assetId) {
        List<AssetAssignment> assignments = assignmentRepository.findByAssetAssetId(assetId);
        List<AssetAssignmentDto> assignmentDtos = assignments.stream()
                .map(AssetAssignmentMapper.INSTANCE::assetAssignmentToDto)
                .collect(Collectors.toList());

        return new AssetAssignmentsResponse(assignmentDtos);
    }

    @Override
    public AssetAssignmentDto findById(Integer assignmentId) {
        AssetAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssetAssignmentNotFoundException(assignmentId));

        return AssetAssignmentMapper.INSTANCE.assetAssignmentToDto(assignment);
    }

    @Override
    @Transactional
    public AssetAssignmentDto markAsLostOrBroken(Integer assignmentId, AssignmentStatus status, String notes) {
        if (status != AssignmentStatus.LOST && status != AssignmentStatus.BROKEN) {
            throw new IllegalArgumentException("Status must be either LOST or BROKEN");
        }

        AssetAssignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new AssetAssignmentNotFoundException(assignmentId));

        assignment.setStatus(status);
        assignment.setNotes(notes);

        // Update asset status
        Asset asset = assignment.getAsset();
        asset.setStatus(status == AssignmentStatus.LOST ? AssetStatus.LOST : AssetStatus.BROKEN);
        assetRepository.save(asset);

        AssetAssignment updatedAssignment = assignmentRepository.save(assignment);
        return AssetAssignmentMapper.INSTANCE.assetAssignmentToDto(updatedAssignment);
    }
}

package com.assetsservice.controller;

import com.assetsservice.exception.AssetNotFoundException;
import com.assetsservice.model.dto.AssetAssignmentDto;
import com.assetsservice.model.enumtype.AssignmentStatus;
import com.assetsservice.model.response.AssetAssignmentsResponse;
import com.assetsservice.service.AssetAssignmentService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/asset-assignments")
@AllArgsConstructor
public class AssetAssignmentController {

    private final AssetAssignmentService assignmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssetAssignmentDto assignAsset(@RequestBody AssetAssignmentDto assignmentDto) throws AssetNotFoundException {
        return assignmentService.assignAsset(assignmentDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeAssignment(@PathVariable("id") Integer assignmentId) {
        assignmentService.removeAssignment(assignmentId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/status")
    public AssetAssignmentDto updateAssignmentStatus(
            @PathVariable("id") Integer assignmentId,
            @RequestParam AssignmentStatus status
    ) {
        return assignmentService.updateAssignmentStatus(assignmentId, status);
    }

    @GetMapping("/employee/{employeeId}")
    public AssetAssignmentsResponse getAssignmentsByEmployee(@PathVariable Integer employeeId) {
        return assignmentService.findByEmployeeId(employeeId);
    }

    @GetMapping("/asset/{assetId}")
    public AssetAssignmentsResponse getAssignmentsByAsset(@PathVariable Integer assetId) {
        return assignmentService.findByAssetId(assetId);
    }

    @GetMapping("/{id}")
    public AssetAssignmentDto getAssignment(@PathVariable("id") Integer assignmentId) {
        return assignmentService.findById(assignmentId);
    }

    @PutMapping("/{id}/mark")
    public AssetAssignmentDto markAsLostOrBroken(
            @PathVariable("id") Integer assignmentId,
            @RequestParam AssignmentStatus status,
            @RequestParam(required = false) String notes
    ) {
        return assignmentService.markAsLostOrBroken(assignmentId, status, notes);
    }
}

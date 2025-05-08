package com.assetsservice.model.dto;

import com.assetsservice.model.enumtype.AssignmentStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AssetAssignmentDto(
        Integer assignmentId,
        @NotNull Integer assetId,
        @NotNull Integer employeeId,
        @NotNull AssignmentStatus status,
        LocalDateTime assignmentDate,
        LocalDateTime returnDate,
        LocalDateTime lastModifiedDate,
        String notes
) {}

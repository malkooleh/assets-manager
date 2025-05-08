package com.assetsservice.model.response;

import com.assetsservice.model.dto.AssetAssignmentDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetAssignmentsResponse {
    private List<AssetAssignmentDto> assignments;
}

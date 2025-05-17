package com.assetsservice.controller;

import com.assetsservice.model.db.Asset;
import com.assetsservice.model.db.AssetAssignment;
import com.assetsservice.model.dto.AssetAssignmentDto;
import com.assetsservice.model.enumtype.AssetStatus;
import com.assetsservice.model.enumtype.AssetType;
import com.assetsservice.model.enumtype.AssignmentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AssetAssignmentControllerIntegrationTest extends BaseControllerIntegrationTest {

    @Test
    void shouldAssignAsset() throws Exception {
        // given
        Asset testAsset = createDefaultTestAsset();

        AssetAssignmentDto assignmentDto = new AssetAssignmentDto(
                null,
                testAsset.getAssetId(),
                TEST_EMPLOYEE_ID,
                AssignmentStatus.ACTIVE,
                null,
                null,
                null,
                "Initial assignment"
        );

        // when & then
        mockMvc.perform(post("/asset-assignments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(assignmentDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assetId", is(testAsset.getAssetId())))
                .andExpect(jsonPath("$.employeeId", is(TEST_EMPLOYEE_ID)))
                .andExpect(jsonPath("$.status", is(AssignmentStatus.ACTIVE.toString())))
                .andExpect(jsonPath("$.notes", is("Initial assignment")));

        // verify assignment was created
        assertThat(assignmentRepository.findAll()).hasSize(1);

        // verify asset status was updated to IN_USE
        Asset updatedAsset = assetRepository.findById(testAsset.getAssetId()).orElseThrow();
        assertThat(updatedAsset.getStatus()).isEqualTo(AssetStatus.IN_USE);
    }

    @Test
    void shouldMarkAssetAssignmentAsReturned_WhenRemoveAssignment() throws Exception {
        // given
        Asset testAsset = createDefaultTestAsset();
        AssetAssignment assignment = createTestAssignment(testAsset, AssignmentStatus.ACTIVE);

        // when & then
        mockMvc.perform(delete("/asset-assignments/{id}", assignment.getAssignmentId()))
                .andExpect(status().isOk());

        // verify assignment was returned
        AssetAssignment updatedAssignment = assignmentRepository.findById(assignment.getAssignmentId()).orElseThrow();
        assertThat(updatedAssignment.getStatus()).isEqualTo(AssignmentStatus.RETURNED);
    }

    @Test
    void shouldUpdateAssignmentStatus() throws Exception {
        // given
        Asset testAsset = createDefaultTestAsset();
        AssetAssignment assignment = createTestAssignment(testAsset, AssignmentStatus.ACTIVE);

        // when & then
        mockMvc.perform(put("/asset-assignments/{id}/status", assignment.getAssignmentId())
                        .param("status", AssignmentStatus.LOST.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(AssignmentStatus.LOST.toString())));

        // verify status was updated
        AssetAssignment updatedAssignment = assignmentRepository.findById(assignment.getAssignmentId()).orElseThrow();
        assertThat(updatedAssignment.getStatus()).isEqualTo(AssignmentStatus.LOST);
    }

    @Test
    void shouldGetAssignmentsByEmployee() throws Exception {
        // given
        Asset testAsset = createDefaultTestAsset();
        createTestAssignment(testAsset, AssignmentStatus.ACTIVE);
        createTestAssignment(testAsset, AssignmentStatus.RETURNED);

        // when & then
        mockMvc.perform(get("/asset-assignments/employee/{employeeId}", TEST_EMPLOYEE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignments", hasSize(2)))
                .andExpect(jsonPath("$.assignments[*].employeeId", everyItem(is(TEST_EMPLOYEE_ID))));
    }

    @Test
    void shouldGetAssignmentsByAsset() throws Exception {
        // given
        Asset testAsset = createDefaultTestAsset();
        createTestAssignment(testAsset, AssignmentStatus.ACTIVE);

        // Create another asset and assignment
        Asset anotherAsset = createTestAsset("Another Asset", AssetType.MONITOR, AssetStatus.AVAILABLE, TEST_USER_ID);

        AssetAssignment anotherAssignment = AssetAssignment.builder()
                .asset(anotherAsset)
                .employeeId(789) // different employee
                .status(AssignmentStatus.ACTIVE)
                .assignmentDate(LocalDateTime.now())
                .build();
        assignmentRepository.save(anotherAssignment);

        // when & then
        mockMvc.perform(get("/asset-assignments/asset/{assetId}", testAsset.getAssetId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignments", hasSize(1)))
                .andExpect(jsonPath("$.assignments[0].employeeId", is(TEST_EMPLOYEE_ID)));
    }

    @Test
    void shouldGetAssignmentById() throws Exception {
        // given
        Asset testAsset = createDefaultTestAsset();
        AssetAssignment assignment = createTestAssignment(testAsset, AssignmentStatus.ACTIVE);

        // when & then
        mockMvc.perform(get("/asset-assignments/{id}", assignment.getAssignmentId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignmentId", is(assignment.getAssignmentId())))
                .andExpect(jsonPath("$.employeeId", is(TEST_EMPLOYEE_ID)))
                .andExpect(jsonPath("$.status", is(AssignmentStatus.ACTIVE.toString())));
    }

    @Test
    void shouldMarkAsLostOrBroken() throws Exception {
        // given
        Asset testAsset = createDefaultTestAsset();
        AssetAssignment assignment = createTestAssignment(testAsset, AssignmentStatus.ACTIVE);
        String notes = "Device was dropped";

        // when & then
        mockMvc.perform(put("/asset-assignments/{id}/mark", assignment.getAssignmentId())
                        .param("status", AssignmentStatus.BROKEN.toString())
                        .param("notes", notes))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(AssignmentStatus.BROKEN.toString())))
                .andExpect(jsonPath("$.notes", is(notes)));

        // verify assignment was updated
        AssetAssignment updatedAssignment = assignmentRepository.findById(assignment.getAssignmentId()).orElseThrow();
        assertThat(updatedAssignment.getStatus()).isEqualTo(AssignmentStatus.BROKEN);
        assertThat(updatedAssignment.getNotes()).isEqualTo(notes);

        // verify asset status was updated to BROKEN
        Asset updatedAsset = assetRepository.findById(testAsset.getAssetId()).orElseThrow();
        assertThat(updatedAsset.getStatus()).isEqualTo(AssetStatus.BROKEN);
    }
}

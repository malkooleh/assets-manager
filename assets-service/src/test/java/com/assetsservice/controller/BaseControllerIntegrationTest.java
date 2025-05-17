package com.assetsservice.controller;

import com.assetsservice.model.db.Asset;
import com.assetsservice.model.db.AssetAssignment;
import com.assetsservice.model.db.AssetPhoto;
import com.assetsservice.model.enumtype.AssetStatus;
import com.assetsservice.model.enumtype.AssetType;
import com.assetsservice.model.enumtype.AssignmentStatus;
import com.assetsservice.repository.AssetAssignmentRepository;
import com.assetsservice.repository.AssetPhotoRepository;
import com.assetsservice.repository.AssetRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base class for controller integration tests providing common setup and utility methods.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class BaseControllerIntegrationTest {

    protected static final int TEST_USER_ID = 123;
    protected static final int TEST_EMPLOYEE_ID = 456;

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected AssetRepository assetRepository;

    @Autowired
    protected AssetAssignmentRepository assignmentRepository;

    @Autowired
    protected AssetPhotoRepository photoRepository;

    @BeforeEach
    void baseSetUp() {
        // Clean up repositories before each test
        photoRepository.deleteAll();
        assignmentRepository.deleteAll();
        assetRepository.deleteAll();
    }

    /**
     * Creates a test asset with default values.
     */
    protected Asset createDefaultTestAsset() {
        Asset asset = Asset.builder()
                .name("Test Asset")
                .assetType(AssetType.LAPTOP)
                .status(AssetStatus.AVAILABLE)
                .userId(TEST_USER_ID)
                .created(LocalDateTime.now())
                .build();
        return assetRepository.save(asset);
    }

    /**
     * Creates a test asset with the specified parameters.
     */
    protected Asset createTestAsset(
            String name,
            AssetType type,
            AssetStatus status,
            Integer userId
    ) {
        Asset asset = Asset.builder()
                .name(name)
                .assetType(type)
                .status(status)
                .userId(userId)
                .created(LocalDateTime.now())
                .build();
        return assetRepository.save(asset);
    }

    /**
     * Creates a test asset assignment with the asset and status.
     */
    protected AssetAssignment createTestAssignment(Asset asset, AssignmentStatus status) {
        AssetAssignment assignment = AssetAssignment.builder()
                .asset(asset)
                .employeeId(TEST_EMPLOYEE_ID)
                .status(status)
                .assignmentDate(LocalDateTime.now())
                .build();
        return assignmentRepository.save(assignment);
    }

    /**
     * Creates a test asset assignment with the given parameters.
     */
    protected AssetAssignment createTestAssignment(
            Asset asset,
            Integer employeeId,
            AssignmentStatus status,
            String notes
    ) {
        AssetAssignment assignment = AssetAssignment.builder()
                .asset(asset)
                .employeeId(employeeId)
                .status(status)
                .assignmentDate(LocalDateTime.now())
                .notes(notes)
                .build();

        return assignmentRepository.save(assignment);
    }

    /**
     * Creates a test asset photo with the given parameters and writes test content to disk.
     */
    protected AssetPhoto createTestPhoto(
            Asset asset,
            String uploadDir,
            byte[] content,
            String description
    ) throws Exception {

        // Create directory for asset if it doesn't exist
        String assetUploadDir = uploadDir + File.separator + asset.getAssetId();
        File assetDirectory = new File(assetUploadDir);
        if (!assetDirectory.exists()) {
            assetDirectory.mkdirs();
        }

        // Create a unique filename
        String fileName = "test-image-" + UUID.randomUUID() + ".jpg";
        String filePath = assetUploadDir + File.separator + fileName;

        // Write test content to file
        Files.write(Path.of(filePath), content);

        // Create and save photo metadata
        AssetPhoto photo = AssetPhoto.builder()
                .asset(asset)
                .fileName(fileName)
                .contentType(MediaType.IMAGE_JPEG_VALUE)
                .fileSize((long) content.length)
                .filePath(filePath)
                .description(description)
                .uploadDate(LocalDateTime.now())
                .build();

        return photoRepository.save(photo);
    }
}

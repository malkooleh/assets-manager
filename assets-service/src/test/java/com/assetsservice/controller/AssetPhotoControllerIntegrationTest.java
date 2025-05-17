package com.assetsservice.controller;

import com.assetsservice.model.db.Asset;
import com.assetsservice.model.db.AssetPhoto;
import com.assetsservice.model.dto.AssetPhotoDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MvcResult;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestPropertySource(properties = {
        "app.upload.dir=test-uploads"
})
class AssetPhotoControllerIntegrationTest extends BaseControllerIntegrationTest {

    private static final String TEST_DESCRIPTION = "Test photo description";
    private static final byte[] TEST_IMAGE_CONTENT = "Test image content".getBytes();

    @Value("${app.upload.dir:test-uploads}")
    private String uploadDir;

    @TempDir
    static Path tempDir;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public String uploadDir() {
            return tempDir.toString();
        }
    }

    @Override
    @BeforeEach
    void baseSetUp() {
        super.baseSetUp();

        // Create test directory if it doesn't exist
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    @Test
    void shouldUploadPhoto() throws Exception {
        // given
        Asset testAsset = createDefaultTestAsset();

        // Create asset-specific upload directory
        createAssetUploadDirectory(testAsset);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                TEST_IMAGE_CONTENT
        );

        // when & then
        MvcResult result = mockMvc.perform(multipart("/asset-photos/{assetId}", testAsset.getAssetId())
                        .file(file)
                        .param("description", TEST_DESCRIPTION))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assetId", is(testAsset.getAssetId())))
                .andExpect(jsonPath("$.fileName", containsString("test-image.jpg")))
                .andExpect(jsonPath("$.contentType", is(MediaType.IMAGE_JPEG_VALUE)))
                .andExpect(jsonPath("$.description", is(TEST_DESCRIPTION)))
                .andReturn();

        // verify photo was created in database
        AssetPhotoDto photoDto = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                AssetPhotoDto.class
        );

        assertThat(photoRepository.findById(photoDto.photoId())).isPresent();

        // verify file was saved to disk
        File savedFile = new File(photoDto.filePath());
        assertThat(savedFile).exists();
        assertThat(Files.readAllBytes(savedFile.toPath())).isEqualTo(TEST_IMAGE_CONTENT);
    }

    @Test
    void shouldGetPhotoContent() throws Exception {
        // given
        Asset testAsset = createDefaultTestAsset();
        AssetPhoto photo = createTestPhoto(testAsset);

        // when & then
        mockMvc.perform(get("/asset-photos/{photoId}/content", photo.getPhotoId()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString(photo.getFileName())))
                .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE))
                .andExpect(content().bytes(TEST_IMAGE_CONTENT));
    }

    @Test
    void shouldGetPhotosByAssetId() throws Exception {
        // given
        Asset testAsset = createDefaultTestAsset();
        createTestPhoto(testAsset);
        createTestPhoto(testAsset); // Create another photo for the same asset

        // when & then
        mockMvc.perform(get("/asset-photos/asset/{assetId}", testAsset.getAssetId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].assetId", everyItem(is(testAsset.getAssetId()))))
                .andExpect(jsonPath("$[*].description", everyItem(is(TEST_DESCRIPTION))));
    }

    @Test
    void shouldDeletePhoto() throws Exception {
        // given
        Asset testAsset = createDefaultTestAsset();
        AssetPhoto photo = createTestPhoto(testAsset);
        String filePath = photo.getFilePath();

        // when & then
        mockMvc.perform(delete("/asset-photos/{photoId}", photo.getPhotoId()))
                .andExpect(status().isOk());

        // verify photo was deleted from database
        assertThat(photoRepository.findById(photo.getPhotoId())).isEmpty();

        // verify file was deleted from disk
        File file = new File(filePath);
        assertThat(file).doesNotExist();
    }

    @Test
    void shouldGetPhotoById() throws Exception {
        // given
        Asset testAsset = createDefaultTestAsset();
        AssetPhoto photo = createTestPhoto(testAsset);

        // when & then
        mockMvc.perform(get("/asset-photos/{photoId}", photo.getPhotoId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.photoId", is(photo.getPhotoId())))
                .andExpect(jsonPath("$.assetId", is(testAsset.getAssetId())))
                .andExpect(jsonPath("$.fileName", is(photo.getFileName())))
                .andExpect(jsonPath("$.contentType", is(photo.getContentType())))
                .andExpect(jsonPath("$.description", is(photo.getDescription())));
    }

    private void createAssetUploadDirectory(Asset asset) {
        File assetDirectory = new File(uploadDir + File.separator + asset.getAssetId());
        if (!assetDirectory.exists()) {
            assetDirectory.mkdirs();
        }
    }

    private AssetPhoto createTestPhoto(Asset asset) throws Exception {
        // Create directory for asset if it doesn't exist
        createAssetUploadDirectory(asset);

        // Create a unique filename
        String fileName = "test-image-" + System.currentTimeMillis() + ".jpg";
        String filePath = uploadDir + File.separator + asset.getAssetId() + File.separator + fileName;

        // Write test content to file
        Files.write(Path.of(filePath), TEST_IMAGE_CONTENT);

        // Create and save photo metadata
        AssetPhoto photo = AssetPhoto.builder()
                .asset(asset)
                .fileName(fileName)
                .contentType(MediaType.IMAGE_JPEG_VALUE)
                .fileSize((long) TEST_IMAGE_CONTENT.length)
                .filePath(filePath)
                .description(TEST_DESCRIPTION)
                .uploadDate(LocalDateTime.now())
                .build();

        return photoRepository.save(photo);
    }
}

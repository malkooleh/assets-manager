package com.assetsservice.service.impl;

import com.assetsservice.exception.AssetNotFoundException;
import com.assetsservice.exception.AssetPhotoNotFoundException;
import com.assetsservice.model.db.Asset;
import com.assetsservice.model.db.AssetPhoto;
import com.assetsservice.model.dto.AssetPhotoDto;
import com.assetsservice.model.mapper.AssetPhotoMapper;
import com.assetsservice.repository.AssetPhotoRepository;
import com.assetsservice.repository.AssetRepository;
import com.assetsservice.service.AssetPhotoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class AssetPhotoServiceImpl implements AssetPhotoService {

    private final AssetPhotoRepository photoRepository;
    private final AssetRepository assetRepository;
    private final String uploadDir;

    public AssetPhotoServiceImpl(
            AssetPhotoRepository photoRepository,
            AssetRepository assetRepository,
            @Value("${app.upload.dir:uploads}") String uploadDir
    ) {
        this.photoRepository = photoRepository;
        this.assetRepository = assetRepository;
        this.uploadDir = uploadDir;

        // Create upload directory if it doesn't exist
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    @Override
    @Transactional
    public AssetPhotoDto uploadPhoto(Integer assetId, MultipartFile file, String description) throws IOException, AssetNotFoundException {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new AssetNotFoundException(assetId));

        // Generate unique filename
        String originalFilename = Optional.ofNullable(file.getOriginalFilename())
                .map(StringUtils::cleanPath)
                .orElse("%s-%s".formatted(assetId, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
        String fileExtension = "";
        if (originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID() + fileExtension;

        // Create directory for asset if it doesn't exist
        String assetUploadDir = uploadDir + File.separator + assetId;
        File assetDirectory = new File(assetUploadDir);
        if (!assetDirectory.exists()) {
            assetDirectory.mkdirs();
        }

        // Save file to disk
        Path targetLocation = Paths.get(assetUploadDir + File.separator + uniqueFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // Create and save photo metadata
        AssetPhoto photo = AssetPhoto.builder()
                .asset(asset)
                .fileName(originalFilename)
                .contentType(file.getContentType())
                .fileSize(file.getSize())
                .filePath(targetLocation.toString())
                .description(description)
                .uploadDate(LocalDateTime.now())
                .build();

        AssetPhoto savedPhoto = photoRepository.save(photo);
        return AssetPhotoMapper.INSTANCE.assetPhotoToDto(savedPhoto);
    }

    @Override
    public List<AssetPhotoDto> getPhotosByAssetId(Integer assetId) {
        List<AssetPhoto> photos = photoRepository.findByAssetAssetId(assetId);
        return photos.stream()
                .map(AssetPhotoMapper.INSTANCE::assetPhotoToDto)
                .toList();
    }

    @Override
    public AssetPhotoDto getPhotoById(Integer photoId) {
        AssetPhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new AssetPhotoNotFoundException(photoId));

        return AssetPhotoMapper.INSTANCE.assetPhotoToDto(photo);
    }

    @Override
    @Transactional
    public void deletePhoto(Integer photoId) {
        AssetPhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new AssetPhotoNotFoundException(photoId));

        // Delete file from disk
        try {
            Files.deleteIfExists(Paths.get(photo.getFilePath()));
        } catch (IOException e) {
            // Log error but continue with database deletion
            log.error("Failed to delete file: {}", photo.getFilePath());
        }

        photoRepository.deleteById(photoId);
    }

    @Override
    public byte[] getPhotoContent(Integer photoId) throws IOException {
        AssetPhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new AssetPhotoNotFoundException(photoId));

        Path filePath = Paths.get(photo.getFilePath());
        return Files.readAllBytes(filePath);
    }
}

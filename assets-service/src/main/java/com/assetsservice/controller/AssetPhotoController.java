package com.assetsservice.controller;

import com.assetsservice.exception.AssetNotFoundException;
import com.assetsservice.model.dto.AssetPhotoDto;
import com.assetsservice.service.AssetPhotoService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/asset-photos")
@AllArgsConstructor
public class AssetPhotoController {

    private final AssetPhotoService assetPhotoService;

    @PostMapping("/{assetId}")
    public ResponseEntity<AssetPhotoDto> uploadPhoto(
            @PathVariable Integer assetId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String description
    ) throws AssetNotFoundException, IOException {
        AssetPhotoDto photoDto = assetPhotoService.uploadPhoto(assetId, file, description);
        return ResponseEntity.status(HttpStatus.CREATED).body(photoDto);
    }

    @GetMapping("/{photoId}/content")
    public ResponseEntity<Resource> getPhotoContent(@PathVariable Integer photoId) throws IOException {
        AssetPhotoDto photoDto = assetPhotoService.getPhotoById(photoId);
        byte[] photoContent = assetPhotoService.getPhotoContent(photoId);

        ByteArrayResource resource = new ByteArrayResource(photoContent);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + photoDto.fileName() + "\"")
                .contentType(MediaType.parseMediaType(photoDto.contentType()))
                .body(resource);
    }

    @GetMapping("/asset/{assetId}")
    public ResponseEntity<List<AssetPhotoDto>> getPhotosByAssetId(@PathVariable Integer assetId) {
        List<AssetPhotoDto> photos = assetPhotoService.getPhotosByAssetId(assetId);
        return ResponseEntity.ok(photos);
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Integer photoId) {
        assetPhotoService.deletePhoto(photoId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{photoId}")
    public ResponseEntity<AssetPhotoDto> getPhotoById(@PathVariable Integer photoId) {
        AssetPhotoDto photoDto = assetPhotoService.getPhotoById(photoId);
        return ResponseEntity.ok(photoDto);
    }
}
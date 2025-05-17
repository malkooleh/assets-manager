package com.assetsservice.service;

import com.assetsservice.exception.AssetNotFoundException;
import com.assetsservice.model.dto.AssetPhotoDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AssetPhotoService {

    AssetPhotoDto uploadPhoto(Integer assetId, MultipartFile file, String description) throws IOException, AssetNotFoundException;
    
    List<AssetPhotoDto> getPhotosByAssetId(Integer assetId);
    
    AssetPhotoDto getPhotoById(Integer photoId);
    
    void deletePhoto(Integer photoId);
    
    byte[] getPhotoContent(Integer photoId) throws IOException;
}

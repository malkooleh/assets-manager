package com.assetsservice.controller;

import com.assetsservice.model.dto.AssetDto;
import com.assetsservice.model.response.AssetsResponse;
import com.assetsservice.service.AssetService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor

@RestController
@RequestMapping("/assets")
public class AssetController {

    private final AssetService assetService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void saveAsset(@RequestBody AssetDto asset) {
        assetService.addAsset(asset);
    }

    @GetMapping
    public Page<AssetDto> getAssets(@PageableDefault(page = 0, size = 20) Pageable pageable) {
        return assetService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public AssetDto getAsset(@PathVariable("id") Integer assetId) {
        return assetService.findById(assetId);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsset(@PathVariable("id") Integer assetId) {
        AssetDto asset = assetService.findById(assetId);
        if (asset != null) {
            assetService.deleteById(assetId);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping
    public void updateAsset(@RequestBody @Validated AssetDto asset) throws Exception {
        assetService.updateAsset(asset);
    }

    @GetMapping("/users/{userId}")
    public AssetsResponse getAssetsBelongToUser(@PathVariable("userId") Integer userId) throws Exception {
        return assetService.findByUserId(userId);
    }
}

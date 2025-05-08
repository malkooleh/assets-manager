package com.assetsservice.service.impl;

import com.assetsservice.exception.AssetNotFoundException;
import com.assetsservice.model.db.Asset;
import com.assetsservice.model.dto.AssetDto;
import com.assetsservice.model.enumtype.AssetStatus;
import com.assetsservice.model.mapper.AssetMapper;
import com.assetsservice.model.response.AssetHistoryResponse;
import com.assetsservice.model.response.AssetsResponse;
import com.assetsservice.repository.AssetRepository;
import com.assetsservice.service.AssetService;
import lombok.AllArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AssetServiceImpl implements AssetService {

    private final AssetRepository assetRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public AssetServiceImpl(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    @Override
    public void addAsset(AssetDto assetDto) {
        Asset entity = AssetMapper.INSTANCE.assetDtoToAsset(assetDto);
        assetRepository.save(entity);
    }

    @Override
    public AssetDto findByName(String name) {
        Asset asset = assetRepository.findByName(name);
        return AssetMapper.INSTANCE.assetToAssetDto(asset);
    }

    @Override
    public AssetDto findById(Integer assetId) {
        return assetRepository.findById(assetId)
                .map(AssetMapper.INSTANCE::assetToAssetDto)
                .orElse(null);
    }

    @Override
    public Page<AssetDto> findAll(Pageable pageable) {
        Page<Asset> assetPage = assetRepository.findAll(pageable);
        if (!assetPage.hasContent()) {
            return Page.empty();
        }

        return new PageImpl<>(assetPage.stream()
                .map(AssetMapper.INSTANCE::assetToAssetDto)
                .toList(), pageable, assetPage.getTotalElements());
    }

    @Override
    public void deleteById(Integer assetId) {
        assetRepository.deleteById(assetId);
    }

    @Override
    public AssetsResponse findByUserId(Integer userId) throws AssetNotFoundException {
        Asset asset = Optional.ofNullable(assetRepository.findByUserId(userId))
                .orElseThrow(AssetNotFoundException::new);
        return new AssetsResponse(List.of(AssetMapper.INSTANCE.assetToAssetDto(asset)));
    }

    @Override
    @Transactional
    public void updateAsset(AssetDto assetDto) throws AssetNotFoundException {
        if (assetRepository.findById(assetDto.assetId()).isPresent()) {
            addAsset(assetDto);
        } else {
            throw new AssetNotFoundException();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AssetHistoryResponse getAssetHistory(Integer assetId) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        List<Object[]> revisions = auditReader.createQuery()
                .forRevisionsOfEntity(Asset.class, false, true)
                .add(AuditEntity.id().eq(assetId))
                .getResultList();

        List<AssetHistoryResponse.AssetRevision> assetRevisions = new ArrayList<>();

        Asset previousAsset = null;

        for (Object[] revision : revisions) {
            Asset asset = (Asset) revision[0];
            com.assetsservice.model.db.RevisionAudit revisionEntity = (com.assetsservice.model.db.RevisionAudit) revision[1];
            RevisionType revisionType = (RevisionType) revision[2];

            if (previousAsset != null) {
                // Compare fields and create revisions for each changed field
                if (!previousAsset.getName().equals(asset.getName())) {
                    assetRevisions.add(createRevisionEntry(revisionEntity, "name", previousAsset.getName(), asset.getName()));
                }

                if (!previousAsset.getAssetType().equals(asset.getAssetType())) {
                    assetRevisions.add(createRevisionEntry(revisionEntity, "assetType", previousAsset.getAssetType().toString(), asset.getAssetType().toString()));
                }

                if (!previousAsset.getStatus().equals(asset.getStatus())) {
                    assetRevisions.add(createRevisionEntry(revisionEntity, "status", previousAsset.getStatus().toString(), asset.getStatus().toString()));
                }

                // Compare userId (which might be null)
                if ((previousAsset.getUserId() == null && asset.getUserId() != null) ||
                        (previousAsset.getUserId() != null && !previousAsset.getUserId().equals(asset.getUserId()))) {
                    assetRevisions.add(createRevisionEntry(revisionEntity, "userId",
                            previousAsset.getUserId() != null ? previousAsset.getUserId().toString() : "null",
                            asset.getUserId() != null ? asset.getUserId().toString() : "null"));
                }

                // Compare notes (which might be null)
                if ((previousAsset.getNotes() == null && asset.getNotes() != null) ||
                        (previousAsset.getNotes() != null && !previousAsset.getNotes().equals(asset.getNotes()))) {
                    assetRevisions.add(createRevisionEntry(revisionEntity, "notes",
                            previousAsset.getNotes() != null ? previousAsset.getNotes() : "null",
                            asset.getNotes() != null ? asset.getNotes() : "null"));
                }
            } else if (revisionType == RevisionType.ADD) {
                // For the first revision (creation), add all fields
                assetRevisions.add(createRevisionEntry(revisionEntity, "name", null, asset.getName()));
                assetRevisions.add(createRevisionEntry(revisionEntity, "assetType", null, asset.getAssetType().toString()));
                assetRevisions.add(createRevisionEntry(revisionEntity, "status", null, asset.getStatus().toString()));
                if (asset.getUserId() != null) {
                    assetRevisions.add(createRevisionEntry(revisionEntity, "userId", null, asset.getUserId().toString()));
                }
                if (asset.getNotes() != null) {
                    assetRevisions.add(createRevisionEntry(revisionEntity, "notes", null, asset.getNotes()));
                }
            }

            previousAsset = asset;
        }

        return new AssetHistoryResponse(assetRevisions);
    }

    private AssetHistoryResponse.AssetRevision createRevisionEntry(
            com.assetsservice.model.db.RevisionAudit revisionEntity,
            String fieldName,
            String oldValue,
            String newValue) {
        return new AssetHistoryResponse.AssetRevision(
                revisionEntity.getRevisionId(),
                revisionEntity.getTimestamp(),
                revisionEntity.getUsername(),
                fieldName,
                oldValue,
                newValue
        );
    }

    @Override
    @Transactional
    public AssetDto updateAssetStatus(Integer assetId, AssetStatus status, String notes) throws AssetNotFoundException {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new AssetNotFoundException("Asset not found with ID: " + assetId));

        asset.setStatus(status);
        if (notes != null) {
            asset.setNotes(notes);
        }

        // If the asset is marked as AVAILABLE, remove any user assignment
        if (status == AssetStatus.AVAILABLE) {
            asset.setUserId(null);
        }

        Asset updatedAsset = assetRepository.save(asset);
        return AssetMapper.INSTANCE.assetToAssetDto(updatedAsset);
    }

    @Override
    public Page<AssetDto> findByStatus(AssetStatus status, Pageable pageable) {
        return assetRepository.findByStatus(status, pageable)
                .map(AssetMapper.INSTANCE::assetToAssetDto);
    }
}
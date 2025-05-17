package com.assetsservice.model.db;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static com.assetsservice.repository.AssetPhotoRepository.ASSET_PHOTO_ID_SEQUENCE;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity
@Audited
@EntityListeners(AuditingEntityListener.class)
public class AssetPhoto {

    @Id
    @SequenceGenerator(name = ASSET_PHOTO_ID_SEQUENCE, sequenceName = ASSET_PHOTO_ID_SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ASSET_PHOTO_ID_SEQUENCE)
    private Integer photoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long fileSize;

    @Column(nullable = false)
    private String filePath;

    @Column
    private String description;

    @Column
    @CreatedDate
    private LocalDateTime uploadDate;
}

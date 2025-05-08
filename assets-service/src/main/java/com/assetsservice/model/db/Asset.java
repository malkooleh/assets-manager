package com.assetsservice.model.db;

import com.assetsservice.model.enumtype.AssetStatus;
import com.assetsservice.model.enumtype.AssetType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static com.assetsservice.repository.AssetRepository.ASSET_ID_SEQUENCE;
import static jakarta.persistence.EnumType.STRING;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity
@Audited
@EntityListeners(AuditingEntityListener.class)
public class Asset {

    @Id
    @SequenceGenerator(name = ASSET_ID_SEQUENCE, sequenceName = ASSET_ID_SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ASSET_ID_SEQUENCE)
    private Integer assetId;

    @Column
    @NotNull
    private String name;

    @Column
    @Enumerated(STRING)
    @NotNull
    private AssetType assetType;

    @Column
    @Enumerated(STRING)
    @NotNull
    private AssetStatus status;

    @Column
    @CreatedDate
    private LocalDateTime created;
    
    @Column
    @LastModifiedDate
    private LocalDateTime lastModified;
    
    @Column
    @CreatedBy
    private String createdBy;
    
    @Column
    @LastModifiedBy
    private String modifiedBy;

    @Column
    private Integer userId;
    
    @Column
    private String notes;
}

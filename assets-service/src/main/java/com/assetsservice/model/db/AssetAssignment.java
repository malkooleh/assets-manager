package com.assetsservice.model.db;

import com.assetsservice.model.enumtype.AssignmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static com.assetsservice.repository.AssetAssignmentRepository.ASSET_ASSIGNMENT_ID_SEQUENCE;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

@Entity
@Audited
@EntityListeners(AuditingEntityListener.class)
public class AssetAssignment {

    @Id
    @SequenceGenerator(name = ASSET_ASSIGNMENT_ID_SEQUENCE, sequenceName = ASSET_ASSIGNMENT_ID_SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ASSET_ASSIGNMENT_ID_SEQUENCE)
    private Integer assignmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(nullable = false)
    private Integer employeeId;

    @Column
    @NotNull
    @Enumerated(EnumType.STRING)
    private AssignmentStatus status;

    @Column
    @CreatedDate
    private LocalDateTime assignmentDate;

    @Column
    private LocalDateTime returnDate;

    @Column
    @LastModifiedDate
    private LocalDateTime lastModifiedDate;

    @Column
    private String notes;
}

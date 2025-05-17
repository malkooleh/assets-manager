package com.assetsservice.model.db;

import com.assetsservice.helper.RevisionAuditListener;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

import java.time.LocalDateTime;

import static jakarta.persistence.TemporalType.TIMESTAMP;

/**
 * Entity for storing revision information for Hibernate Envers.
 */
@Entity
@RevisionEntity(RevisionAuditListener.class)
@Getter
@Setter
public class RevisionAudit {

    private static final String REVISION_AUDIT_REVISION_ID_SEQUENCE = "revision_audit_revision_id_sequence";

    // This class defines the columns of the revision_audit table; this is used instead of the default
    // in order to use better column names, and the Date type for the timestamp.

    @Id
    @SequenceGenerator(name = REVISION_AUDIT_REVISION_ID_SEQUENCE, sequenceName = REVISION_AUDIT_REVISION_ID_SEQUENCE, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = REVISION_AUDIT_REVISION_ID_SEQUENCE)
    @RevisionNumber
    @Column(name = "revision_id")
    private Integer revisionId;

    @RevisionTimestamp
    @Temporal(TIMESTAMP)
    private LocalDateTime timestamp;
    
    @Column(name = "username", nullable = false)
    private String username;
}

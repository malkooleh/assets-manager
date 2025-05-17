CREATE SEQUENCE asset_photo_id_sequence;

CREATE TABLE asset_photo
(
    photo_id     INT DEFAULT nextval('asset_photo_id_sequence') PRIMARY KEY,
    asset_id     INT NOT NULL,
    file_name    VARCHAR(255) NOT NULL,
    content_type VARCHAR(255) NOT NULL,
    file_size    BIGINT NOT NULL,
    file_path    VARCHAR(255) NOT NULL,
    description  TEXT,
    upload_date  TIMESTAMP,
    CONSTRAINT asset_photo_asset_id_fk FOREIGN KEY (asset_id) REFERENCES asset (asset_id)
);

CREATE TABLE asset_photo_audit
(
    revision_id   INT NOT NULL,
    revision_type INT,
    photo_id      INT,
    asset_id      INT,
    file_name     VARCHAR(255),
    content_type  VARCHAR(255),
    file_size     BIGINT,
    file_path     VARCHAR(255),
    description   TEXT,
    upload_date   TIMESTAMP,
    CONSTRAINT asset_photo_audit_revision_id_fk FOREIGN KEY (revision_id) REFERENCES revision_audit (revision_id) ON DELETE CASCADE
);

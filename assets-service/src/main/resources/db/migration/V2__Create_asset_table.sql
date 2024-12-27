CREATE SEQUENCE asset_id_sequence;

CREATE TABLE asset
(
    asset_id     INT DEFAULT nextval('asset_id_sequence') PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    asset_type   TEXT         NOT NULL,
    status       TEXT         NOT NULL,
    created TIMESTAMP,
    user_id      INT
);

CREATE TABLE asset_audit
(
    revision_id   INT NOT NULL,
    revision_type INT,
    asset_id      INT,
    name          VARCHAR(255),
    asset_type    TEXT,
    status        TEXT,
    created  TIMESTAMP,
    user_id       INT,
    CONSTRAINT asset_audit_revision_id_fk FOREIGN KEY (revision_id) REFERENCES revision_audit (revision_id) ON DELETE CASCADE
);
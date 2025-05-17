CREATE SEQUENCE asset_assignment_id_sequence;

CREATE TABLE asset_assignment
(
    assignment_id      INT DEFAULT nextval('asset_assignment_id_sequence') PRIMARY KEY,
    asset_id           INT NOT NULL,
    employee_id        INT NOT NULL,
    status             TEXT NOT NULL,
    assignment_date    TIMESTAMP,
    return_date        TIMESTAMP,
    last_modified_date TIMESTAMP,
    notes              TEXT,
    CONSTRAINT asset_assignment_asset_id_fk FOREIGN KEY (asset_id) REFERENCES asset (asset_id)
);

CREATE TABLE asset_assignment_audit
(
    revision_id        INT NOT NULL,
    revision_type      INT,
    assignment_id      INT,
    asset_id           INT,
    employee_id        INT,
    status             TEXT,
    assignment_date    TIMESTAMP,
    return_date        TIMESTAMP,
    last_modified_date TIMESTAMP,
    notes              TEXT,
    CONSTRAINT asset_assignment_audit_revision_id_fk FOREIGN KEY (revision_id) REFERENCES revision_audit (revision_id) ON DELETE CASCADE
);

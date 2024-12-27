CREATE SEQUENCE revision_audit_revision_id_sequence;

CREATE TABLE revision_audit
(
    revision_id INT DEFAULT nextval('revision_audit_revision_id_sequence') PRIMARY KEY,
    timestamp   TIMESTAMP NOT NULL
);
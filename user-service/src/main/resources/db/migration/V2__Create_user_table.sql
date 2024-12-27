CREATE SEQUENCE user_id_sequence;

CREATE TABLE users
(
    user_id INT DEFAULT nextval('user_id_sequence') PRIMARY KEY,
    name    VARCHAR(255)        NOT NULL,
    email   VARCHAR(255) UNIQUE NOT NULL,
    created TIMESTAMP
);

CREATE TABLE users_audit
(
    revision_id   INT NOT NULL,
    revision_type INT,
    user_id       INT,
    name          VARCHAR(255),
    email         VARCHAR(255),
    created       TIMESTAMP,
    CONSTRAINT users_audit_revision_id_fk FOREIGN KEY (revision_id) REFERENCES revision_audit (revision_id) ON DELETE CASCADE
);

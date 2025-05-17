ALTER TABLE asset
ADD COLUMN last_modified TIMESTAMP;

ALTER TABLE asset
ADD COLUMN created_by VARCHAR(255);

ALTER TABLE asset
ADD COLUMN modified_by VARCHAR(255);

ALTER TABLE asset
ADD COLUMN notes TEXT;

ALTER TABLE asset_audit
ADD COLUMN last_modified TIMESTAMP;

ALTER TABLE asset_audit
ADD COLUMN created_by VARCHAR(255);

ALTER TABLE asset_audit
ADD COLUMN modified_by VARCHAR(255);

ALTER TABLE asset_audit
ADD COLUMN notes TEXT;

-- Add username column to revision_audit table
ALTER TABLE revision_audit
ADD COLUMN username VARCHAR(255) NOT NULL DEFAULT 'system';

--liquibase formatted sql

--changeset vyacheslav_borisov_15_05_2026:014-add-severity-to-incidents
ALTER TABLE incidents
    ADD COLUMN IF NOT EXISTS severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM';

--rollback ALTER TABLE incidents
--rollback     DROP COLUMN IF EXISTS severity;
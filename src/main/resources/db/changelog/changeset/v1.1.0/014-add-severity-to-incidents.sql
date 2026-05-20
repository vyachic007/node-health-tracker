--liquibase formatted sql

--changeset slava_borisov:014-add-severity-to-incidents
ALTER TABLE incidents
    ADD COLUMN severity VARCHAR(20) NOT NULL DEFAULT 'MEDIUM';

--rollback ALTER TABLE incidents
--rollback     DROP COLUMN severity;
--liquibase formatted sql

--changeset vyacheslav_borisov_16_05_2026:012_add_credentials_changed_at_to_users
ALTER TABLE users
    ADD COLUMN credentials_changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

--rollback ALTER TABLE users DROP COLUMN credentials_changed_at;
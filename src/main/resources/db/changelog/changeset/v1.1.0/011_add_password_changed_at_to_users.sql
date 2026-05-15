--liquibase formatted sql

--changeset vyacheslav_borisov_15_05_2026:011_add_password_changed_at_to_users
ALTER TABLE users
    ADD COLUMN password_changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

--rollback ALTER TABLE users DROP COLUMN password_changed_at;
--liquibase formatted sql

--changeset vyacheslav_borisov_03_05_2026:010_add_last_checked_at_to_network_services
ALTER TABLE network_services
    ADD COLUMN last_checked_at TIMESTAMP;

--rollback ALTER TABLE network_services DROP COLUMN last_checked_at;
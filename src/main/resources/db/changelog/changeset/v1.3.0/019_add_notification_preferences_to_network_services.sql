-- liquibase formatted sql

-- changeset vyacheslav_borisov_27_05_2026:019-add-notification-preferences-to-network-services

ALTER TABLE network_services
    ADD COLUMN notify_email BOOLEAN NOT NULL DEFAULT true;

ALTER TABLE network_services
    ADD COLUMN notify_telegram BOOLEAN NOT NULL DEFAULT true;

ALTER TABLE network_services
    ADD COLUMN notify_vk BOOLEAN NOT NULL DEFAULT true;

-- rollback ALTER TABLE network_services DROP COLUMN notify_vk;
-- rollback ALTER TABLE network_services DROP COLUMN notify_telegram;
-- rollback ALTER TABLE network_services DROP COLUMN notify_email;

-- liquibase formatted sql

-- changeset vyacheslav_borisov_25_05_2026:018-add-degradation-fields-to-network-services

ALTER TABLE network_services
    ADD COLUMN response_time_threshold_ms INTEGER NOT NULL DEFAULT 1000;

ALTER TABLE network_services
    ADD COLUMN degradation_threshold INTEGER NOT NULL DEFAULT 3;

ALTER TABLE network_services
    ADD COLUMN consecutive_degradations INTEGER NOT NULL DEFAULT 0;

-- rollback ALTER TABLE network_services DROP COLUMN consecutive_degradations;
-- rollback ALTER TABLE network_services DROP COLUMN degradation_threshold;
-- rollback ALTER TABLE network_services DROP COLUMN response_time_threshold_ms;
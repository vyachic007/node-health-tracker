-- liquibase formatted sql

-- changeset slava:018-add-degradation-fields-to-network-services
ALTER TABLE network_services
    ADD COLUMN response_time_threshold_ms INTEGER NOT NULL DEFAULT 1000,
    ADD COLUMN degradation_threshold INTEGER NOT NULL DEFAULT 3,
    ADD COLUMN consecutive_degradations INTEGER NOT NULL DEFAULT 0;

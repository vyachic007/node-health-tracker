--liquibase formatted sql

--changeset vyacheslav_borisov_15_05_2026:013-add-anti-flapping-fields-to-network-services
ALTER TABLE network_services
    ADD COLUMN IF NOT EXISTS failure_threshold INTEGER NOT NULL DEFAULT 2,
    ADD COLUMN IF NOT EXISTS recovery_threshold INTEGER NOT NULL DEFAULT 2,
    ADD COLUMN IF NOT EXISTS consecutive_failures INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS consecutive_successes INTEGER NOT NULL DEFAULT 0;

--rollback ALTER TABLE network_services
--rollback     DROP COLUMN IF EXISTS consecutive_successes,
--rollback     DROP COLUMN IF EXISTS consecutive_failures,
--rollback     DROP COLUMN IF EXISTS recovery_threshold,
--rollback     DROP COLUMN IF EXISTS failure_threshold;
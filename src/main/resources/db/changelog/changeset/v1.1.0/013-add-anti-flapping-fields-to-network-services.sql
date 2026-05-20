--liquibase formatted sql

--changeset slava_borisov:013-add-anti-flapping-fields-to-network-services
ALTER TABLE network_services
    ADD COLUMN failure_threshold INTEGER NOT NULL DEFAULT 2,
    ADD COLUMN recovery_threshold INTEGER NOT NULL DEFAULT 2,
    ADD COLUMN consecutive_failures INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN consecutive_successes INTEGER NOT NULL DEFAULT 0;

--rollback ALTER TABLE network_services
--rollback     DROP COLUMN consecutive_successes,
--rollback     DROP COLUMN consecutive_failures,
--rollback     DROP COLUMN recovery_threshold,
--rollback     DROP COLUMN failure_threshold;
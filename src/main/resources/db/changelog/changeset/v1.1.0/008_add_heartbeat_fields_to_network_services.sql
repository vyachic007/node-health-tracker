--liquibase formatted sql

--changeset vyacheslav_borisov_28_04_2026:13_add_heartbeat_fields_to_network_services
ALTER TABLE network_services
    ADD COLUMN heartbeat_token VARCHAR(255),
    ADD COLUMN last_heartbeat_at TIMESTAMP;

ALTER TABLE network_services
    ADD CONSTRAINT uq_network_services_heartbeat_token UNIQUE (heartbeat_token);

--rollback ALTER TABLE network_services DROP CONSTRAINT uq_network_services_heartbeat_token;
--rollback ALTER TABLE network_services DROP COLUMN last_heartbeat_at;
--rollback ALTER TABLE network_services DROP COLUMN heartbeat_token;
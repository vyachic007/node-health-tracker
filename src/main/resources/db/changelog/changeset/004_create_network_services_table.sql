--liquibase formatted sql

--changeset vyacheslav_borisov_04_04_2026:6_create_network_services_table
CREATE TABLE network_services
(
    id               BIGSERIAL PRIMARY KEY,
    node_id          BIGINT       NOT NULL,
    check_type_id    BIGINT       NOT NULL,
    name             VARCHAR(150) NOT NULL,
    target_host      VARCHAR(255) NOT NULL,
    port             INTEGER,
    path             VARCHAR(500),
    interval_seconds INTEGER      NOT NULL,
    is_enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (node_id, name),
    CHECK (LENGTH(TRIM(name)) > 0),
    CHECK (interval_seconds > 0),
    CHECK (port IS NULL OR port BETWEEN 1 AND 65535),
    FOREIGN KEY (node_id) REFERENCES network_nodes (id) ON DELETE CASCADE,
    FOREIGN KEY (check_type_id) REFERENCES check_types (id)
);

--rollback DROP TABLE network_services;
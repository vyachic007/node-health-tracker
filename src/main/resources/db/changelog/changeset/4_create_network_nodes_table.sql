--liquibase formatted sql

--changeset vyacheslav_borisov_04_04_2026:4_create_network_nodes_table
CREATE TABLE network_nodes
(
    id          BIGSERIAL PRIMARY KEY,
    owner_id    BIGINT       NOT NULL,
    name        VARCHAR(150) NOT NULL,
    host        VARCHAR(255) NOT NULL,
    description TEXT,
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (LENGTH(TRIM(name)) > 0),
    CHECK (LENGTH(TRIM(host)) > 0),
    FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE CASCADE
);

--rollback DROP TABLE network_nodes;
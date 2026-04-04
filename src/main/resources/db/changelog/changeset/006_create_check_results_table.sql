--liquibase formatted sql

--changeset vyacheslav_borisov_04_04_2026:8_create_check_results_table
CREATE TABLE check_results
(
    id               BIGSERIAL PRIMARY KEY,
    service_id       BIGINT    NOT NULL,
    status_id        BIGINT    NOT NULL,
    started_at       TIMESTAMP NOT NULL,
    finished_at      TIMESTAMP,
    response_time_ms INTEGER,
    http_status_code INTEGER,
    error_message    TEXT,
    checked_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (response_time_ms IS NULL OR response_time_ms >= 0),
    FOREIGN KEY (service_id) REFERENCES network_services (id) ON DELETE CASCADE,
    FOREIGN KEY (status_id) REFERENCES service_statuses (id)
);

--rollback DROP TABLE check_results;
--liquibase formatted sql

--changeset vyacheslav_borisov_04_04_2026:10_create_incidents_table
CREATE TABLE incidents
(
    id                        BIGSERIAL PRIMARY KEY,
    service_id                BIGINT    NOT NULL,
    status_id                 BIGINT    NOT NULL,
    opened_at                 TIMESTAMP NOT NULL,
    closed_at                 TIMESTAMP,
    reason                    TEXT,
    opened_by_check_result_id BIGINT,
    closed_by_check_result_id BIGINT,
    CHECK (closed_at IS NULL OR closed_at >= opened_at),
    FOREIGN KEY (service_id) REFERENCES network_services (id) ON DELETE CASCADE,
    FOREIGN KEY (status_id) REFERENCES incident_statuses (id),
    FOREIGN KEY (opened_by_check_result_id) REFERENCES check_results (id),
    FOREIGN KEY (closed_by_check_result_id) REFERENCES check_results (id)
);

--rollback DROP TABLE incidents;
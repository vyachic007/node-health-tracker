--liquibase formatted sql

--changeset vyacheslav_borisov_20_05_2026:015-create-incident-timeline-events
CREATE TABLE incident_timeline_events
(
    id              BIGSERIAL PRIMARY KEY,
    incident_id     BIGINT        NOT NULL,
    check_result_id BIGINT,
    event_type      VARCHAR(40)   NOT NULL,
    message         VARCHAR(1000) NOT NULL,
    created_at      TIMESTAMP     NOT NULL,

    FOREIGN KEY (incident_id) REFERENCES incidents (id) ON DELETE CASCADE,
    FOREIGN KEY (check_result_id) REFERENCES check_results (id) ON DELETE SET NULL
);

CREATE INDEX idx_incident_timeline_events_incident_id
    ON incident_timeline_events (incident_id);

CREATE INDEX idx_incident_timeline_events_created_at
    ON incident_timeline_events (created_at);

--rollback DROP TABLE incident_timeline_events;
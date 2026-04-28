--liquibase formatted sql

--changeset vyacheslav_borisov_04_04_2026:14_create_sent_notifications_table
CREATE TABLE sent_notifications
(
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT      NOT NULL,
    incident_id   BIGINT      NOT NULL,
    channel       VARCHAR(20) NOT NULL,
    event         VARCHAR(50) NOT NULL,
    sent_at       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status        VARCHAR(20) NOT NULL,
    error_message TEXT,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (incident_id) REFERENCES incidents (id) ON DELETE CASCADE
);

--rollback DROP TABLE sent_notifications;
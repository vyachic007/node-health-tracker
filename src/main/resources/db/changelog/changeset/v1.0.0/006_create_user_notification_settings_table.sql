--liquibase formatted sql

--changeset vyacheslav_borisov_04_04_2026:12_create_user_notification_settings_table
CREATE TABLE user_notification_settings
(
    id                          BIGSERIAL PRIMARY KEY,
    user_id                     BIGINT      NOT NULL,
    channel                     VARCHAR(20) NOT NULL,
    is_enabled                  BOOLEAN     NOT NULL DEFAULT FALSE,
    destination                 VARCHAR(255),
    notify_on_incident_open     BOOLEAN     NOT NULL DEFAULT TRUE,
    notify_on_incident_resolved BOOLEAN     NOT NULL DEFAULT TRUE,
    UNIQUE (user_id, channel),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

--rollback DROP TABLE user_notification_settings;
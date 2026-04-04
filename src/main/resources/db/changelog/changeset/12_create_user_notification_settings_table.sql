--liquibase formatted sql

--changeset vyacheslav_borisov_04_04_2026:12_create_user_notification_settings_table
CREATE TABLE user_notification_settings
(
    id                          BIGSERIAL PRIMARY KEY,
    user_id                     BIGINT  NOT NULL,
    channel_id                  BIGINT  NOT NULL,
    is_enabled                  BOOLEAN NOT NULL DEFAULT FALSE,
    destination                 VARCHAR(255),
    notify_on_incident_open     BOOLEAN NOT NULL DEFAULT TRUE,
    notify_on_incident_resolved BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (user_id, channel_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (channel_id) REFERENCES notification_channels (id)
);

--rollback DROP TABLE user_notification_settings;
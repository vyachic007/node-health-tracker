--liquibase formatted sql

--changeset vyacheslav_borisov_04_04_2026:11_create_notification_channels_table
CREATE TABLE notification_channels
(
    id   BIGSERIAL PRIMARY KEY,
    code VARCHAR(20)  NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL
);

--rollback DROP TABLE notification_channels;
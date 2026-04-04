--liquibase formatted sql

--changeset vyacheslav_borisov_04_04_2026:13_create_notification_events_table
CREATE TABLE notification_events
(
    id   BIGSERIAL PRIMARY KEY,
    code VARCHAR(50)  NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL
);

--rollback DROP TABLE notification_events;
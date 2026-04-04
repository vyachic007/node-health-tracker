--liquibase formatted sql

--changeset vyacheslav_borisov_04_04_2026:9_create_incident_statuses_table
CREATE TABLE incident_statuses
(
    id   BIGSERIAL PRIMARY KEY,
    code VARCHAR(20)  NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL
);

--rollback DROP TABLE incident_statuses;
--liquibase formatted sql

--changeset vyacheslav_borisov_04_04_2026:7_create_service_statuses_table
CREATE TABLE service_statuses
(
    id   BIGSERIAL PRIMARY KEY,
    code VARCHAR(20)  NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL
);

--rollback DROP TABLE service_statuses;
--liquibase formatted sql

--changeset vyacheslav_borisov_04_04_2026:5_create_check_types_table
CREATE TABLE check_types
(
    id   BIGSERIAL PRIMARY KEY,
    code VARCHAR(20)  NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL
);

--rollback DROP TABLE check_types;
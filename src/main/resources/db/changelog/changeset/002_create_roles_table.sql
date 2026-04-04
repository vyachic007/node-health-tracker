--liquibase formatted sql

--changeset vyacheslav_borisov_04_04_2026:2_create_roles_table
CREATE TABLE roles
(
    id   BIGSERIAL PRIMARY KEY,
    code VARCHAR(50)  NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL
);

--rollback DROP TABLE roles;
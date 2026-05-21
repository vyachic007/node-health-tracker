--liquibase formatted sql

--changeset vyacheslav_borisov_21_05_2026:016-create-audit-logs-table
CREATE TABLE audit_logs
(
    id           BIGSERIAL PRIMARY KEY,
    action_type  VARCHAR(50)   NOT NULL,
    description  VARCHAR(1000) NOT NULL,
    entity_type  VARCHAR(100)  NOT NULL,
    entity_id    BIGINT,
    user_id      BIGINT,
    username     VARCHAR(100),
    created_at   TIMESTAMP     NOT NULL
);

CREATE INDEX idx_audit_logs_user_id ON audit_logs (user_id);
CREATE INDEX idx_audit_logs_action_type ON audit_logs (action_type);
CREATE INDEX idx_audit_logs_created_at ON audit_logs (created_at);
CREATE INDEX idx_audit_logs_entity ON audit_logs (entity_type, entity_id);
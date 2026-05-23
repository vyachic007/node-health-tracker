--liquibase formatted sql

--changeset vyacheslav_borisov_23_05_2026:018-create-telegram-binding-tokens-table
CREATE TABLE telegram_binding_tokens
(
    id         BIGSERIAL PRIMARY KEY,

    user_id    BIGINT      NOT NULL,

    token      VARCHAR(64) NOT NULL UNIQUE,

    expires_at TIMESTAMP   NOT NULL,
    used_at    TIMESTAMP   NULL,
    created_at TIMESTAMP   NOT NULL,

    CONSTRAINT fk_telegram_binding_tokens_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_telegram_binding_tokens_user_id
    ON telegram_binding_tokens (user_id);

CREATE INDEX idx_telegram_binding_tokens_token
    ON telegram_binding_tokens (token);

CREATE INDEX idx_telegram_binding_tokens_expires_at
    ON telegram_binding_tokens (expires_at);

CREATE INDEX idx_telegram_binding_tokens_used_at
    ON telegram_binding_tokens (used_at);
--liquibase formatted sql

--changeset vyacheslav_borisov_22_05_2026:017-create-password-reset-tokens-table
CREATE TABLE password_reset_tokens
(
    id         BIGSERIAL PRIMARY KEY,

    token_hash VARCHAR(255) NOT NULL UNIQUE,

    user_id    BIGINT       NOT NULL,

    expires_at TIMESTAMP    NOT NULL,
    used_at    TIMESTAMP    NULL,
    created_at TIMESTAMP    NOT NULL,

    CONSTRAINT fk_password_reset_tokens_user
        FOREIGN KEY (user_id) REFERENCES users (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_tokens_user_id
    ON password_reset_tokens (user_id);

CREATE INDEX idx_password_reset_tokens_token_hash
    ON password_reset_tokens (token_hash);

CREATE INDEX idx_password_reset_tokens_expires_at
    ON password_reset_tokens (expires_at);
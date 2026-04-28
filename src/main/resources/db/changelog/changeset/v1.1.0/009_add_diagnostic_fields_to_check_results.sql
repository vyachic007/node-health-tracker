--liquibase formatted sql

--changeset vyacheslav_borisov_28_04_2026:9_add_diagnostic_fields_to_check_results
ALTER TABLE check_results
    ADD COLUMN failure_layer VARCHAR(30),
    ADD COLUMN diagnostic_message TEXT,
    ADD COLUMN recommendation TEXT;

--rollback ALTER TABLE check_results DROP COLUMN recommendation;
--rollback ALTER TABLE check_results DROP COLUMN diagnostic_message;
--rollback ALTER TABLE check_results DROP COLUMN failure_layer;
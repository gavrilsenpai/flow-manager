--liquibase formatted sql
--changeset gavrilsenpai:001-create-file-metadata

CREATE TABLE file_metadata (
                               id VARCHAR(36) PRIMARY KEY,
                               original_file_name VARCHAR(255) NOT NULL,
                               source_bucket VARCHAR(255) NOT NULL,
                               source_object_key VARCHAR(255) NOT NULL,
                               target_bucket VARCHAR(255),
                               target_object_key VARCHAR(255),
                               status VARCHAR(32) NOT NULL,
                               error_message TEXT,
                               created_at TIMESTAMP NOT NULL,
                               updated_at TIMESTAMP NOT NULL
);

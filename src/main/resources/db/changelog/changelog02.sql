--liquibase formatted sql

--changeset MarcinSz1993:4
ALTER TABLE app_user RENAME TO users;

--changeset MarcinSz1993:5
ALTER TABLE users
    ADD COLUMN username VARCHAR(255) UNIQUE;
-- liquibase formatted sql

--changeset MarcinSz1993:15

ALTER TABLE event DROP CONSTRAINT event_event_status_check;

ALTER TABLE event ADD CONSTRAINT event_event_status_check
    CHECK (event_status IN ('ACTIVE', 'COMPLETED', 'CANCELLED', 'FULL'));
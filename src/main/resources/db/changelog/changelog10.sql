--liquibase formatted sql

--changeset MarcinSz1993:13

INSERT INTO participants_events(event_id, user_id)
VALUES
(11,2),
(11,6),
(11,8),
(12,4),
(12,6),
(12,10)
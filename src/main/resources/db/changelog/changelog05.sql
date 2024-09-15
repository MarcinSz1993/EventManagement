--liquibase formatted sql

--changeset MarcinSz1993:8

ALTER TABLE event
ADD COLUMN organizer_id bigint
constraint fkhuu2qgto4g6ktuf87710bfkuq
references users;
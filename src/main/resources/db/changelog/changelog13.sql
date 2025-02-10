--liquibase formatted sql
--changeset MarcinSz1993:16

create table reset_token
(
    id          bigserial
        primary key,
    expire_time timestamp(6),
    token       varchar(255),
    user_id     bigint
        constraint uk_fbfq7c1c1wxpt21p6jd2jtvhj
            unique
        constraint fk4vxwjrcj8j479hf5iehw6qnaa
            references users
);


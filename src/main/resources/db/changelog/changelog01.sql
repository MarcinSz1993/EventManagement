--liquibase formatted sql

--changeset MarcinSz1993:1

create table if not exists app_user
(
    birth_date     date,
    id             bigserial
        primary key,
    account_number varchar(255)
        unique,
    account_status varchar(255),
    email          varchar(255)
        unique,
    first_name     varchar(255),
    last_name      varchar(255),
    password       varchar(255),
    phone_number   varchar(255)
        unique,
    role           varchar(255)
        constraint app_user_role_check
            check ((role)::text = ANY ((ARRAY ['USER'::character varying, 'ADMIN'::character varying])::text[]))
);

--changeset MarcinSz1993:2

create table if not exists event
(
    event_date        date,
    max_attendees     integer          not null,
    ticket_price      double precision not null,
    created_date      timestamp(6),
    id                bigserial
        primary key,
    modified_date     timestamp(6),
    event_description varchar(255),
    event_name        varchar(255) unique,
    event_status      varchar(255)
        constraint event_event_status_check
            check ((event_status)::text = ANY
                   ((ARRAY ['ACTIVE'::character varying, 'COMPLETED'::character varying, 'CANCELLED'::character varying])::text[])),
    event_target        varchar(255)
        constraint event_event_type_check
            check ((event_target)::text = ANY
                   ((ARRAY ['FAMILY'::character varying, 'SINGLES'::character varying, 'CHILDREN'::character varying, 'EVERYBODY'::character varying, 'ADULTS_ONLY'::character varying])::text[])),
    location          varchar(255)
);

--changeset MarcinSz1993:3
create table if not exists participants_events
(
    event_id bigint not null
        constraint fk33x5do29tl4e7wop9yror0uck
            references event,
    user_id  bigint not null
        constraint fk2njmq6nnk9o4wmb8db6mg5bup
            references app_user
);

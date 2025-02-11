--liquibase formatted sql

--changeset MarcinSz1993:7

create table review
(
    id       bigserial  not null
        primary key,
    content  varchar(255),
    degree   integer not null,
    event_id bigint
        constraint fkqhtk2kbsx9ga87m7xr21b3ggr
            references event,
    user_id  bigint
        constraint fk6cpw2nlklblpvc7hyt7ko6v3e
            references users
);

create table ticket
(
    id         bigserial  not null
        primary key,
    has_ticket boolean not null,
    event_id   bigint
        constraint fkfytuhjopeamxbt1cpudy92x5n
            references event,
    user_id    bigint
        constraint fkmvugyjf7b45u0juyue7k3pct0
            references users
);

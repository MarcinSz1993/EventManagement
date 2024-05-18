--liquibase formatted sql

--changeset MarcinSz1993:1

CREATE TABLE IF NOT EXISTS event (
                                     id serial primary key,
                                     event_name varchar,
                                     event_description varchar,
                                     location varchar,
                                     max_attendees integer DEFAULT 0,
                                     event_date timestamp,
                                     event_status varchar,
                                     ticket_price double precision,
                                     event_type varchar,
                                     created_date timestamp,
                                     modified_date timestamp
);

CREATE TABLE IF NOT EXISTS app_user(
                                       id serial primary key,
                                       first_name varchar,
                                       last_name varchar,
                                       email varchar UNIQUE ,
                                       password varchar,
                                       birth_date timestamp without time zone,
                                       role varchar,
                                       phone_number varchar UNIQUE ,
                                       account_number varchar UNIQUE ,
                                       account_status varchar
);

CREATE TABLE IF NOT EXISTS users_events(
                                           user_id bigint NOT NULL,
                                           event_id bigint NOT NULL,
                                           PRIMARY KEY (user_id,event_id),
                                           CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES  app_user(id),
                                           CONSTRAINT fk_event FOREIGN KEY (event_id) REFERENCES event(id)
);
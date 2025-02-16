--liquibase formatted sql
--changeset MarcinSz1993:18

INSERT INTO event (event_date, max_attendees, ticket_price, created_date, modified_date, event_description, event_name, event_status, event_target, location, organizer_id)
VALUES ('2025-02-14',1000,0,'2024-12-31','2025-01-31','Write your opinion about my skills','Event Management Feedback','COMPLETED','EVERYBODY','Lublin',1);
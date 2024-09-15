--liquibase formatted sql

--changeset MarcinSz1993:12

INSERT INTO event (event_date, max_attendees, ticket_price, created_date, modified_date, event_description, event_name, event_status, event_target, location, organizer_id)
VALUES
('2024-08-15', 300, 150.00, '2024-01-01 10:00:00','2024-02-01 10:00:00', 'International Startup Conference', 'Global Startup Summit', 'COMPLETED', 'SINGLES', 'Lublin', 1),
('2024-09-13', 100, 0.0, '2024-01-01 10:00:00','2024-04-01 10:00:00', 'Opening McDonald in Łęczna', 'New McDonald restaurant', 'COMPLETED', 'EVERYBODY', 'Łęczna', 1)
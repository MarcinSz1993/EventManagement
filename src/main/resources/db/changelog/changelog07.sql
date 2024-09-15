--liquibase formatted sql

--changeset MarcinSz1993:10

INSERT INTO event(event_date, max_attendees, ticket_price, created_date, modified_date, event_description, event_name, event_status, event_target, location, organizer_id)
VALUES
    ('2024-10-15', 300, 150.00, '2024-01-01 10:00:00', '2024-01-01 10:00:00', 'Annual technology conference', 'Tech Conference 2024', 'ACTIVE', 'ADULTS_ONLY', 'New York', 1),
    ('2024-11-20', 1000, 75.00, '2024-01-02 11:00:00', '2024-01-02 11:00:00', 'Open-air music festival', 'Music Fest', 'ACTIVE', 'EVERYBODY', 'Los Angeles', 1),
    ('2024-10-10', 150, 50.00, '2024-01-03 12:00:00', '2024-01-03 12:00:00', 'Pitch your startup ideas to investors', 'Startup Pitch Day', 'ACTIVE', 'EVERYBODY', 'San Francisco', 1),
    ('2024-10-05', 500, 120.00, '2024-01-04 13:00:00', '2024-01-04 13:00:00', 'Exhibition of modern art', 'Art Expo', 'ACTIVE', 'CHILDREN', 'Paris', 1),
    ('2024-12-25', 200, 80.00, '2024-01-05 14:00:00', '2024-01-05 14:00:00', 'Meet with industry leaders', 'Business Networking Meetup', 'ACTIVE', 'EVERYBODY', 'London', 1),
    ('2024-10-12', 100, 200.00, '2024-01-06 15:00:00', '2024-01-06 15:00:00', 'Hands-on workshop on AI tools', 'AI Workshop', 'ACTIVE', 'SINGLES', 'Berlin', 1),
    ('2024-11-14', 700, 50.00, '2024-01-07 16:00:00', '2024-01-07 16:00:00', 'A gathering of health and fitness enthusiasts', 'Health and Wellness Expo', 'ACTIVE', 'ADULTS_ONLY', 'Toronto', 1),
    ('2024-09-18', 400, 100.00, '2024-01-08 17:00:00', '2024-01-08 17:00:00', 'Esports competition', 'Gaming Tournament', 'ACTIVE', 'EVERYBODY', 'Tokyo', 1),
    ('2024-10-10', 300, 250.00, '2024-01-09 18:00:00', '2024-01-09 18:00:00', 'Fashion designers showcase', 'Fashion Week', 'ACTIVE', 'CHILDREN', 'Milan', 1),
    ('2024-11-20', 50, 300.00, '2024-01-10 19:00:00',  '2024-01-10 19:00:00', 'Explore the finest wines', 'Wine Tasting Tour', 'ACTIVE', 'SINGLES', 'Napa Valley', 1);
--liquibase formatted sql
--changeset MarcinSz1993:19

UPDATE users SET username = 'marcinsz1993'
WHERE id = 1;

INSERT INTO event(event_date, max_attendees, ticket_price, created_date, modified_date, event_description, event_name, event_status, event_target, location, organizer_id)
VALUES
    ('2025-02-28', 5, 120.00, '2025-02-01 12:00:00', '2025-02-01 12:00:00', 'Exclusive wine tasting event', 'Wine & Dine Festival', 'ACTIVE', 'ADULTS_ONLY', 'San Francisco', 5),
    ('2025-03-01', 7, 80.00, '2025-02-02 14:30:00', '2025-02-02 14:30:00', 'Live concert featuring top rock bands', 'Rock Arena Live', 'ACTIVE', 'EVERYBODY', 'Los Angeles', 8),
    ('2025-02-25', 3, 50.00, '2025-02-03 09:00:00', '2025-02-03 09:00:00', 'Interactive coding bootcamp', 'Full-Stack Dev Workshop', 'ACTIVE', 'ADULTS_ONLY', 'Seattle', 12),
    ('2025-02-27', 8, 100.00, '2025-02-04 11:00:00', '2025-02-04 11:00:00', 'Local startup networking meetup', 'Innovators Hub', 'ACTIVE', 'SINGLES', 'Austin', 3),
    ('2025-03-02', 4, 90.00, '2025-02-05 13:00:00', '2025-02-05 13:00:00', 'Annual fashion show featuring top designers', 'Fashion Forward 2025', 'ACTIVE', 'CHILDREN', 'Chicago', 17),
    ('2025-03-03', 6, 60.00, '2025-02-06 15:00:00', '2025-02-06 15:00:00', 'Food festival with international cuisine', 'World Food Expo', 'ACTIVE', 'CHILDREN', 'Miami', 9),
    ('2025-03-06', 2, 110.00, '2025-02-07 10:00:00', '2025-02-07 10:00:00', 'Motivational talk by renowned speakers', 'Success Summit', 'ACTIVE', 'EVERYBODY', 'Boston', 6),
    ('2025-02-23', 3, 75.00, '2025-02-08 08:30:00', '2025-02-08 08:30:00', 'Indie film screenings & panel discussions', 'Cine Indie Fest', 'ACTIVE', 'ADULTS_ONLY', 'Denver', 14),
    ('2025-02-24', 2, 95.00, '2025-02-09 12:45:00', '2025-02-09 12:45:00', 'Wellness and yoga retreat', 'Mind & Body Retreat', 'ACTIVE', 'EVERYBODY', 'San Diego', 2),
    ('2025-02-25', 4, 130.00, '2025-02-10 16:00:00', '2025-02-10 16:00:00', 'Auto expo showcasing the latest cars', 'Future Cars Expo', 'ACTIVE', 'EVERYBODY', 'Detroit', 11),
    ('2025-02-26', 2, 70.00, '2025-02-11 14:00:00', '2025-02-11 14:00:00', 'Hip-hop dance battle championship', 'Street Dance Showdown', 'ACTIVE', 'SINGLES', 'Las Vegas', 7),
    ('2025-02-27', 3, 85.00, '2025-02-12 09:00:00', '2025-02-12 09:00:00', 'Historical reenactment and culture fair', 'Time Travelers Fair', 'ACTIVE', 'CHILDREN', 'Philadelphia', 19),
    ('2025-02-28', 5, 65.00, '2025-02-13 10:00:00', '2025-02-13 10:00:00', 'Gaming tournament with live streaming', 'eSports Championship', 'ACTIVE', 'CHILDREN', 'Dallas', 13),
    ('2025-03-01', 6, 40.00, '2025-02-14 12:00:00', '2025-02-14 12:00:00', 'Art and craft exhibition by local artists', 'Creative Minds Expo', 'ACTIVE', 'EVERYBODY', 'Portland', 20),
    ('2025-03-02', 4, 105.00, '2025-02-15 17:30:00', '2025-02-15 17:30:00', 'National science & tech innovation fair', 'Future Scientists Convention', 'ACTIVE', 'EVERYBODY', 'Washington D.C.', 10);
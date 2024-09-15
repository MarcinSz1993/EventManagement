--liquibase formatted sql

--changeset MarcinSz1993:11

INSERT INTO users(birth_date, account_number, account_status, email, first_name, last_name, password, phone_number, role, username)
VALUES
    ('1990-05-15', '1234567800', 'ACTIVE', 'john.doe@example.com', 'John', 'Doe', '$2a$12$fNvXXE90VQkn2P/i0BTjZO6ijl31e5lfggBWrQp8zLo6JwzdyO1Li', '123456780', 'USER', 'johndoe'),
    ('1985-11-20', '2345678901', 'ACTIVE', 'jane.smith@example.com', 'Jane', 'Smith', '$2a$12$PPmwh.oE1oHkePhPupqYK.5fb6fOUutlXWJtMdEdrCn5GkpGomyF6', '123456783', 'USER', 'janesmith'),
    ('1992-02-10', '3456789012', 'ACTIVE', 'michael.johnson@example.com', 'Michael', 'Johnson', '$2a$12$n9CTAlaVms/DGCbfgWw8LOFUwdz/E/HbkIpXPNGRpa7g5Slcke3UO', '123456787', 'USER', 'mjohnson'),
    ('1988-07-22', '4567890123', 'ACTIVE', 'emily.davis@example.com', 'Emily', 'Davis', ' $2a$12$l93DRq/juGLQJ3zsSm.c6OsGwuUFEaaNHWgaTINBg/C5jJZhLEOk2 ', '123456333', 'USER', 'edavis'),
    ('1995-03-30', '5678901234', 'ACTIVE', 'david.wilson@example.com', 'David', 'Wilson', '$2a$12$VRNBNgxZa4jpiQe9M1/.JufTkbsV3gd9fDjS0OfPvKqXY2zFCzVrO', '123056789', 'USER', 'dwilson'),
    ('1980-09-05', '6789012345', 'ACTIVE', 'sarah.brown@example.com', 'Sarah', 'Brown', '$2a$12$EE0i/viIr2eLJEkijH1.ee7iY9gvyZSGgoIDx8qhYjXKpokSIa1IS', '823456789', 'USER', 'sbrown'),
    ('1998-12-14', '7890123456', 'ACTIVE', 'chris.lee@example.com', 'Chris', 'Lee', '$2a$12$B2sF/kzTguu.qrBOEGfJvOKAXbhRnYkQstPACUzea/ecXPlUyQV1a', '183456789', 'USER', 'clee'),
    ('1982-04-17', '8901234567', 'ACTIVE', 'laura.martin@example.com', 'Laura', 'Martin', '$2a$12$vgIs5QYFokKmcIQyRZOPU.MsKBIk7UK4l6SlcWxeCgEW3bHw07Y/.', '124456789', 'USER', 'lmartin'),
    ('1991-06-25', '9012345678', 'ACTIVE', 'robert.clark@example.com', 'Robert', 'Clark', '$2a$12$y2A8gLo4GQqY64iYcqCvOeoT64pHO4v.qNLUZZSO6ioD41afWpS.C', '423456789', 'USER', 'rclark');


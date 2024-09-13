--liquibase formatted sql

--changeset MarcinSz1993:6
INSERT INTO users(birth_date, id, account_number, account_status, email, first_name, last_name, password, phone_number, role, username)
VALUES ('1993-01-06',1,'1234567890','ACTIVE','marcinsz1993@hotmail.com','Marcin','Szabała','qwerty','123456789','ADMIN','Markinlol')
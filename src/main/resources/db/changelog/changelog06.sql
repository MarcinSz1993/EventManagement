--liquibase formatted sql

--changeset MarcinSz1993:9

UPDATE users SET password='$2a$12$v0pFPEEeohtoYmsvCEhGj.CTxd9.BvCZdxGLyZLPwgwZJuPwm5Q1q'
WHERE id = 1;
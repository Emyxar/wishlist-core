--liquibase formatted sql

--changeset emyxar:20261031-001
--comment: Комент

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(254) UNIQUE NOT NULL,
    password VARCHAR(254) NOT NULL,
    full_name VARCHAR(254) NOT NULL,
    url_avatar TEXT
);
CREATE TYPE USER_ROLE AS ENUM ('ADMIN', 'USER');
CREATE TABLE users
(
    id         bigint PRIMARY KEY,
    email      VARCHAR(255) UNIQUE NOT NULL,
    password   VARCHAR(255)        NOT NULL,
    firstname  VARCHAR(100),
    lastname   VARCHAR(100),
    role       USER_ROLE                NOT NULL DEFAULT 'USER',
    created_at timestamptz         NOT NULL,
    updated_at timestamptz
);
CREATE INDEX idx_users_email ON users (email);
CREATE TABLE users
(
    id       BIGSERIAL PRIMARY KEY,
    username TEXT  NOT NULL,
    password TEXT  NOT NULL,
    roles    JSONB NOT NULL
);

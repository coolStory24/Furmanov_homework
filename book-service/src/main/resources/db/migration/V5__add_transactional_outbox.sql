CREATE TABLE outbox
(
    id BIGSERIAL PRIMARY KEY,
    data TEXT NOT NULL,
    send boolean NOT NULL DEFAULT false
);

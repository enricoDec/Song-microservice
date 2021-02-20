DROP TABLE IF EXISTS concerts CASCADE;
CREATE TABLE concerts
(
    concert_id  serial      not null,
    location    varchar(50) not null,
    artist      varchar(50) not null,
    max_tickets int         not null,

    PRIMARY KEY (concert_id)
);

INSERT INTO concerts(location, artist, max_tickets)
VALUES ('Rome', 'Justin Timberlake', 100);

DROP TABLE IF EXISTS transactions CASCADE;
CREATE TABLE transactions
(
    transaction_id serial  not null,
    payed          boolean not null,

    PRIMARY KEY (transaction_id)
);

INSERT INTO transactions(payed)
VALUES (false);

DROP TABLE IF EXISTS tickets CASCADE;
CREATE TABLE tickets
(
    ticket_id      serial      not null,
    owner          varchar(50) not null,
    concert_id     int         not null,
    transaction_id int         not null,
    ticket_qr_path varchar(80),

    PRIMARY KEY (ticket_id),
    FOREIGN KEY (concert_id)
        REFERENCES concerts (concert_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    FOREIGN KEY (transaction_id)
        REFERENCES transactions (transaction_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

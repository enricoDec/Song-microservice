DROP TABLE IF EXISTS users CASCADE;
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

DROP TABLE IF EXISTS tickets CASCADE;
CREATE TABLE tickets
(
    ticket_id  serial      not null,
    owner      varchar(50) not null,
    concert_id int         not null,

    PRIMARY KEY (ticket_id),
    FOREIGN KEY (concert_id)
        REFERENCES concerts (concert_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

INSERT INTO tickets(owner, concert_id)
VALUES ('mmuster', 1);


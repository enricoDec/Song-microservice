DROP TABLE IF EXISTS songs CASCADE;

CREATE TABLE songs
(
    id    serial not null,
    title  varchar(100) not null,
    artist varchar(100),
    label  varchar(100),
    released int,

    PRIMARY KEY (id)
);

INSERT INTO songs(title, artist, label, released)
VALUES ('Can’t Stop the Feeling', 'Justin Timberlake', 'Trolls', 2016),
       ('Mom', 'Meghan Trainor, Kelli Trainor', 'Thank You', 2016),
       ('Team', 'Iggy Azalea', null, 2016),
       ('Ghostbusters (I''m not a fraid)', 'Fall Out Boy, Missy Elliott', 'Ghostbusters', 2016),
       ('Bad Things', 'Camila Cabello, Machine Gun Kelly', 'Bloom', 2017),
       ('I Took a Pill in Ibiza', 'Mike Posner', 'At Night, Alone.', 2016),
       ('i hate u, i love u', 'Gnash', 'Top Hits 2017', 2017),
       ('No', 'Meghan Trainor', 'Thank You', 2016),
       ('Private Show', 'Britney Spears', 'Glory', 2016),
       ('7 Years', 'Lukas Graham', 'Lukas Graham (Blue Album)', 2015);

DROP TABLE IF EXISTS playlists_songs CASCADE;
DROP TABLE IF EXISTS playlists CASCADE;

create table playlists
(
    id         serial      not null,
    name       varchar(45) not null,
    is_private boolean     not null,
    owner      varchar(50) not null,

    PRIMARY KEY (id)
);
create table playlists_songs
(
    playlist int not null,
    song     int not null,

    CONSTRAINT fk_playlist
        FOREIGN KEY (playlist)
            REFERENCES playlists (id)
            ON DELETE CASCADE
            ON UPDATE CASCADE,
    CONSTRAINT fk_song
        FOREIGN KEY (song)
            REFERENCES songs (id)
            ON DELETE CASCADE
            ON UPDATE CASCADE
);

INSERT INTO playlists(name, is_private, owner)
VALUES ('Mmuster''s Private Playlist', true, 'mmuster'),
       ('Mmuster''s Public Playlist', false, 'mmuster'),
       ('Eschuler''s Private Playlist', true, 'eschuler'),
       ('Eschuler''s Public Playlist', false, 'eschuler');
INSERT INTO playlists_songs(playlist, song)
VALUES (1, 4),
       (1, 5),
       (2, 6),
       (2, 7),
       (3, 2),
       (3, 3),
       (4, 8),
       (4, 9);
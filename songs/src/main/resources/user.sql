CREATE TABLE users
(
    userid    varchar(50) not null,
    password  varchar(50) not null,
    firstname varchar(50) not null,
    lastname  varchar(50) not null,

    PRIMARY KEY (userid)
);

TRUNCATE TABLE users CASCADE;

insert into users(userid, password, firstname, lastname)
values ('mmuster', 'pass1234', 'Maxime', 'Muster'),
       ('eschuler', 'pass1234', 'Elena', 'Schuler');

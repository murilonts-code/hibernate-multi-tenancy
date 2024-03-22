CREATE SEQUENCE known_dbs_id_seq;
SELECT setval('known_dbs_id_seq', 2);
create table if not exists known_dbs
(
    id       bigint generated always as identity,
    host     varchar(40) not null,
    username varchar(40) not null,
    password varchar(40) not null,
    db  varchar(40) not null,
    port     integer     not null
);
INSERT INTO known_dbs(host, username, password, db, port) VALUES ('localhost', 'quarkus_test', 'quarkus_test', 'quarkus_test',  5432);
INSERT INTO known_dbs(host, username, password, db, port) VALUES ('localhost', 'mycompany', 'mycompany', 'mycompany',  5433);
INSERT INTO known_dbs(host, username, password, db, port) VALUES ('localhost', 'base', 'base', 'base',  5434);

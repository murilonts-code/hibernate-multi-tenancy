CREATE SEQUENCE known_fruits_id_seq;
SELECT setval('known_fruits_id_seq', 3);
create table if not exists known_fruits
(
  id   INT,
  name VARCHAR(40)
);
INSERT INTO known_fruits(id, name) VALUES (1, 'Cherry');
INSERT INTO known_fruits(id, name) VALUES (2, 'Apple');
INSERT INTO known_fruits(id, name) VALUES (3, 'Banana');

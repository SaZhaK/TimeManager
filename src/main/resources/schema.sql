create table if not exists users
(
  id       serial,
  login    varchar(32),
  password varchar(32)
);
create table users (
  id int IDENTITY(1, 1) primary key,
  username varchar(255) unique,
  password varchar(255),
  superuser char(1),
  principal float,
  startDate datetime,
  endDate datetime,
  enabled char(1)
);

create table positions (
  id int IDENTITY(1,1) primary key,
  code varchar(255),
  name varchar(255),
  amount int,
  price float,
  profit float,
  userid int
);

create table dealLogs (
  id int IDENTITY(1,1) primary key,
  code varchar(255),
  name varchar(255),
  bs char(4),
  price float,
  amount int,
  commission float,
  created datetime
);


insert into users(username, password, superuser, principal, startDate, endDate, enabled)
values('wfei', 'test', 'Y', 1000000, '2010-11-06', null, 'Y');

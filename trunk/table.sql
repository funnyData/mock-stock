create table users (
  id int IDENTITY(1, 1) primary key,
  username varchar(255) unique,
  password varchar(255),
  superuser char(1),
  principal float,
  initialPrincipal float,
  startDate datetime,
  endDate datetime,
  enabled char(1)
);

/*
* cost including the commission fee;
* realProfit will be calculated when amount is zero;
* lastAmount will be the amount when user sell out this stock
*/
create table positions (
  id int IDENTITY(1,1) primary key,
  code varchar(255),
  name varchar(255),
  amount int,
  cost float,
  commission float,
  profit float,
  userid varchar(255),
  created datetime,
  modified datetime
);

/*
* price not including the commission fee
*/
create table dealLogs (
  id int IDENTITY(1,1) primary key,
  code varchar(255),
  name varchar(255),
  bs char(4),
  price float,
  amount int,
  commission float,
  userid varchar(255),
  created datetime
);

insert into users(username, password, superuser, principal, initialPrincipal, startDate, endDate, enabled)
values('wfei', 'test', 'Y', 1000000, 1000000, '2010-11-06', null, 'Y');

insert into positions (code,name, amount,cost, commission, userid)
values('600710', '常林股份', 15000, 8.39, 880.95, 'wfei');

insert into positions (code,name, amount,cost, commission, userid)
values('600856', '长百集团', 15000, 7.07, 742.35, 'wfei');

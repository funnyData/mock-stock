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
alter table users add displayName varchar(255);

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

/*
dhzq 东海证券 (admin)
lczx 理财中心 (admin)
kxxl 凯旋西路
zsl 周山路
zzdl 中州东路
xyl 西苑路
jsxl 建设西路
*/

insert into users(username, displayName, password, superuser, principal, initialPrincipal, startDate, endDate, enabled)
values('dhzq', '东海证券', '1234', 'Y', 1000000, 1000000, '2010-11-10', null, 'Y');
insert into users(username, displayName, password, superuser, principal, initialPrincipal, startDate, endDate, enabled)
values('lczx', '理财中心', '1234', 'Y', 1000000, 1000000, '2010-11-10', null, 'Y');
insert into users(username, displayName, password, superuser, principal, initialPrincipal, startDate, endDate, enabled)
values('kxxl', '凯旋西路', '1234','Y', 1000000, 1000000, '2010-11-10', null, 'N');
insert into users(username, displayName, password, superuser, principal, initialPrincipal, startDate, endDate, enabled)
values('zsl', '周山路', '1234','Y', 1000000, 1000000, '2010-11-10', null, 'N');
insert into users(username, displayName, password, superuser, principal, initialPrincipal, startDate, endDate, enabled)
values('zzdl', '中州东路', '1234','Y', 1000000, 1000000, '2010-11-10', null, 'N');
insert into users(username, displayName, password, superuser, principal, initialPrincipal, startDate, endDate, enabled)
values('xyl', '西苑路', '1234','Y', 1000000, 1000000, '2010-11-10', null, 'N');
insert into users(username, displayName, password, superuser, principal, initialPrincipal, startDate, endDate, enabled)
values('jsxl', '建设西路', '1234','Y', 1000000, 1000000, '2010-11-10', null, 'N');

/*
{
{"000733", "振华科技", "9000", "12.88"},
{"000547", "闽福发A", "5000", "7.97"},
{"002006", "精工科技", "2500", "7.13"},
{"000968", "煤气化", "10000", "20.58"},
{"000407", "胜利股份", "30000", "7.65"},
{"000630", "铜陵有色", "4000", "28.29"}
}
*/
update users set principal = '435591.2' where username = 'lczx'; 



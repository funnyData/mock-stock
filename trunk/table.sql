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
values('dhzq', '东海证券', '1234', 'Y', 2000000, 1000000, '2010-11-10', null, 'Y');
insert into users(username, displayName, password, superuser, principal, initialPrincipal, startDate, endDate, enabled)
values('lczx', '理财中心', '1234', 'Y', 2000000, 1000000, '2010-11-10', null, 'Y');
insert into users(username, displayName, password, superuser, principal, initialPrincipal, startDate, endDate, enabled)
values('kxxl', '凯旋西路', '1234','Y', 2000000, 1000000, '2010-11-10', null, 'N');
insert into users(username, displayName, password, superuser, principal, initialPrincipal, startDate, endDate, enabled)
values('zsl', '周山路', '1234','Y', 2000000, 1000000, '2010-11-10', null, 'N');
insert into users(username, displayName, password, superuser, principal, initialPrincipal, startDate, endDate, enabled)
values('zzdl', '中州东路', '1234','Y', 2000000, 1000000, '2010-11-10', null, 'N');
insert into users(username, displayName, password, superuser, principal, initialPrincipal, startDate, endDate, enabled)
values('xyl', '西苑路', '1234','Y', 2000000, 1000000, '2010-11-10', null, 'N');
insert into users(username, displayName, password, superuser, principal, initialPrincipal, startDate, endDate, enabled)
values('jsxl', '建设西路', '1234','Y', 2000000, 1000000, '2010-11-10', null, 'N');

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
update users set principal = '1000000' where username = 'dhzq';
update users set principal = '309313.4' where username = 'lczx';
update users set principal = '29242.2' where username = 'kxxl';
update users set principal = '186959.74' where username = 'zsl';
update users set principal = '89377.5' where username = 'xyl';
update users set principal = '416480.7' where username = 'jsxl';
update users set principal = '355525.5' where username = 'zzdl';

/*
凯西营业部

{000002", "万科A", "20000", "9.854142572"},
{600581", "八一钢铁", "15000", "13.94915254"},
{600129", "太极集团", "20000", "12.02275673"},
{002162", "斯米克", "10000", "15.31081755"},
{600702", "沱牌曲酒", "10000", "22.78048853"},
{600741", "华域汽车", "10000", "13.69439681"},

剩余资金, "29242.2"


周山路
{600178", "东安动力", "10000", "11.91733799"},
{600439", "瑞贝卡", "20000", "10.92840977"},
{000968", "煤气化", "10000", "20.68215354"},
{000965", "天保基建", "15000", "13.31960808"},
{000407", "胜利股份", "10000", "7.680508475"},
{601877", "正泰电器", "3000", "22.94112662"},

剩余资金, "186959.74"


西苑路
{000968", "煤气化", "10000", "20.78255234"},
{000407", "胜利股份", "30000", "7.630309073"},
{600405", "动力源", "10000", "13.21248255"},
{000088", "盐田港", "10000", "7.138354935"},
{000630", "铜陵有色", "4000", "28.16186441"},

剩余资金, "339113.5"


建设西路
{600590", "泰豪科技", "10000", "14.07591226"},
{600967", "北方创业", "10000", "16.70636092"},
{600030", "中信证券", "10000", "16.72644068"},
{600439", "瑞贝卡", "5000", "11.33502493"},

剩余资金 "541852.2"


{中州东路"},
{000968", "煤气化", "5000", "20.29059821"},
{000407", "胜利股份", "20000", "7.630309073"},
{000002", "万科A", "30000", "9.589202393"},

剩余资金, "480645.2"
*/


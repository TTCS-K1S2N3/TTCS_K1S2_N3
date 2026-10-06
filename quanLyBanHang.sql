create database QuanLyBanHang;
use QuanLyBanHang;

create table Customer(
    cID int primary key,
    cName varchar(100) not null,
    cAge int check(cAge > 0)
);

create table Product(
    pID int primary key,
    pName varchar(100) not null,
    pPrice decimal(18,2) check(pPrice >= 0)
);

create table `Order`(
    oID int primary key,
    cID int not null,
    oDate date not null,
    oTotalPrice decimal(18,2),
    foreign key(cID) references Customer(cID)
);

create table OrderDetail(
    oID int,
    pID int,
    odQTY int not null check(odQTY > 0),
    primary key(oID,pID),
    foreign key(oID) references `Order`(oID),
    foreign key(pID) references Product(pID)
);
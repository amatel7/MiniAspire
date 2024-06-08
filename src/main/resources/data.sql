create table user (
    id int primary key auto_increment,
    username varchar(255) not null,
    password varchar(255) not null,
    token varchar(255) not null,
    role varchar(255) not null,
    UNIQUE (username),
    UNIQUE (token)
);
insert into user ( username, password,token,role) values ( 'user1', 'password1','token1','USER');
insert into user ( username, password,token,role) values ( 'user2', 'password2','token2','USER');
insert into user ( username, password,token,role) values ( 'user3', 'password3','token3','USER');
insert into user ( username, password,token,role) values ( 'user4', 'password4','token4','USER');
insert into user ( username, password,token,role) values ( 'user5', 'password5','token5','USER');
insert into user ( username, password,token,role) values ( 'user6', 'password6','token6','USER');
insert into user ( username, password,token,role) values ( 'admin1', 'admin-password7','admin-token6','ADMIN');


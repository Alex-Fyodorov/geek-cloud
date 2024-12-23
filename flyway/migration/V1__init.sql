create table accounts (
 id        bigint primary key auto_increment,
 username  varchar(30) not null unique,
 password  varchar(80) not null,
 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
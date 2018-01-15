

create table if not exists users (
       userId uuid not null primary key
       );


create table if not exists clicks (
       clickId uuid not null primary key,
       timestamp timestamptz not null,
       userId uuid not null references users (userId)
       );

create index clickTimestampIndex on clicks (timestamp);

create table if not exists impressions (
       impressionId uuid not null primary key,
       timestamp timestamptz not null,
       userId uuid not null references users (userId)
       );

create index impressionTimestampIndex on impressions (timestamp);



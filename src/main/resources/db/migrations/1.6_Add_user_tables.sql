-- liquibase formatted sql
-- changeset vangel:1.5-Add_user_tables splitStatements:false

create table app_user
(
    id  serial primary key,
    uid uuid default uuid_generate_v4() unique
);

alter table namespace
    add column user_uid serial references app_user (id) on delete cascade;

delete
from namespace
where user_uid is null;

-- liquibase formatted sql
-- changeset vangel:1.6-Add_user_tables splitStatements:false

create table if not exists app_user
(
    id  serial primary key,
    uid uuid default uuid_generate_v4() unique
);

alter table namespace
    add column if not exists user_uid serial references app_user (id) on delete cascade;

delete
from namespace
where user_uid is null;

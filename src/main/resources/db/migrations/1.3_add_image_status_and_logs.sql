-- liquibase formatted sql
-- changeset vangel:1.3-_add_image_status_and_logs splitStatements:false
create type image_status as enum ('initialized', 'started', 'completed', 'failed');
create cast (varchar as image_status) with inout as implicit;

alter table image_tag
    add column status image_status default 'completed' not null;

alter table image_tag
    alter column status drop default;

create table if not exists image_log
(
    id       serial primary key,
    data     text   not null,
    image_id serial not null unique references image_tag (id) on delete cascade
);

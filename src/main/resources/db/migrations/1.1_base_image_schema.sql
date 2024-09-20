-- liquibase formatted sql
-- changeset vangel:1.1_base_image_schema splitStatements:false


create table if not exists base_image
(
    id       serial primary key,
    language varchar(64) not null,
    version  varchar(64)
);

create table if not exists base_image_ref
(
    id serial primary key
);

create table if not exists base_image_git
(
    id         serial primary key,
    build_tool varchar(64) not null,
    version    varchar(64) not null,
    value      text        not null,
    base_id    serial      not null references base_image (id) on delete cascade,
    ref_id     serial      not null references base_image_ref (id) on delete cascade
);


create table if not exists base_image_exe
(
    id        serial primary key,
    value     text        not null,
    file_type varchar(64) not null,
    base_id   serial      not null references base_image (id) on delete cascade,
    ref_id    serial      not null references base_image_ref (id) on delete cascade
);


create type base_image_arg_type as enum ('string', 'file');
create type base_image_stage as enum ('runtime', 'build');
create table if not exists base_image_arg
(
    id                serial primary key,
    name              varchar(64)         not null,
    type              base_image_arg_type not null default 'string',
    stage             base_image_stage    not null default 'build',
    description       varchar(255)        not null,
    base_image_ref_id serial              not null references base_image_ref (id) on delete cascade
);

alter table image
    add column base_ref_id serial not null references base_image_ref (id);

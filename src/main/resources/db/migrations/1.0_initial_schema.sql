-- liquibase formatted sql
-- changeset vangel:1.0-initial-schema splitStatements:false
create extension if not exists hstore;
create extension if not exists "uuid-ossp";

create table if not exists namespace
(
    uid  uuid default uuid_generate_v4() primary key,
    name varchar(64) not null unique
);

create table if not exists application
(
    id            serial primary key,
    name          varchar(64) not null unique,
    namespace_uid uuid        not null references namespace (uid) on delete cascade
);

create table if not exists external_service
(
    id            serial primary key,
    name          varchar(64) not null,
    port          integer     not null,
    config        hstore      not null,
    namespace_uid uuid        not null references namespace (uid) on delete cascade
);

create table if not exists deployment
(
    id             serial primary key,
    name           varchar(64) not null,
    application_id serial      not null references application (id) on delete cascade
);

create table if not exists service
(
    id             serial primary key,
    name           varchar(128) not null,
    deployment_id  serial references deployment (id),
    application_id serial       not null references application (id) on delete cascade
);

create table if not exists image
(
    id            serial primary key,
    name          varchar(64) not null,
    version       varchar(64) not null,
    hash          uuid        not null,
    arguments     hstore      not null,
    namespace_uid uuid        not null references namespace (uid) on delete cascade
);

create table if not exists environment
(
    id   serial primary key,
    name varchar(64) not null
);

create table if not exists environment_value
(
    id             serial primary key,
    name           varchar(255) not null,
    value          varchar(255) not null,
    environment_id serial       not null references environment (id) on delete cascade
);

create table if not exists pod
(
    id              serial primary key,
    name            varchar(64)  not null,
    port            int          not null,
    workdir         varchar(255) not null,
    active_image_id serial       not null references image (id),
    deployment_id   serial       not null references deployment (id) on delete cascade,
    environment_id  serial       not null references environment (id)
);


create table if not exists service_port
(
    id          serial primary key,
    name        varchar(64) not null,
    port        integer     not null,
    pod_id      serial      not null references pod (id) on delete cascade,
    service_id  serial      not null references service (id) on delete cascade
);

create table if not exists ingress
(
    id             serial primary key,
    name           varchar(64) not null,
    application_id serial      not null references application (id) on delete cascade
);

create table if not exists ingress_rule
(
    id              serial primary key,
    path            varchar(255) not null default '/',
    domain          varchar(255),
    service_port_id serial       not null references service_port (id),
    ingress_id      serial       not null references ingress (id) on delete cascade
);


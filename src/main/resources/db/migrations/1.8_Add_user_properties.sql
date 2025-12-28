-- liquibase formatted sql
-- changeset vangel:1.8-Add_user_propertiez splitStatements:false

alter table app_user
    add column if not exists username   varchar(64),
    add column if not exists first_name varchar(64),
    add column if not exists last_name  varchar(64);
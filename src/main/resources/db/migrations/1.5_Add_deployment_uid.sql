-- liquibase formatted sql
-- changeset vangel:1.5-add_deployment_uid splitStatements:false

alter table deployment
    add column uid uuid default uuid_generate_v4() unique not null;


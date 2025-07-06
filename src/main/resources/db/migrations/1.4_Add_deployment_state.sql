-- liquibase formatted sql
-- changeset vangel:1.4-Add_deployment_state splitStatements:false
create type deployment_status as enum ('initial', 'started', 'stopped', 'failed');
create cast (varchar as deployment_status) with inout as implicit;

alter table deployment
    add column state deployment_status default 'initial' not null;


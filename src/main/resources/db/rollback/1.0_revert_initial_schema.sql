-- liquibase formatted sql
-- changeset vangel:1.0-revert-initial-schema splitStatements

drop table if exists ingress_rule;
drop table if exists ingress;
drop table if exists service_port;
drop table if exists service;
drop table if exists pod;
drop table if exists environment_value;
drop table if exists environment;
drop table if exists image;
drop table if exists deployment;
drop table if exists external_service;
drop table if exists application;
drop table if exists namespace;
drop extension if exists "uuid-ossp";
drop extension if exists hstore;

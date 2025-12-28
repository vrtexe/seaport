create user keycloak_user with password 'password';

create database keycloak
    with owner keycloak_user;

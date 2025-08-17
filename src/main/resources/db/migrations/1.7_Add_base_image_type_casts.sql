-- liquibase formatted sql
-- changeset vangel:1.7_Add_base_image_type_casts splitStatements:false
create cast (varchar as base_image_stage) with inout as implicit;
create cast (varchar as base_image_arg_type) with inout as implicit;

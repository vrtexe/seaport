#!/usr/bin/env bash

directory=$(dirname "$0")

dbScriptsSource="$directory/development/scripts/database"
dbScriptsDirectory="$directory/data/database/scripts"


mkdir -p $dbScriptsDirectory
cp -f $dbScriptsSource/* $dbScriptsDirectory
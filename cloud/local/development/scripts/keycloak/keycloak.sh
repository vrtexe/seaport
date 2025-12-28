#!/usr/bin/env bash

directory=$(dirname "$0")

kubectl -n sso exec deployments/keycloak-sso-deployment -- /bin/bash -c "mkdir -p /opt/keycloak/data/import/"

base64 $directory/main-realm.json | kubectl -n sso exec -i deployments/keycloak-sso-deployment -- /bin/bash -c "base64 -d > /opt/keycloak/data/import/main-realm.json"

kubectl -n sso exec deployments/keycloak-sso-deployment -- /bin/bash -c "/opt/keycloak/bin/kc.sh import --file /opt/keycloak/data/import/main-realm.json --optimized"

kubectl -n sso rollout restart deployment keycloak-sso-deployment

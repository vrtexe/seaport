#!/usr/bin/env bash

directory=$(dirname "$0")

$directory/cluster/kind-start.sh

$directory/setup.sh

kubectl apply -k $directory/development

kubectl config view --raw > $directory/kubeconfig.yaml

cp -f $directory/kubeconfig.yaml $directory/../../src/main/resources/

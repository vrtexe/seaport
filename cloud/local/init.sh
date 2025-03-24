#!/bin/sh

directory=$(dirname "$0")

$directory/cluster/kind-start.sh

kubectl apply -k $directory/development

kubectl config view --raw > $directory/kubeconfig.yaml

cp -f $directory/kubeconfig.yaml $directory/../../src/main/resources/

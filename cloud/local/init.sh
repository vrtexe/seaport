#!/bin/sh

directory=$(dirname "$0")

$directory/cluster/kind-start.sh

kubectl apply -k development

kubectl config view --raw > kubeconfig.yaml

cp $directory/kubeconfig.yaml $directory/../../src/main/resources/

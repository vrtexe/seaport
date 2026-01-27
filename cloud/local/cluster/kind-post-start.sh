#!/usr/bin/env bash

directory=$(dirname "$0")
cluster_name='kind-cluster'

if [[ -n "$1" ]]; then
  cluster_name="$1"
fi

$directory/kind-cluster-exec.sh $cluster_name 'echo "10.96.101.236 registry-service.registry-internal.svc.cluster.local" >> /etc/hosts'
$directory/kind-cluster-exec.sh $cluster_name 'echo "10.96.101.236 internal.io" >> /etc/hosts'
$directory/load-registry-config.sh $cluster_name

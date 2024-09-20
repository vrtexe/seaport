#!/bin/sh

cluster=$1
directory=$(dirname $0)


echo 'Started loading registry configs'

CONTAINERD_REGISTRY_CONFIG_PATH="/etc/containerd"

for registryPath in $directory/certs.d/*
do

  registry=${registryPath#$directory/}
  $directory/kind-cluster-exec.sh $cluster "mkdir -p $CONTAINERD_REGISTRY_CONFIG_PATH/$registry"

  for registryFilePath in $registryPath/*
  do
    registryFile=${registryFilePath#$directory/}

    $directory/kind-cluster-exec.sh $cluster "touch $CONTAINERD_REGISTRY_CONFIG_PATH/$registryFile"
    $directory/kind-cluster-exec.sh $cluster "echo '$(cat $registryFilePath)' > $CONTAINERD_REGISTRY_CONFIG_PATH/$registryFile"
  done

done

echo 'Done loading registry configs'




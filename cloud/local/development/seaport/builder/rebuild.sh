#!/bin/sh

directory=$(dirname "$0")

kubectl delete -f $directory/build.yaml
kubectl apply -k $directory/.
#!/bin/sh

directory=$(dirname "$0")
cluster_name='kind-cluster'

kind delete cluster -n $cluster_name

kind create cluster --config $directory/cluster.yaml

kind export kubeconfig -n $cluster_name

$directory/kind-cluster-exec.sh $cluster_name 'echo "10.96.101.236 registry-service.internal-registry.svc.cluster.local" >> /etc/hosts'
$directory/kind-cluster-exec.sh $cluster_name 'echo "10.96.101.236 internal.io" >> /etc/hosts'
$directory/load-registry-config.sh $cluster_name

kubectl apply -f $directory/nginx-ingress/deploy.yaml

kubectl rollout status deployment -n ingress-nginx ingress-nginx-controller
kubectl wait --namespace ingress-nginx \
  --for=condition=ready pod \
  --selector=app.kubernetes.io/component=controller \
  --timeout=180s



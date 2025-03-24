#!/bin/sh

cluster="$1"
command="$2"

for node in $(kind get nodes -n "$cluster")
do
  docker exec $node /bin/sh -c "$command"
done
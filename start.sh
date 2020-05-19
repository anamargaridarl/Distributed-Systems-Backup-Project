#!/bin/bash
TERMINAL=$1
DIMENSIONS=$(xdpyinfo | grep dimensions | sed -r 's/^[^0-9]*([0-9]+x[0-9]+).*$/\1/')
WIDTH=$(echo "$DIMENSIONS" | sed -r 's/x.*//')
HEIGHT=$(echo "$DIMENSIONS" | sed -r 's/.*x//')
echo "Launching Peers"
cd test/client
$TERMINAL --geometry 117x27+0+0 &
cd ../peer1
$TERMINAL --geometry 117x27+$(($WIDTH/4))+0 -e "java base.PeerInitial 1.0 1 peer1 5000" &
cd ../peer2
$TERMINAL --geometry 117x27+0+$(($HEIGHT/2+10)) -e "java base.PeerInitial 1.0 2 peer2 5001"  &
cd ../peer3
$TERMINAL --geometry 117x27+$(($WIDTH/4))+$(($HEIGHT/2+10)) -e "java base.PeerInitial 1.0 3 peer3 5002"
